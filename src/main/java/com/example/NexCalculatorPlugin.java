package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;

@Slf4j
@PluginDescriptor(
        name = "Nex Static Calculator",
        description = "Calculates Nex drop probabilities, luck percentiles, history list and session stats.",
        tags = {"nex", "calculator", "drop", "luck", "session", "delve", "history"}
)
public class NexCalculatorPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private NexCalculatorConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ClientThread clientThread; // Sblocca il gestore dei thread ufficiale di RuneLite

    private NexCalculatorPanel panel;
    private NavigationButton navButton;

    private int currentKc = 0;
    private int startingSessionKc = -1;
    private int sessionKc = 0;
    private long startXp = -1;
    private int accumulatedDamage = 0;
    private boolean fightingNex = false;
    private int lastLiveDamagePercent = 0;
    private boolean isMvpThisKill = false;

    private static final Pattern NEX_KC_PATTERN = Pattern.compile("Your Nex (?:kill )?count is:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    protected void startUp() throws Exception
    {
        panel = new NexCalculatorPanel(config, configManager);

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "nex_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Nex Calculator")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        Integer savedKc = configManager.getConfiguration("nexcalculator", "current_kc", Integer.class);
        if (savedKc != null)
        {
            currentKc = savedKc;
        }
        panel.updateDisplayWithLiveDamage(currentKc, sessionKc, 0);
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        if ("nexcalculator".equals(configChanged.getGroup()))
        {
            if (panel != null)
            {
                panel.updateDisplayWithLiveDamage(currentKc, sessionKc, lastLiveDamagePercent);
            }
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        // Intercetta l'ID del pacchetto del Collection Log
        if (event.getGroupId() == 210)
        {
            // Esegue l'istruzione sul thread grafico corretto senza mandare in blocco il gioco
            clientThread.invokeLater(() -> {
                Widget collectionLogWidget = client.getWidget(210, 2);
                if (collectionLogWidget != null)
                {
                    searchCollectionLog(collectionLogWidget);
                }
            });
        }
    }

    private void searchCollectionLog(Widget widget)
    {
        if (widget == null) return;

        String text = widget.getText();
        if (text != null && !text.isEmpty())
        {
            String cleanText = Text.sanitizeMultilineText(text);

            if (cleanText.contains("Nex kills:"))
            {
                Matcher matcher = Pattern.compile("Nex\\s*kills?:\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(cleanText);
                if (matcher.find())
                {
                    int extractedKc = Integer.parseInt(matcher.group(1));
                    if (startingSessionKc == -1)
                    {
                        startingSessionKc = extractedKc;
                    }
                    sessionKc = extractedKc - startingSessionKc;
                    currentKc = extractedKc;
                    configManager.setConfiguration("nexcalculator", "current_kc", currentKc);
                }
            }

            checkAndSaveDropCount(cleanText, "Torva full helm", "torva_helm");
            checkAndSaveDropCount(cleanText, "Torva platebody", "torva_body");
            checkAndSaveDropCount(cleanText, "Torva platelegs", "torva_legs");
            checkAndSaveDropCount(cleanText, "Zaryte vambraces", "zaryte_vambs");
            checkAndSaveDropCount(cleanText, "Nihil horn", "nihil_horn");
            checkAndSaveDropCount(cleanText, "Ancient hilt", "ancient_hilt");
            checkAndSaveDropCount(cleanText, "Nexling", "nexling_pet");
        }

        Widget[] staticChildren = widget.getStaticChildren();
        if (staticChildren != null) { for (Widget child : staticChildren) searchCollectionLog(child); }

        Widget[] dynamicChildren = widget.getDynamicChildren();
        if (dynamicChildren != null) { for (Widget child : dynamicChildren) searchCollectionLog(child); }

        Widget[] nestedChildren = widget.getNestedChildren();
        if (nestedChildren != null) { for (Widget child : nestedChildren) searchCollectionLog(child); }

        if (panel != null)
        {
            panel.updateDisplayWithLiveDamage(currentKc, sessionKc, 0);
        }
    }

    private void checkAndSaveDropCount(String cleanText, String itemName, String configKey)
    {
        if (cleanText.toLowerCase().contains(itemName.toLowerCase()))
        {
            Matcher matcher = Pattern.compile(Pattern.quote(itemName) + ":\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(cleanText);
            if (matcher.find())
            {
                int count = Integer.parseInt(matcher.group(1));
                configManager.setConfiguration("nexcalculator", "received_" + configKey, count);
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (chatMessage.getType() == net.runelite.api.ChatMessageType.GAMEMESSAGE)
        {
            String message = chatMessage.getMessage();
            String cleanMessage = Text.sanitizeMultilineText(message);

            if (cleanMessage.contains("MVP:") || cleanMessage.toLowerCase().contains("most valuable player"))
            {
                isMvpThisKill = true;
            }

            Matcher matcher = NEX_KC_PATTERN.matcher(cleanMessage);
            if (matcher.find())
            {
                currentKc = Integer.parseInt(matcher.group(1));
                if (startingSessionKc == -1) startingSessionKc = currentKc - 1;
                sessionKc = currentKc - startingSessionKc;

                int finalDamage = (lastLiveDamagePercent > 0) ? lastLiveDamagePercent : configManager.getConfiguration("nexcalculator", "manual_damage_setup", Integer.class);

                configManager.setConfiguration("nexcalculator", "kill_damage_" + currentKc, finalDamage);
                configManager.setConfiguration("nexcalculator", "kill_mvp_" + currentKc, isMvpThisKill);
                configManager.setConfiguration("nexcalculator", "current_kc", currentKc);

                if (panel != null)
                {
                    panel.updateDisplayWithLiveDamage(currentKc, sessionKc, 0);
                }

                startXp = -1;
                accumulatedDamage = 0;
                fightingNex = false;
                lastLiveDamagePercent = 0;
                isMvpThisKill = false;
            }
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged npcChanged)
    {
        if (npcChanged.getNpc() != null && npcChanged.getNpc().getId() == 11278)
        {
            fightingNex = true;
            if (startXp == -1)
            {
                startXp = client.getOverallExperience();
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged)
    {
        if (fightingNex && startXp != -1)
        {
            long currentXp = client.getOverallExperience();
            long xpGained = currentXp - startXp;
            accumulatedDamage = (int) (xpGained / 4);

            lastLiveDamagePercent = (int) (((double) accumulatedDamage / 3400.0) * 100.0);

            if (panel != null)
            {
                panel.updateDisplayWithLiveDamage(currentKc, sessionKc, lastLiveDamagePercent);
            }
        }
    }

    @Provides
    NexCalculatorConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NexCalculatorConfig.class);
    }
}
