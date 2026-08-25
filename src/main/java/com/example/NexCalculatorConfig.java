package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("nexcalculator")
public interface NexCalculatorConfig extends Config
{
    enum DisplayMode
    {
        SHOW,
        HIDE
    }

    @ConfigItem(
        keyName = "teamSize",
        name = "Team Size",
        description = "The number of players in your team.",
        position = 1
    )
    default int teamSize()
    {
        return 3;
    }

    @ConfigItem(keyName = "showHelm", name = "Show Torva Full Helm", description = "Toggle display for Torva Full Helm.", position = 2)
    default DisplayMode showHelm() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showBody", name = "Show Torva Platebody", description = "Toggle display for Torva Platebody.", position = 3)
    default DisplayMode showBody() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showLegs", name = "Show Torva Platelegs", description = "Toggle display for Torva Platelegs.", position = 4)
    default DisplayMode showLegs() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showVambs", name = "Show Zaryte Vambraces", description = "Toggle display for Zaryte Vambraces.", position = 5)
    default DisplayMode showVambs() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showHorn", name = "Show Nihil Horn", description = "Toggle display for Nihil Horn.", position = 6)
    default DisplayMode showHorn() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showHilt", name = "Show Ancient Hilt", description = "Toggle display for Ancient Hilt.", position = 7)
    default DisplayMode showHilt() { return DisplayMode.SHOW; }

    @ConfigItem(keyName = "showPet", name = "Show Nexling (Pet)", description = "Toggle display for Nexling.", position = 8)
    default DisplayMode showPet() { return DisplayMode.SHOW; }
}
