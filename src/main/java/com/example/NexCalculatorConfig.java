package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("nexcalculator")
public interface NexCalculatorConfig extends Config
{
    enum DisplayMode
    {
        SHOW,
        HIDE
    }

    enum AutoOpenMode
    {
        REGION,
        SCOREBOARD,
        NEVER
    }

    @ConfigSection(
            name = "Panel Settings",
            description = "Core configurations for the calculator interface",
            position = 0
    )
    String panelSection = "panelSection";

    @ConfigSection(
            name = "Reward Display",
            description = "Choose which specific rare drops to show or hide in the lateral panel",
            position = 1
    )
    String rewardSection = "rewardSection";

    @ConfigItem(
            keyName = "damagePercentage",
            name = "Damage Percentage",
            description = "Your fixed average damage percentage during the kills",
            position = 1,
            section = panelSection
    )
    default int damagePercentage()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "teamSize",
            name = "Team Size",
            description = "The number of players in your team for automatic scaling",
            position = 2,
            section = panelSection
    )
    default int teamSize()
    {
        return 3;
    }

    @ConfigItem(
            keyName = "autoOpen",
            name = "Auto Open on",
            description = "Select when the side panel should automatically slide open",
            position = 3,
            section = panelSection
    )
    default AutoOpenMode autoOpen()
    {
        return AutoOpenMode.REGION;
    }

    @ConfigItem(
            keyName = "hideAfter",
            name = "Hide plugin after",
            description = "Time in minutes to hide the panel after leaving the area",
            position = 4,
            section = panelSection
    )
    default int hideAfter()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "showHelm",
            name = "Torva Full Helm",
            description = "Toggle visibility of Torva Full Helm",
            position = 1,
            section = rewardSection
    )
    default DisplayMode showHelm()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showBody",
            name = "Torva Platebody",
            description = "Toggle visibility of Torva Platebody",
            position = 2,
            section = rewardSection
    )
    default DisplayMode showBody()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showLegs",
            name = "Torva Platelegs",
            description = "Toggle visibility of Torva Platelegs",
            position = 3,
            section = rewardSection
    )
    default DisplayMode showLegs()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showVambs",
            name = "Zaryte Vambraces",
            description = "Toggle visibility of Zaryte Vambraces",
            position = 4,
            section = rewardSection
    )
    default DisplayMode showVambs()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showHorn",
            name = "Nihil Horn",
            description = "Toggle visibility of Nihil Horn",
            position = 5,
            section = rewardSection
    )
    default DisplayMode showHorn()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showHilt",
            name = "Ancient Hilt",
            description = "Toggle visibility of Ancient Hilt",
            position = 6,
            section = rewardSection
    )
    default DisplayMode showHilt()
    {
        return DisplayMode.SHOW;
    }

    @ConfigItem(
            keyName = "showPet",
            name = "Nexling (Pet)",
            description = "Toggle visibility of Nexling pet",
            position = 7,
            section = rewardSection
    )
    default DisplayMode showPet()
    {
        return DisplayMode.SHOW;
    }
}
