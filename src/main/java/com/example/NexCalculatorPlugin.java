package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = "Nex Static Calculator",
        description = "Calcola la probabilità dei drop basandosi su una percentuale di danno fissa",
        tags = {"nex", "calculator", "drop", "trio"}
)
public class NexCalculatorPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientToolbar clientToolbar;
    @Inject private NexCalculatorConfig config;

    private NexCalculatorPanel panel;
    private NavigationButton navButton;
    private int currentKc = 0;

    // Pattern per leggere il KC dalla chat quando killi il boss o controlli il log
    private static final Pattern NEX_KC_PATTERN = Pattern.compile("Your Nex count is: (\\d+)");

    @Override
    protected void startUp() throws Exception
    {
        panel = new NexCalculatorPanel(config);

        // Carica l'icona dalle risorse (se non c'è ancora, caricherà un'icona di fallback temporanea)
        BufferedImage icon;
        try {
            icon = ImageUtil.loadImageResource(getClass(), "/nex_icon.png");
        } catch (Exception e) {
            icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        }

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
        // Legge il KC direttamente quando appare il messaggio verde in chat alla morte di Nex
        String message = chatMessage.getMessage();
        Matcher matcher = NEX_KC_PATTERN.matcher(message);
        if (matcher.find())
        {
            currentKc = Integer.parseInt(matcher.group(1));
            panel.updateDisplay(currentKc);
        }
    }

    @Provides
    NexCalculatorConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NexCalculatorConfig.class);
    }
}
