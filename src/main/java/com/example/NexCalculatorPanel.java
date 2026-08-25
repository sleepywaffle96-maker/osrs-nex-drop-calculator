package com.example;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class NexCalculatorPanel extends PluginPanel
{
    private final NexCalculatorConfig config;
    private final ConfigManager configManager;

    private final JLabel totalKillsLabel = new JLabel("Total Kills: 0");
    private final JLabel sessionKillsLabel = new JLabel("Session Kills: 0");

    private final JPanel expectedPanel = new JPanel();
    private final JPanel receivedPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsContainer = new JPanel(cardLayout);
    private final JButton expectedBtn = new JButton("Expected");
    private final JButton receivedBtn = new JButton("Received");

    private final JPanel sessionExpectedPanel = new JPanel();

    // Componenti per la scheda Manual (Con l'aggiunta del Team Size interno!)
    private final JSlider manualDamageSlider = new JSlider(0, 100, 20);
    private final JLabel manualSliderLabel = new JLabel("Average Damage: 20%");
    private final JComboBox<String> teamSizeComboBox = new JComboBox<>(new String[]{"Solo (1)", "Duo (2)", "Trio (3)", "4-man (4)", "5-man (5)", "Mass (6+)"});

    private final JPanel historyListContainer = new JPanel();
    private final JScrollPane historyScrollPane = new JScrollPane(historyListContainer);

    private int lastKills = 0;
    private int lastSessionKills = 0;
    private int lastLiveDamage = 0;
    private boolean isAdjustingSlider = false; // Evita loop infiniti nei calcoli combinati

    public NexCalculatorPanel(NexCalculatorConfig config, ConfigManager configManager)
    {
        this.config = config;
        this.configManager = configManager;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JLabel title = new JLabel("Nex Calculator", SwingConstants.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));
        totalKillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalKillsLabel.setForeground(Color.YELLOW);
        totalKillsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        headerPanel.add(title);
        headerPanel.add(totalKillsLabel);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Dialog", Font.BOLD, 12));

        // HUB 1: ALL
        JPanel allTabContent = new JPanel(new BorderLayout());
        JPanel subMenuPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        subMenuPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        expectedBtn.setFocusPainted(false);
        receivedBtn.setFocusPainted(false);
        setButtonSelected(expectedBtn, true);
        setButtonSelected(receivedBtn, false);

        expectedPanel.setLayout(new BoxLayout(expectedPanel, BoxLayout.Y_AXIS));
        receivedPanel.setLayout(new BoxLayout(receivedPanel, BoxLayout.Y_AXIS));
        cardsContainer.add(expectedPanel, "EXPECTED");
        cardsContainer.add(receivedPanel, "RECEIVED");

        expectedBtn.addActionListener(e -> {
            cardLayout.show(cardsContainer, "EXPECTED");
            setButtonSelected(expectedBtn, true);
            setButtonSelected(receivedBtn, false);
        });
        receivedBtn.addActionListener(e -> {
            cardLayout.show(cardsContainer, "RECEIVED");
            setButtonSelected(expectedBtn, false);
            setButtonSelected(receivedBtn, true);
        });

        subMenuPanel.add(expectedBtn);
        subMenuPanel.add(receivedBtn);
        allTabContent.add(subMenuPanel, BorderLayout.NORTH);
        allTabContent.add(cardsContainer, BorderLayout.CENTER);

        // HUB 2: SESSION
        JPanel sessionTabContent = new JPanel(new BorderLayout());
        sessionKillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sessionKillsLabel.setForeground(Color.CYAN);
        sessionKillsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        sessionKillsLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        sessionExpectedPanel.setLayout(new BoxLayout(sessionExpectedPanel, BoxLayout.Y_AXIS));

        sessionTabContent.add(sessionKillsLabel, BorderLayout.NORTH);
        sessionTabContent.add(sessionExpectedPanel, BorderLayout.CENTER);

        // HUB 3: MANUAL (Riprogettato con la calibrazione automatica del Team Size)
        JPanel manualTabContent = new JPanel();
        manualTabContent.setLayout(new BoxLayout(manualTabContent, BoxLayout.Y_AXIS));
        manualTabContent.setBorder(new EmptyBorder(15, 5, 15, 5));

        JLabel manualTitle = new JLabel("Historical Damage Calibration");
        manualTitle.setFont(new Font("Dialog", Font.BOLD, 14));
        manualTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pannello orizzontale compatto per impostare la dimensione della squadra
        JPanel teamPanel = new JPanel(new BorderLayout(5, 0));
        teamPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
        JLabel teamLabel = new JLabel("Team Setup: ");
        teamLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        teamSizeComboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
        teamPanel.add(teamLabel, BorderLayout.WEST);
        teamPanel.add(teamSizeComboBox, BorderLayout.CENTER);

        manualSliderLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        manualSliderLabel.setForeground(Color.ORANGE);
        manualSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        manualSliderLabel.setBorder(new EmptyBorder(15, 0, 5, 0));

        Integer savedManualDamage = configManager.getConfiguration("nexcalculator", "manual_damage_setup", Integer.class);
        int initialSliderVal = (savedManualDamage != null) ? savedManualDamage : 20;
        manualDamageSlider.setValue(initialSliderVal);
        manualSliderLabel.setText("Average Damage: " + initialSliderVal + "%");

        // Carica l'ultimo Team Size salvato
        Integer savedTeamSizeIdx = configManager.getConfiguration("nexcalculator", "manual_team_size_idx", Integer.class);
        if (savedTeamSizeIdx != null) {
            teamSizeComboBox.setSelectedIndex(savedTeamSizeIdx);
        } else {
            teamSizeComboBox.setSelectedIndex(2); // Seleziona Trio come default
        }

        // Azione del menu a cascata: se cambi la dimensione del team, muove lo slider da solo sulla quota teorica corretta!
        teamSizeComboBox.addActionListener(e -> {
            if (isAdjustingSlider) return;
            int idx = teamSizeComboBox.getSelectedIndex();
            configManager.setConfiguration("nexcalculator", "manual_team_size_idx", idx);
            int calculatedDmg = 20;
            switch(idx) {
                case 0: calculatedDmg = 100; break; // Solo
                case 1: calculatedDmg = 50;  break; // Duo
                case 2: calculatedDmg = 33;  break; // Trio
                case 3: calculatedDmg = 25;  break; // 4-man
                case 4: calculatedDmg = 20;  break; // 5-man
                case 5: calculatedDmg = 3;   break; // Mass (Media standard 3% a testa)
            }
            manualDamageSlider.setValue(calculatedDmg);
        });

        manualDamageSlider.setMajorTickSpacing(20);
        manualDamageSlider.setPaintTicks(true);
        manualDamageSlider.setPaintLabels(true);
        manualDamageSlider.setFont(new Font("Dialog", Font.BOLD, 12));
        manualDamageSlider.setBackground(getBackground());

        manualDamageSlider.addChangeListener(e -> {
            int value = manualDamageSlider.getValue();
            manualSliderLabel.setText("Average Damage: " + value + "%");
            configManager.setConfiguration("nexcalculator", "manual_damage_setup", value);
            updateDisplayWithLiveDamage(lastKills, lastSessionKills, lastLiveDamage);
        });

        manualTabContent.add(manualTitle);
        manualTabContent.add(teamPanel);
        manualTabContent.add(manualSliderLabel);
        manualTabContent.add(manualDamageSlider);

        // HUB 4: HISTORY
        JPanel historyTabContent = new JPanel(new BorderLayout());
        historyListContainer.setLayout(new BoxLayout(historyListContainer, BoxLayout.Y_AXIS));
        historyScrollPane.setBorder(null);
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        historyTabContent.add(historyScrollPane, BorderLayout.CENTER);

        tabs.addTab("All", allTabContent);
        tabs.addTab("Session", sessionTabContent);
        tabs.addTab("Manual", manualTabContent);
        tabs.addTab("History", historyTabContent);

        add(tabs, BorderLayout.CENTER);
    }

    private void setButtonSelected(JButton button, boolean selected)
    {
        if (selected) {
            button.setBackground(new Color(60, 60, 60));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Dialog", Font.BOLD, 13));
        } else {
            button.setBackground(new Color(35, 35, 35));
            button.setForeground(Color.GRAY);
            button.setFont(new Font("Dialog", Font.PLAIN, 12));
        }
    }
    private int getSavedReceivedCount(String key)
    {
        Integer count = configManager.getConfiguration("nexcalculator", "received_" + key, Integer.class);
        return (count != null) ? count : 0;
    }

    public void updateDisplayWithLiveDamage(int currentKills, int sessionKills, int liveDamagePercent)
    {
        this.lastKills = currentKills;
        this.lastSessionKills = sessionKills;
        this.lastLiveDamage = liveDamagePercent;

        totalKillsLabel.setText("Total Kills: " + currentKills);
        sessionKillsLabel.setText("Session Kills: " + sessionKills);

        expectedPanel.removeAll();
        receivedPanel.removeAll();
        sessionExpectedPanel.removeAll();
        historyListContainer.removeAll();

        int manualDamageSetup = manualDamageSlider.getValue();
        double manualFraction = manualDamageSetup / 100.0;

        double baseChanceSumAll = 0.0;
        double baseChanceSumSession = 0.0;

        for (int i = currentKills; i >= 1; i--)
        {
            Integer savedDamage = configManager.getConfiguration("nexcalculator", "kill_damage_" + i, Integer.class);
            double damageFraction = (savedDamage != null) ? (savedDamage / 100.0) : manualFraction;

            Boolean savedMvp = configManager.getConfiguration("nexcalculator", "kill_mvp_" + i, Boolean.class);
            boolean wasMvp = (savedMvp != null) && savedMvp;

            double currentBaseChance = wasMvp ? (1.0 / 43.0) * 1.10 : (1.0 / 43.0);
            baseChanceSumAll += currentBaseChance * damageFraction;

            double specificKillChance = (currentBaseChance * damageFraction) * 100.0;
            historyListContainer.add(new HistoryRowPanel(i, (int)(damageFraction * 100), wasMvp, specificKillChance));
        }

        for (int i = (currentKills - sessionKills + 1); i <= currentKills; i++)
        {
            if (i <= 0) continue;
            Integer savedDamage = configManager.getConfiguration("nexcalculator", "kill_damage_" + i, Integer.class);
            double damageFraction = (savedDamage != null) ? (savedDamage / 100.0) : manualFraction;

            Boolean savedMvp = configManager.getConfiguration("nexcalculator", "kill_mvp_" + i, Boolean.class);
            boolean wasMvp = (savedMvp != null) && savedMvp;

            double currentBaseChance = wasMvp ? (1.0 / 43.0) * 1.10 : (1.0 / 43.0);
            baseChanceSumSession += currentBaseChance * damageFraction;
        }

        if (liveDamagePercent > 0)
        {
            double liveDamageFraction = liveDamagePercent / 100.0;
            baseChanceSumAll += (1.0 / 43.0) * liveDamageFraction;
            baseChanceSumSession += (1.0 / 43.0) * liveDamageFraction;
        }

        double rawAnyAll = baseChanceSumAll * 100.0;
        double rawAnySession = baseChanceSumSession * 100.0;

        double petBase = 1.0 / 500.0;
        double rawPetAll = (currentKills * petBase) * 100.0;
        double rawPetSession = (sessionKills * petBase) * 100.0;

        expectedPanel.add(new DropRowPanel("Any Unique", rawAnyAll, 1.0, Color.ORANGE, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Full Helm", rawAnyAll, (2.0 / 12.0), Color.RED, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Platebody", rawAnyAll, (2.0 / 12.0), Color.RED, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Platelegs", rawAnyAll, (2.0 / 12.0), Color.RED, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Zaryte Vambraces", rawAnyAll, (1.0 / 12.0), Color.RED, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Nihil Horn", rawAnyAll, (2.0 / 12.0), Color.ORANGE, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Ancient Hilt", rawAnyAll, (1.0 / 12.0), Color.MAGENTA, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Nexling (Pet)", rawPetAll, 1.0, Color.CYAN, "nexling.png"));

        receivedPanel.add(new DropRowPanel("Any Unique", getSavedReceivedCount("any_unique"), Color.GRAY, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Full Helm", getSavedReceivedCount("torva_helm"), Color.GRAY, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Platebody", getSavedReceivedCount("torva_body"), Color.GRAY, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Platelegs", getSavedReceivedCount("torva_legs"), Color.GRAY, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Zaryte Vambraces", getSavedReceivedCount("zaryte_vambs"), Color.GRAY, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Nihil Horn", getSavedReceivedCount("nihil_horn"), Color.GRAY, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Ancient Hilt", getSavedReceivedCount("ancient_hilt"), Color.GRAY, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Nexling (Pet)", getSavedReceivedCount("nexling_pet"), Color.GRAY, "nexling.png"));

        sessionExpectedPanel.add(new DropRowPanel("Any Unique", rawAnySession, 1.0, Color.ORANGE, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Full Helm", rawAnySession, (2.0 / 12.0), Color.RED, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Platebody", rawAnySession, (2.0 / 12.0), Color.RED, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Platelegs", rawAnySession, (2.0 / 12.0), Color.RED, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Zaryte Vambraces", rawAnySession, (1.0 / 12.0), Color.RED, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Nihil Horn", rawAnySession, (2.0 / 12.0), Color.ORANGE, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Ancient Hilt", rawAnySession, (1.0 / 12.0), Color.MAGENTA, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Nexling (Pet)", rawPetSession, 1.0, Color.CYAN, "nexling.png"));

        expectedPanel.revalidate();
        expectedPanel.repaint();
        receivedPanel.revalidate();
        receivedPanel.repaint();
        sessionExpectedPanel.revalidate();
        sessionExpectedPanel.repaint();
        historyListContainer.revalidate();
        historyListContainer.repaint();
    }
    private class DropRowPanel extends JPanel
    {
        private final String itemName;
        private final double rawProgressValue;
        private final double dropWeight;
        private final boolean isExpectedMode;
        private final int receivedCount;
        private final BufferedImage itemIcon;

        public DropRowPanel(String name, double rawValue, double weight, Color barColor, String iconPath)
        {
            this.itemName = name;
            this.rawProgressValue = rawValue * weight;
            this.dropWeight = weight;
            this.isExpectedMode = true;
            this.receivedCount = 0;
            this.itemIcon = loadIcon(iconPath);
            setupPanelDimensions();
        }

        public DropRowPanel(String name, int receivedCount, Color barColor, String iconPath)
        {
            this.itemName = name;
            this.rawProgressValue = 0;
            this.dropWeight = 0;
            this.isExpectedMode = false;
            this.receivedCount = receivedCount;
            this.itemIcon = loadIcon(iconPath);
            setupPanelDimensions();
        }

        private void setupPanelDimensions()
        {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(getPreferredSize().width, 36));
            setBorder(new EmptyBorder(4, 5, 4, 5));
            setBackground(new Color(30, 30, 30));
        }

        private BufferedImage loadIcon(String path)
        {
            try {
                return net.runelite.client.util.ImageUtil.loadImageResource(NexCalculatorPanel.class, path);
            } catch (Exception e) {
                return new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            }
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (itemIcon != null)
            {
                int iconY = (getHeight() - 18) / 2;
                g2d.drawImage(itemIcon, 2, iconY, 18, 18, null);
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Dialog", Font.BOLD, 14));
            FontMetrics metrics = g2d.getFontMetrics();
            int outerTextY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

            String outerNumberText = isExpectedMode ? String.valueOf((int) (rawProgressValue / 100.0)) : String.valueOf(receivedCount);
            g2d.drawString(outerNumberText, 24, outerTextY);

            int barX = 45;
            int barY = 2;
            int barWidth = getWidth() - barX - 5;
            int barHeight = getHeight() - 4;

            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(barX, barY, barWidth, barHeight);

            String innerText = "";

            if (isExpectedMode)
            {
                double barraPercent = rawProgressValue % 100.0;
                if (rawProgressValue > 0)
                {
                    int fillWidth = (int) ((barWidth * Math.min(100.0, barraPercent)) / 100.0);
                    g2d.setColor(new Color(40, 70, 120, 150));
                    g2d.fillRect(barX, barY, fillWidth, barHeight);
                }
                innerText = String.format("%.1f%%", barraPercent);
            }
            else
            {
                int manualSetup = manualDamageSlider.getValue();
                double damageFraction = manualSetup / 100.0;
                double baseChanceSum = 0.0;
                for (int i = 1; i <= lastKills; i++)
                {
                    Integer savedDamage = configManager.getConfiguration("nexcalculator", "kill_damage_" + i, Integer.class);
                    baseChanceSum += (savedDamage != null ? savedDamage / 100.0 : damageFraction) * (1.0 / 43.0);
                }

                double targetWeight = 1.0;
                if (itemName.contains("Helm") || itemName.contains("Body") || itemName.contains("Legs") || itemName.contains("Horn")) targetWeight = 2.0 / 12.0;
                if (itemName.contains("Vambraces") || itemName.contains("Hilt")) targetWeight = 1.0 / 12.0;

                double expectedValue = (itemName.contains("Pet")) ? (lastKills * (1.0 / 500.0)) : (baseChanceSum * 100.0 * targetWeight) / 100.0;
                double diff = receivedCount - expectedValue;

                int midX = barX + (barWidth / 2);

                if (diff < 0)
                {
                    double luckFactor = Math.min(1.0, Math.abs(diff) / Math.max(1.0, expectedValue));
                    int fillWidth = (int) ((barWidth / 2) * luckFactor);
                    g2d.setColor(new Color(150, 40, 40, 180));
                    g2d.fillRect(midX - fillWidth, barY, fillWidth, barHeight);
                    innerText = String.format("-%.2f", Math.abs(diff));
                }
                else
                {
                    double luckFactor = expectedValue == 0 ? (receivedCount > 0 ? 1.0 : 0.0) : Math.min(1.0, diff / expectedValue);
                    int fillWidth = (int) ((barWidth / 2) * luckFactor);
                    g2d.setColor(new Color(40, 130, 40, 180));
                    g2d.fillRect(midX, barY, fillWidth, barHeight);
                    innerText = String.format("+%.2f", diff);
                }

                g2d.setColor(new Color(70, 70, 70));
                g2d.fillRect(midX, barY, 1, barHeight);
            }

            g2d.setColor(new Color(90, 90, 90));
            g2d.drawRect(barX, barY, barWidth, barHeight);

            g2d.setColor(Color.WHITE);
            int textX = barX + (barWidth - metrics.stringWidth(innerText)) / 2;
            int textY = barY + (barHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(innerText, textX, textY);
        }
    }

    private class HistoryRowPanel extends JPanel
    {
        public HistoryRowPanel(int killNum, int damagePercent, boolean isMvp, double dropChance)
        {
            setLayout(new GridLayout(1, 4, 2, 0));
            setPreferredSize(new Dimension(getPreferredSize().width, 26));
            setBorder(new EmptyBorder(2, 5, 2, 5));
            setBackground(killNum % 2 == 0 ? new Color(35, 35, 35) : new Color(42, 42, 42));

            JLabel labelKill = new JLabel("K #" + killNum);
            labelKill.setFont(new Font("Dialog", Font.BOLD, 12));
            labelKill.setForeground(Color.LIGHT_GRAY);

            JLabel labelDmg = new JLabel(damagePercent + "% Dmg");
            labelDmg.setFont(new Font("Dialog", Font.PLAIN, 11));
            labelDmg.setForeground(Color.WHITE);

            JLabel labelMvp = new JLabel(isMvp ? "MVP" : "-");
            labelMvp.setFont(new Font("Dialog", Font.BOLD, 11));
            labelMvp.setForeground(isMvp ? Color.YELLOW : Color.DARK_GRAY);
            labelMvp.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel labelChance = new JLabel("+" + String.format("%.2f%%", dropChance));
            labelChance.setFont(new Font("Dialog", Font.BOLD, 11));
            labelChance.setForeground(Color.GREEN);
            labelChance.setHorizontalAlignment(SwingConstants.RIGHT);

            add(labelKill);
            add(labelDmg);
            add(labelMvp);
            add(labelChance);
        }
    }
}
