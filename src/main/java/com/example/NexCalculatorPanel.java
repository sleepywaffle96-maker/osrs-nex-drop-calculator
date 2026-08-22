package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
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

    private final DropRowPanel anyDropRow = new DropRowPanel("Any Unique", Color.ORANGE);
    private final DropRowPanel helmRow = new DropRowPanel("Torva Full Helm", new Color(139, 0, 0));
    private final DropRowPanel bodyRow = new DropRowPanel("Torva Body", new Color(139, 0, 0));
    private final DropRowPanel legsRow = new DropRowPanel("Torva Legs", new Color(139, 0, 0));
    private final DropRowPanel vambsRow = new DropRowPanel("Zaryte Vambraces", Color.RED);
    private final DropRowPanel hornRow = new DropRowPanel("Nihil Horn", Color.GRAY);
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

        JPanel dropListPanel = new JPanel(new GridLayout(7, 1, 0, 10));
        dropListPanel.add(anyDropRow);
        dropListPanel.add(helmRow);
        dropListPanel.add(bodyRow);
        dropListPanel.add(legsRow);
        dropListPanel.add(vambsRow);
        dropListPanel.add(hornRow);
        dropListPanel.add(petRow);

        mainPanel.add(dropListPanel);
        add(mainPanel, BorderLayout.NORTH);
    }

    public void updateDisplay(int kc)
    {
        kcLabel.setText("Nex Kill Count: " + kc);
        if (kc <= 0) return;

        double dmgPercent = config.damagePercentage() / 100.0;
        double mvpBonus = config.isMvp() ? 1.10 : 1.0;

        double pAny = (1.0 / 43.0) * dmgPercent * mvpBonus;
        double pPiece = pAny * (1.0 / 5.0);
        double pPet = (1.0 / 500.0) * dmgPercent * mvpBonus;

        double chanceAny = 1.0 - Math.pow(1.0 - pAny, kc);
        double chancePiece = 1.0 - Math.pow(1.0 - pPiece, kc);
        double chancePet = 1.0 - Math.pow(1.0 - pPet, kc);

        anyDropRow.updateChance(chanceAny);
        helmRow.updateChance(chancePiece);
        bodyRow.updateChance(chancePiece);
        legsRow.updateChance(chancePiece);
        vambsRow.updateChance(chancePiece);
        hornRow.updateChance(chancePiece);
        petRow.updateChance(chancePet);
    }

    private static class DropRowPanel extends JPanel
    {
        private final JProgressBar progressBar;

        public DropRowPanel(String name, Color barColor)
        {
            setLayout(new BorderLayout());
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
