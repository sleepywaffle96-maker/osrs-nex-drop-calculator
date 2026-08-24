package com.example;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginTabs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import net.runelite.client.util.ImageUtil;

public class NexCalculatorPanel extends PluginPanel
{
    private final NexCalculatorConfig config;
    private final JLabel totalKillsLabel = new JLabel("Total Kills: 0");
    
    private final JPanel expectedPanel = new JPanel();
    private final JPanel receivedPanel = new JPanel();

    public NexCalculatorPanel(NexCalculatorConfig config)
    {
        this.config = config;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        // 1. HEADER CON IL CONTEGGIO DELLE UCCISIONI
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JLabel title = new JLabel("Nex Calculator", SwingConstants.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));
        
        totalKillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalKillsLabel.setForeground(Color.YELLOW);
        
        headerPanel.add(title);
        headerPanel.add(totalKillsLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CONFIGURAZIONE DELLE SCHEDE (TABS) STILE DELVE CALCULATOR
        PluginTabs tabs = new PluginTabs();
        JPanel mainTabContent = new JPanel();
        mainTabContent.setLayout(new BoxLayout(mainTabContent, BoxLayout.Y_AXIS));
        
        expectedPanel.setLayout(new BoxLayout(expectedPanel, BoxLayout.Y_AXIS));
        receivedPanel.setLayout(new BoxLayout(receivedPanel, BoxLayout.Y_AXIS));

        // Sezione Expected
        JPanel expectedGroup = new JPanel(new BorderLayout());
        JLabel expectedTitle = new JLabel("Expected");
        expectedTitle.setFont(new Font(expectedTitle.getFont().getName(), Font.BOLD, 13));
        expectedTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
        expectedGroup.add(expectedTitle, BorderLayout.NORTH);
        expectedGroup.add(expectedPanel, BorderLayout.CENTER);

        // Sezione Received
        JPanel receivedGroup = new JPanel(new BorderLayout());
        JLabel receivedTitle = new JLabel("Received");
        receivedTitle.setFont(new Font(receivedTitle.getFont().getName(), Font.BOLD, 13));
        receivedTitle.setBorder(new EmptyBorder(15, 0, 5, 0)); // Spazio sopra Received
        receivedGroup.add(receivedTitle, BorderLayout.NORTH);
        receivedGroup.add(receivedPanel, BorderLayout.CENTER);

        mainTabContent.add(expectedGroup);
        mainTabContent.add(receivedGroup);

        tabs.addTab("PI", mainTabContent);
        tabs.addTab("Session", new JPanel());
        tabs.addTab("Historical", new JPanel());
        
        add(tabs, BorderLayout.CENTER);
    }

    public void updateDisplay(int currentKills)
    {
        updateDisplayWithLiveDamage(currentKills, config.damagePercentage());
    }

    public void updateDisplayWithLiveDamage(int currentKills, int liveDamagePercent)
    {
        totalKillsLabel.setText("Total Kills: " + currentKills);

        expectedPanel.removeAll();
        receivedPanel.removeAll();

        double damagePercent = liveDamagePercent / 100.0;
        int team = config.teamSize();

        // Formule matematiche per le probabilità di Nex
        double pHelm = (1.0 / 43.0) * (2.0 / 12.0) * damagePercent * currentKills * 100.0;
        double pBody = (1.0 / 43.0) * (2.0 / 12.0) * damagePercent * currentKills * 100.0;
        double pLegs = (1.0 / 43.0) * (2.0 / 12.0) * damagePercent * currentKills * 100.0;
        double pBow = (1.0 / 43.0) * (1.0 / 12.0) * damagePercent * currentKills * 100.0;

        // Inserimento condizionale con barre grandi e icone a lato (Expected vs Received)
        if (config.showTorva() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            expectedPanel.add(new DropRowPanel("Torva Full Helm", pHelm, Color.RED, "torva_helm.png"));
            expectedPanel.add(new DropRowPanel("Torva Platebody", pBody, Color.RED, "torva_body.png"));
            expectedPanel.add(new DropRowPanel("Torva Platelegs", pLegs, Color.RED, "torva_legs.png"));
            
            receivedPanel.add(new DropRowPanel("Torva Pieces", 0.0, Color.GRAY, "torva_body.png"));
        }

        if (config.showWeapon() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            expectedPanel.add(new DropRowPanel("Zaryte Crossbow", pBow, Color.ORANGE, "zaryte_crossbow.png"));
            receivedPanel.add(new DropRowPanel("Zaryte Crossbow", 0.0, Color.GRAY, "zaryte_crossbow.png"));
        }

        expectedPanel.revalidate();
        expectedPanel.repaint();
        receivedPanel.revalidate();
        receivedPanel.repaint();
    }

    // CLASSE PERSONALIZZATA PER RENDERE LE BARRE GRANDI CON ICONE COME IN DELVE
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
            this.textToShow = String.format("%.1f%%", chance);
            
            // Carica in modo sicuro l'iconcina del drop
            BufferedImage img = null;
            try {
                img = ImageUtil.loadImageResource(NexCalculatorPanel.class, iconPath);
            } catch (Exception e) {
                img = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB); // Sostituto vuoto in caso di errore
            }
            this.itemIcon = img;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(getPreferredSize().width, 28)); // Rende le barre più grandi/spesse
            setBorder(new EmptyBorder(2, 5, 2, 5));
            setBackground(new Color(40, 40, 40)); // Colore di sfondo della barra vuota
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. DISEGNA LA BARRA DI PROGRESSO SULLO SFONDO
            int fillWidth = (int) ((getWidth() * percentage) / 100.0);
            g2d.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 60)); // Sfondo semitrasparente riempito
            g2d.fillRect(0, 0, fillWidth, getHeight());
            
            g2d.setColor(fillColor);
            g2d.fillRect(0, getHeight() - 2, fillWidth, 2); // Linea lucida inferiore come accento grafico

            // 2. DISEGNA L'ICONA DEL DROP SULLA SINISTRA
            if (itemIcon != null)
            {
                int iconY = (getHeight() - 18) / 2;
                g2d.drawImage(itemIcon, 5, iconY, 18, 18, null);
            }

            // 3. DISEGNA LA PERCENTUALE SULLA DESTRA
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(getFont().getName(), Font.PLAIN, 12));
            FontMetrics metrics = g2d.getFontMetrics();
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(textToShow, getWidth() - metrics.stringWidth(textToShow) - 8, textY);
        }
    }
}
