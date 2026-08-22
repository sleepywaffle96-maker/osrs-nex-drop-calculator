package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("nexcalculator")
public interface NexCalculatorConfig extends Config
{
    @ConfigItem(
            keyName = "damagePercentage",
            name = "Percentuale Danno",
            description = "La tua percentuale media di danno inflitto a Nex (es. 33 per Trio)",
            position = 1
    )
    @Range(min = 1, max = 100)
    default int damagePercentage()
    {
        return 33;
    }

    @ConfigItem(
            keyName = "isMvp",
            name = "Bonus MVP Attivo",
            description = "Spunta se ottieni regolarmente il ruolo di MVP (+10% di probabilità)",
            position = 2
    )
    default boolean isMvp()
    {
        return false;
    }
}
