package com.example;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import net.runelite.client.util.ImageUtil;

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

    private final JSlider manualDamageSlider = new JSlider(0, 100, 20);
    private final JLabel manualSliderLabel = new JLabel("Average Damage: 20%");

    private int lastKills = 0;
    private int lastSessionKills = 0;
    private int lastLiveDamage = 0;

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
        tabs.setFont(new Font("Dialog", Font.BOLD, 13));

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

        JPanel sessionTabContent = new JPanel(new BorderLayout());
        sessionKillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sessionKillsLabel.setForeground(Color.CYAN);
        sessionKillsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        sessionKillsLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        sessionExpectedPanel.setLayout(new BoxLayout(sessionExpectedPanel, BoxLayout.Y_AXIS));

        sessionTabContent.add(sessionKillsLabel, BorderLayout.NORTH);
        sessionTabContent.add(sessionExpectedPanel, BorderLayout.CENTER);

        JPanel manualTabContent = new JPanel();
        manualTabContent.setLayout(new BoxLayout(manualTabContent, BoxLayout.Y_AXIS));
        manualTabContent.setBorder(new EmptyBorder(15, 5, 15, 5));

        JLabel manualTitle = new JLabel("Historical Damage Calibration");
        manualTitle.setFont(new Font("Dialog", Font.BOLD, 14));
        manualTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        manualSliderLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        manualSliderLabel.setForeground(Color.ORANGE);
        manualSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        manualSliderLabel.setBorder(new EmptyBorder(15, 0, 10, 0));

        Integer savedManualDamage = configManager.getConfiguration("nexcalculator", "manual_damage_setup", Integer.class);
        if (savedManualDamage != null) {
            manualDamageSlider.setValue(savedManualDamage);
            manualSliderLabel.setText("Average Damage: " + savedManualDamage + "%");
        }

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
        manualTabContent.add(manualSliderLabel);
        manualTabContent.add(manualDamageSlider);

        tabs.addTab("All", allTabContent);
        tabs.addTab("Session", sessionTabContent);
        tabs.addTab("Manual", manualTabContent);

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

        // Legge lo slider per impostare la percentuale manuale
        int manualDamageSetup = manualDamageSlider.getValue();
        double manualFraction = manualDamageSetup / 100.0;

        double baseChanceSumAll = 0.0;
        double baseChanceSumSession = 0.0;

        // --- CALCOLO PER LA SCHEDA "ALL" (STORICO COMPLETO) ---
        for (int i = 1; i <= currentKills; i++)
        {
            Integer savedDamage = configManager.getConfiguration("nexcalculator", "kill_damage_" + i, Integer.class);
            double damageFraction = (savedDamage != null) ? (savedDamage / 100.0) : manualFraction;

            Boolean savedMvp = configManager.getConfiguration("nexcalculator", "kill_mvp_" + i, Boolean.class);
            boolean wasMvp = (savedMvp != null) && savedMvp;

            double currentBaseChance = wasMvp ? (1.0 / 43.0) * 1.10 : (1.0 / 43.0);
            baseChanceSumAll += currentBaseChance * damageFraction;
        }

        // --- CALCOLO PER LA SCHEDA "SESSION" (SOLO I KILL DI OGGI) ---
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

        // Danno live in combattimento
        if (liveDamagePercent > 0)
        {
            double liveDamageFraction = liveDamagePercent / 100.0;
            baseChanceSumAll += (1.0 / 43.0) * liveDamageFraction;
            baseChanceSumSession += (1.0 / 43.0) * liveDamageFraction;
        }

        // Calcoli statistici
        double chanceAll = (1.0 - Math.exp(-baseChanceSumAll)) * 100.0;
        double chanceSession = (1.0 - Math.exp(-baseChanceSumSession)) * 100.0;

        double petBase = 1.0 / 500.0;
        double pPetAll = (1.0 - Math.pow(1.0 - petBase, currentKills)) * 100.0;
        double pPetSession = (1.0 - Math.pow(1.0 - petBase, sessionKills)) * 100.0;

        // ----------------------------------------------------
        // POPOLA SCHEDA 1: "ALL" -> EXPECTED
        // ----------------------------------------------------
        expectedPanel.add(new DropRowPanel("Any Unique", chanceAll, Color.ORANGE, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Full Helm", chanceAll * (2.0 / 12.0), Color.RED, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Platebody", chanceAll * (2.0 / 12.0), Color.RED, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Torva Platelegs", chanceAll * (2.0 / 12.0), Color.RED, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Zaryte Vambraces", chanceAll * (1.0 / 12.0), Color.RED, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Nihil Horn", chanceAll * (2.0 / 12.0), Color.ORANGE, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Ancient Hilt", chanceAll * (1.0 / 12.0), Color.MAGENTA, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) expectedPanel.add(new DropRowPanel("Nexling (Pet)", pPetAll, Color.CYAN, "nexling.png"));

        // POPOLA SCHEDA 1: "ALL" -> RECEIVED (CONTATORI)
        receivedPanel.add(new DropRowPanel("Any Unique", getSavedReceivedCount("any_unique"), Color.GRAY, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Full Helm", getSavedReceivedCount("torva_helm"), Color.GRAY, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Platebody", getSavedReceivedCount("torva_body"), Color.GRAY, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Torva Platelegs", getSavedReceivedCount("torva_legs"), Color.GRAY, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Zaryte Vambraces", getSavedReceivedCount("zaryte_vambs"), Color.GRAY, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Nihil Horn", getSavedReceivedCount("nihil_horn"), Color.GRAY, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Ancient Hilt", getSavedReceivedCount("ancient_hilt"), Color.GRAY, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) receivedPanel.add(new DropRowPanel("Nexling (Pet)", getSavedReceivedCount("nexling_pet"), Color.GRAY, "nexling.png"));

        // ----------------------------------------------------
        // POPOLA SCHEDA 2: "SESSION"
        // ----------------------------------------------------
        sessionExpectedPanel.add(new DropRowPanel("Any Unique", chanceSession, Color.ORANGE, "nex_icon.png"));
        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Full Helm", chanceSession * (2.0 / 12.0), Color.RED, "torva_helm.png"));
        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Platebody", chanceSession * (2.0 / 12.0), Color.RED, "torva_body.png"));
        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Torva Platelegs", chanceSession * (2.0 / 12.0), Color.RED, "torva_legs.png"));
        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Zaryte Vambraces", chanceSession * (1.0 / 12.0), Color.RED, "zaryte_vambraces.png"));
        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Nihil Horn", chanceSession * (2.0 / 12.0), Color.ORANGE, "nihil_horn.png"));
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Ancient Hilt", chanceSession * (1.0 / 12.0), Color.MAGENTA, "ancient_hilt.png"));
        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW) sessionExpectedPanel.add(new DropRowPanel("Nexling (Pet)", pPetSession, Color.CYAN, "nexling.png"));

        expectedPanel.revalidate();
        expectedPanel.repaint();
        receivedPanel.revalidate();
        receivedPanel.repaint();
        sessionExpectedPanel.revalidate();
        sessionExpectedPanel.repaint();
    }
    private int getSavedReceivedCount(String key)
    {
        Integer count = configManager.getConfiguration("nexcalculator", "received_" + key, Integer.class);
        return (count != null) ? count : 0;
    }

    private class DropRowPanel extends JPanel
    {
        private final double displayValue;
        private final boolean isPercentage;
        private final Color fillColor;
        private final BufferedImage itemIcon;
        private final String valueText;

        public DropRowPanel(String name, double value, Color barColor, String iconPath)
        {
            this.displayValue = value;
            this.isPercentage = (barColor != Color.GRAY);
            this.fillColor = barColor;

            if (isPercentage) {
                this.valueText = String.format("%.1f%%", value);
            } else {
                this.valueText = String.valueOf((int) value);
            }

            BufferedImage img = null;
            try {
                img = ImageUtil.loadImageResource(NexCalculatorPanel.class, iconPath);
            } catch (Exception e) {
                img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            }
            this.itemIcon = img;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(getPreferredSize().width, 36)); // Altezza capsula stile Delve
            setBorder(new EmptyBorder(4, 5, 4, 5));
            setBackground(new Color(30, 30, 30));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 1. ICONA ESTERNA SULLA SINISTRA
            if (itemIcon != null)
            {
                int iconY = (getHeight() - 18) / 2;
                g2d.drawImage(itemIcon, 2, iconY, 18, 18, null);
            }

            // 2. RETTANGOLO DELLA BARRA DI CALCOLO (Fisso dopo l'immagine)
            int barX = 26;
            int barY = 2;
            int barWidth = getWidth() - barX - 10;
            int barHeight = getHeight() - 4;

            // Sfondo interno scurissimo del box
            g2d.setColor(new Color(20, 20, 20));
            g2d.fillRect(barX, barY, barWidth, barHeight);

            // 3. SE LA % È MAGGIORE DI ZERO, COLORA L'AVANZAMENTO SOLIDO OPACO
            if (isPercentage && displayValue > 0)
            {
                int fillWidth = (int) ((barWidth * Math.min(100.0, displayValue)) / 100.0);

                // Crea una tonalità calda profonda (Marrone/Rosso cupo stile Delve)
                Color deepColor = new Color(
                        Math.max(0, fillColor.getRed() - 40),
                        Math.max(0, fillColor.getGreen() - 20),
                        Math.max(0, fillColor.getBlue() - 20)
                );
                g2d.setColor(deepColor);
                g2d.fillRect(barX, barY, fillWidth, barHeight);
            }

            // 4. CONTORNO GRIGIO SOTTILE DELLA MATTONELLA (Sempre visibile, anche a 0%!)
            g2d.setColor(new Color(90, 90, 90));
            g2d.drawRect(barX, barY, barWidth, barHeight);

            // 5. TESTO NUMERICO STAMPATO AL CENTRO DEL RETTANGOLO (Carattere Grande e Nitido)
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Dialog", Font.BOLD, 14));
            FontMetrics metrics = g2d.getFontMetrics();

            int textX = barX + (barWidth - metrics.stringWidth(valueText)) / 2;
            int textY = barY + (barHeight - metrics.getHeight()) / 2 + metrics.getAscent();

            g2d.drawString(valueText, textX, textY);
        }
    }
}
