package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = "Nex Static Calculator",
        description = "Calculates Nex drop probabilities based on static damage percentages for Trios or Masses.",
        tags = {"nex", "calculator", "drop", "trio"}
)
public class NexCalculatorPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread; // Corretto: inserito il gestore dei thread ufficiale di RuneLite
    @Inject private ClientToolbar clientToolbar;
    @Inject private NexCalculatorConfig config;

    private NexCalculatorPanel panel;
    private NavigationButton navButton;
    private int currentKc = 0;
    private int startXp = -1;
    private int accumulatedDamage = 0;
    private boolean fightingNex = false;

    private static final Pattern NEX_KC_PATTERN = Pattern.compile("Your Nex count is: (\\d+)");

    @Override
    protected void startUp() throws Exception
    {
        panel = new NexCalculatorPanel(config);

        final BufferedImage icon = net.runelite.client.util.ImageUtil.loadImageResource(getClass(), "nex_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Nex Calculator")
                .icon(icon)
                .priority(6)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
        panel.updateDisplay(currentKc);
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        String message = chatMessage.getMessage();
        Matcher matcher = NEX_KC_PATTERN.matcher(message);
        if (matcher.find())
        {
            currentKc = Integer.parseInt(matcher.group(1));
            panel.updateDisplay(currentKc);
            startXp = -1;
            accumulatedDamage = 0;
            fightingNex = false;
        }
    }

    @Subscribe
    public void onWidgetLoaded(net.runelite.api.events.WidgetLoaded widgetLoaded)
    {
        // 621 è l'ID del gruppo per il Collection Log nelle nuove API di RuneLite
        if (widgetLoaded.getGroupId() == 621)
        {
            // Corretto: adesso usiamo clientThread.invoke() come richiesto dalle API moderne
            clientThread.invoke(() -> {
                net.runelite.api.widgets.Widget titleWidget = client.getWidget(621, 2);
                if (titleWidget != null && titleWidget.getText().contains("Nex"))
                {
                    net.runelite.api.widgets.Widget contentWidget = client.getWidget(210, 2);
                    if (contentWidget != null)
                    {
                        String text = contentWidget.getText();
                        Matcher matcher = Pattern.compile("Kills: (\\d+)").matcher(text);
                        if (matcher.find())
                        {
                            currentKc = Integer.parseInt(matcher.group(1));
                            panel.updateDisplay(currentKc);
                        }
                    }
                }
            });
        }
    }

    @Subscribe
    public void onNpcChanged(net.runelite.api.events.NpcChanged npcChanged)
    {
        if (npcChanged.getNpc() != null && npcChanged.getNpc().getId() == 11278) // 11278 = ID di Nex
        {
            fightingNex = true;
            if (startXp == -1)
            {
                startXp = client.getOverallExperience();
            }
        }
    }

    @Subscribe
    public void onStatChanged(net.runelite.api.events.StatChanged statChanged)
    {
        if (fightingNex && startXp != -1)
        {
            int currentXp = client.getOverallExperience();
            int xpGained = currentXp - startXp;
            accumulatedDamage = xpGained / 4; // 4 XP = 1 Danno in OSRS
            
            int liveDamagePercent = (int) (((double) accumulatedDamage / 3400.0) * 100.0);
            
            if (panel != null)
            {
                panel.updateDisplayWithLiveDamage(currentKc, liveDamagePercent);
            }
        }
    }

    @Provides
    NexCalculatorConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NexCalculatorConfig.class);
    }
}
