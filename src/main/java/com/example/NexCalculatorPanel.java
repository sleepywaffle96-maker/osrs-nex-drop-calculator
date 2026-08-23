package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class NexCalculatorPanel extends PluginPanel
{
    private final NexCalculatorConfig config;
    private final JPanel mainPanel = new JPanel();
    private final JLabel kcLabel = new JLabel("Nex Kill Count: 0");
    private final JPanel dropListPanel = new JPanel();

    private final DropRowPanel anyDropRow = new DropRowPanel("Any Unique", Color.ORANGE);
    private final DropRowPanel helmRow = new DropRowPanel("Torva Full Helm", new Color(139, 0, 0));
    private final DropRowPanel bodyRow = new DropRowPanel("Torva Body", new Color(139, 0, 0));
    private final DropRowPanel legsRow = new DropRowPanel("Torva Legs", new Color(139, 0, 0));
    private final DropRowPanel vambsRow = new DropRowPanel("Zaryte Vambraces", Color.RED);
    private final DropRowPanel hornRow = new DropRowPanel("Nihil Horn", Color.GRAY);
    private final DropRowPanel hiltRow = new DropRowPanel("Ancient Hilt", new Color(184, 134, 11));
    private final DropRowPanel petRow = new DropRowPanel("Nexling (Pet)", Color.CYAN);

    public NexCalculatorPanel(NexCalculatorConfig config)
    {
        super();
        this.config = config;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Nex Drop Calculator");
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(title);

        kcLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        kcLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(kcLabel);

        dropListPanel.setLayout(new BoxLayout(dropListPanel, BoxLayout.Y_AXIS));
        mainPanel.add(dropListPanel);

        add(mainPanel, BorderLayout.NORTH);
    }

    public void updateDisplay(int currentKills)
    {
        updateDisplayWithLiveDamage(currentKills, config.damagePercentage());
    }

    public void updateDisplayWithLiveDamage(int currentKills, int liveDamagePercent)
    {
        kcLabel.setText("Nex Kill Count: " + currentKills);

        double damagePercent = liveDamagePercent / 100.0;

        double pTotalUniques = (1.0 / 43.0) * damagePercent * currentKills;
        double pPiece = (1.0 / 43.0) * (2.0 / 12.0) * damagePercent * currentKills;
        double pVambs = (1.0 / 43.0) * (1.0 / 12.0) * damagePercent * currentKills;
        double pPet = (1.0 / 500.0) * damagePercent * currentKills;

        anyDropRow.updateChance(pTotalUniques);
        helmRow.updateChance(pPiece);
        bodyRow.updateChance(pPiece);
        legsRow.updateChance(pPiece);
        vambsRow.updateChance(pVambs);
        hornRow.updateChance(pPiece);
        hiltRow.updateChance(pPiece);
        petRow.updateChance(pPet);

        dropListPanel.removeAll();
        dropListPanel.add(anyDropRow);

        if (config.showHelm() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(helmRow);
        }

        if (config.showBody() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(bodyRow);
        }

        if (config.showLegs() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(legsRow);
        }

        if (config.showVambs() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(vambsRow);
        }

        if (config.showHorn() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(hornRow);
        }

        if (config.showHilt() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(hiltRow);
        }

        if (config.showPet() == NexCalculatorConfig.DisplayMode.SHOW)
        {
            dropListPanel.add(petRow);
        }

        dropListPanel.revalidate();
        dropListPanel.repaint();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private static class DropRowPanel extends JPanel
    {
        private final JProgressBar progressBar;

        public DropRowPanel(String name, Color barColor)
        {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 0, 10, 0));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

            progressBar = new JProgressBar(0, 1000);
            progressBar.setStringPainted(true);
            progressBar.setForeground(barColor);
            progressBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            add(nameLabel, BorderLayout.NORTH);
            add(progressBar, BorderLayout.CENTER);
        }

        public void updateChance(double chance)
        {
            progressBar.setValue((int)(chance * 1000));
            progressBar.setString(String.format("%.1f%%", chance * 100.0));
        }
    }
}
