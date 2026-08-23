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

    public void updateDisplay(int currentKills)
    {
        updateDisplayWithLiveDamage(currentKills, config.damagePercentage());
    }

    public void updateDisplayWithLiveDamage(int currentKills, int liveDamagePercent)
    {
        // Corretto il nome del componente per i Kills totali
        kcLabel.setText("Nex Kill Count: " + currentKills);

        // Trasformiamo la percentuale live in decimali per la formula matematica
        double damagePercent = liveDamagePercent / 100.0;

        // Formule matematiche per calcolare le probabilità (espresse in decimali)
        double pTotalUniques = (1.0 / 43.0) * damagePercent * currentKills;
        double pPiece = (1.0 / 43.0) * (2.0 / 12.0) * damagePercent * currentKills; // Torva & Horn
        double pVambs = (1.0 / 43.0) * (1.0 / 12.0) * damagePercent * currentKills; // Vambraces
        double pPet = (1.0 / 500.0) * damagePercent * currentKills;                 // Nexling

        // Aggiorna direttamente le righe esistenti usando la funzione di DropRowPanel
        anyDropRow.updateChance(pTotalUniques);
        helmRow.updateChance(pPiece);
        bodyRow.updateChance(pPiece);
        legsRow.updateChance(pPiece);
        vambsRow.updateChance(pVambs);
        hornRow.updateChance(pPiece);
        petRow.updateChance(pPet);

        mainPanel.revalidate();
        mainPanel.repaint();
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

