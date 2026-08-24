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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
    name = "Nex Static Calculator",
    description = "Calculates Nex drop probabilities based on damage and MVP history.",
    tags = {"nex", "calculator", "drop", "trio", "mass", "luck"}
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

    private NexCalculatorPanel panel;
    private NavigationButton navButton;

    private int currentKc = 0;
    private long startXp = -1;
    private int accumulatedDamage = 0;
    private boolean fightingNex = false;
    private int lastLiveDamagePercent = 0;
    private boolean isMvpThisKill = false; // Flag per il bonus MVP

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
            panel.updateDisplayWithLiveDamage(currentKc, 0);
        }
    }

    @Override
    protected void shutDown() throws Exception
    {
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (chatMessage.getType() == net.runelite.api.ChatMessageType.GAMEMESSAGE)
        {
            String message = chatMessage.getMessage();
            
            // Rileva se sei l'MVP della stanza (messaggio standard di RuneLite/OSRS)
            if (message.contains("MVP:") || message.toLowerCase().contains("most valuable player"))
            {
                isMvpThisKill = true;
            }

            Matcher matcher = NEX_KC_PATTERN.matcher(message);
            if (matcher.find())
            {
                currentKc = Integer.parseInt(matcher.group(1));
                
                // SALVATAGGIO PERSISTENTE: Scrive sul PC sia il danno che lo stato MVP
                configManager.setConfiguration("nexcalculator", "kill_damage_" + currentKc, lastLiveDamagePercent);
                configManager.setConfiguration("nexcalculator", "kill_mvp_" + currentKc, isMvpThisKill);
                configManager.setConfiguration("nexcalculator", "current_kc", currentKc);
                
                panel.updateDisplayWithLiveDamage(currentKc, 0);
                
                // Reset totale per il boss successivo
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
                panel.updateDisplayWithLiveDamage(currentKc, lastLiveDamagePercent);
            }
        }
    }

    @Provides
    NexCalculatorConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NexCalculatorConfig.class);
    }
}
