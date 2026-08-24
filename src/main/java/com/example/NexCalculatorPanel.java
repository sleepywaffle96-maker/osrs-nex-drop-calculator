package com.example;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginTabs;
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
    
    // Contenitori fisici per separare le due schermate
    private final JPanel expectedPanel = new JPanel();
    private final JPanel receivedPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsContainer = new JPanel(cardLayout);

    // I due bottoni selettori centrali affiancati come in Delve
    private final JButton expectedBtn = new JButton("Expected");
    private final JButton receivedBtn = new JButton("Received");

    private int lastKills = 0;
    private int lastLiveDamage = 0;

    public NexCalculatorPanel(NexCalculatorConfig config, ConfigManager configManager)
    {
        this.config = config;
        this.configManager = configManager;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        // HEADER IN CIMA CON IL CONTEGGIO KILL
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JLabel title = new JLabel("Nex Calculator", SwingConstants.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));
        
        totalKillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalKillsLabel.setForeground(Color.YELLOW);
        
        headerPanel.add(title);
        headerPanel.add(totalKillsLabel);
        add(headerPanel, BorderLayout.NORTH);

        // LE SCHEDE SUPERIORI GENERALI (TABS)
        PluginTabs tabs = new PluginTabs();
        JPanel mainTabContent = new JPanel(new BorderLayout());
        
        // IL SOTTOMENU CON I DUE PULSANTI AFFIANCATI
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

        // Azioni al clic per cambiare istantaneamente la lista visualizzata
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

        mainTabContent.add(subMenuPanel, BorderLayout.NORTH);
        mainTabContent.add(cardsContainer, BorderLayout.CENTER);

        tabs.addTab("All", mainTabContent);
        tabs.addTab("Session", new JPanel());
        tabs.addTab("Manual", new JPanel());
        
        add(tabs, BorderLayout.CENTER);
    }

    private void setButtonSelected(JButton button, boolean selected)
    {
        if (selected) {
            button.setBackground(new Color(60, 60, 60));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(35, 35, 35));
            button.setForeground(Color.GRAY);
        }
    }
    public void updateDisplayWithLiveDamage(int currentKills, int liveDamagePercent)
    {
        this.lastKills = currentKills;
        this.lastLiveDamage = liveDamagePercent;

        totalKillsLabel.setText("Total Kills: " + currentKills);

        expectedPanel.removeAll();
        receivedPanel.removeAll();

        double totalDamagePercentageAccumulated = 0.0;
        double baseChanceSum = 0.0;

        // CRONOLOGIA AVANZATA: Somma le probabilità reali e applica il bonus MVP per ogni kill passata
        for (int i = 1; i <= currentKills; i++)
        {
            // 1. Recupera il danno salvato sul PC
            Integer savedDamage = configManager.getConfiguration("nexcalculator", "kill_damage_" + i, Integer.class);
            double damageFraction = (savedDamage != null) ? (savedDamage / 100.0) : (config.damagePercentage() / 100.0);
            
            // 2. Recupera lo stato MVP salvato sul PC
            Boolean savedMvp = configManager.getConfiguration("nexcalculator", "kill_mvp_" + i, Boolean.class);
            boolean wasMvp = (savedMvp != null) && savedMvp;

            // La chance base di Nex è 1/43. Se sei MVP, ottieni il bonus moltiplicatore del 10% (1.10)
            double currentBaseChance = wasMvp ? (1.0 / 43.0) * 1.10 : (1.0 / 43.0);
            
            // Accumula il contributo di probabilità reale di questo scontro specifico
            baseChanceSum += currentBaseChance * damageFraction;
        }

        // Aggiunge il danno parziale in tempo reale se la battaglia è in corso adesso
        if (liveDamagePercent > 0)
        {
            double liveDamageFraction = liveDamagePercent / 100.0;
            baseChanceSum += (1.0 / 43.0) * liveDamageFraction;
        }

        // Calcolo statistico composto e preciso al millimetro basato sull'intera storia MVP e Danni
        double overallUniqueChance = (1.0 - Math.exp(-baseChanceSum)) * 100.0;

        // Suddivisione esatta dei 12 slot della tabella dei drop di Nex
        double pAny = overallUniqueChance;
        double pHelm = overallUniqueChance * (2.0 / 12.0);
        double pBody = overallUniqueChance * (2.0 / 12.0);
        double pLegs = overallUniqueChance * (2.0 / 12.0);
        double pVambs = overallUniqueChance * (1.0 / 12.0);
        double pHorn = overallUniqueChance * (2.0 / 12.0);
        double pHilt = overallUniqueChance * (1.0 / 12.0);
        
        // Calcolo specifico per il Pet Nexling (1 su 500 fisso e cumulativo per uccisione)
        double petBase = 1.0 / 500.0;
        double pPet = (1.0 - Math.pow(1.0 - petBase, currentKills)) * 100.0;

        // SEZIONE "EXPECTED" (BARRE GRANDI COLORATE E ICONE)
        expectedPanel.add(new DropRowPanel("Any Unique", pAny, Color.ORANGE, "nex_icon.png"));
        
        if (config.showTorva() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            expectedPanel.add(new DropRowPanel("Torva Full Helm", pHelm, Color.RED, "torva_helm.png"));
            expectedPanel.add(new DropRowPanel("Torva Platebody", pBody, Color.RED, "torva_body.png"));
            expectedPanel.add(new DropRowPanel("Torva Platelegs", pLegs, Color.RED, "torva_legs.png"));
        }

        if (config.showWeapon() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            expectedPanel.add(new DropRowPanel("Zaryte Vambraces", pVambs, Color.RED, "zaryte_vambraces.png"));
            expectedPanel.add(new DropRowPanel("Nihil Horn", pHorn, Color.ORANGE, "nihil_horn.png"));
        }

        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            expectedPanel.add(new DropRowPanel("Ancient Hilt", pHilt, Color.MAGENTA, "ancient_hilt.png"));
        }
        
        expectedPanel.add(new DropRowPanel("Nexling (Pet)", pPet, Color.CYAN, "nexling.png"));

        // SEZIONE "RECEIVED" (CONTATORI DROPS OTTENUTI)
        receivedPanel.add(new DropRowPanel("Any Unique Received", 0.0, Color.GRAY, "nex_icon.png"));
        if (config.showTorva() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            receivedPanel.add(new DropRowPanel("Torva Pieces", 0.0, Color.GRAY, "torva_body.png"));
        }
        if (config.showWeapon() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            receivedPanel.add(new DropRowPanel("Weapons", 0.0, Color.GRAY, "zaryte_crossbow.png"));
        }
        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            receivedPanel.add(new DropRowPanel("Hilts", 0.0, Color.GRAY, "ancient_hilt.png"));
        }
        receivedPanel.add(new DropRowPanel("Nexling", 0.0, Color.GRAY, "nexling.png"));

        expectedPanel.revalidate();
        expectedPanel.repaint();
        receivedPanel.revalidate();
        receivedPanel.repaint();
    }

    // CLASSE INTERNA: DISEGNA LE BARRE SPESSE CON L'ICONA INCASTONATA A SINISTRA STILE DELVE
    private class DropRowPanel extends JPanel
    {
        private final double percentage;
        private final Color fillColor;
        private final BufferedImage itemIcon;
        private final String textToShow;

        public DropRowPanel(String name, double chance, Color barColor, String iconPath)
        {
            this.percentage = Math.min(100.0, Math.max(0.0, chance));
            this.fillColor = barColor;
            this.textToShow = name + ": " + String.format("%.2f%%", chance);
            
            BufferedImage img = null;
            try {
                img = ImageUtil.loadImageResource(NexCalculatorPanel.class, iconPath);
            } catch (Exception e) {
                img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            }
            this.itemIcon = img;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(getPreferredSize().width, 28)); // Altezza barra spessa
            setBorder(new EmptyBorder(2, 5, 2, 5));
            setBackground(new Color(40, 40, 40));
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Disegna il riempimento semitrasparente
            int fillWidth = (int) ((getWidth() * percentage) / 100.0);
            g2d.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 65));
            g2d.fillRect(0, 0, fillWidth, getHeight());
            
            // 2. Linea di accento inferiore lucida
            g2d.setColor(fillColor);
            g2d.fillRect(0, getHeight() - 2, fillWidth, 2);

            // 3. Disegna l'immagine dell'oggetto a sinistra
            if (itemIcon != null)
            {
                int iconY = (getHeight() - 18) / 2;
                g2d.drawImage(itemIcon, 5, iconY, 18, 18, null);
            }

            // 4. Disegna il testo spostato a destra per non coprire l'icona
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(getFont().getName(), Font.PLAIN, 11));
            FontMetrics metrics = g2d.getFontMetrics();
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(textToShow, 28, textY);
        }
    }
}
