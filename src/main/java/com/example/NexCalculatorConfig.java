package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("nexcalculator")
public interface NexCalculatorConfig extends Config
{
    // 1. ELENCO OPZIONI PER I MENU A TENDINA (ENUM)
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

    // 2. LE SEZIONI COMPRIMIBILI CON LE FRECCETTE
    @ConfigSection(
        name = "Panel Settings",
        description = "Core configurations for the calculator interface",
        position = 0
    )
    String panelSection = "panelSection";

    @ConfigSection(
        name = "Reward Display",
        description = "Choose which rare drops to show or hide in the lateral panel",
        position = 1
    )
    String rewardSection = "rewardSection";

    // 3. OPZIONI DENTRO "PANEL SETTINGS"
    @ConfigItem(
        keyName = "damagePercentage",
        name = "Damage Percentage",
        description = "Your fixed average damage percentage during the kills",
        position = 1,
        section = panelSection
    )
    default int damagePercentage()
    {
        return 0; // Parte da 0%
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
        return 3; // Parte da 3 (Trio)
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
        return AutoOpenMode.REGION; // Menu a tendina: Region, Scoreboard, Never
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
        return 10; // Casella per i minuti
    }

    // 4. OPZIONI DENTRO "REWARD DISPLAY" (MENU A TENDINA SHOW/HIDE)
    @ConfigItem(
        keyName = "showTorva",
        name = "Torva Armor",
        description = "Toggle visibility of Torva Full Helm, Platebody, and Platelegs pieces",
        position = 1,
        section = rewardSection
    )
    default DisplayMode showTorva()
    {
        return DisplayMode.SHOW; // Menu a tendina: SHOW / HIDE
    }

    @ConfigItem(
        keyName = "showWeapon",
        name = "Rare Weapons",
        description = "Toggle visibility of the Zaryte Crossbow and Nihil Horn weapons",
        position = 2,
        section = rewardSection
    )
    default DisplayMode showWeapon()
    {
        return DisplayMode.SHOW; // Menu a tendina: SHOW / HIDE
    }

    @ConfigItem(
        keyName = "showHilt",
        name = "Ancient Hilt",
        description = "Toggle visibility of the Ancient Hilt item in the main list",
        position = 3,
        section = rewardSection
    )
    default DisplayMode showHilt()
    {
        return DisplayMode.SHOW; // Menu a tendina: SHOW / HIDE
    }
}
