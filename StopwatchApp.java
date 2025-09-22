// File: src/com/example/stopwatch/StopwatchApp.java
package com.example.stopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class StopwatchApp {
    private boolean running = false;
    private long startNano = 0L;
    private long elapsedNanoBefore = 0L; // accumulated before pause
    private Timer swingTimer;

    private JLabel timeLabel;
    private DefaultListModel<String> lapsModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StopwatchApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Stopwatch — Java Desktop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 600);
        frame.setLocationRelativeTo(null);

        // Root panel with gradient background
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 16, 58),
                        0, h, new Color(58, 10, 80));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }
        };
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Glass-like card
        JPanel glassCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // translucent rounded background
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
                g2.setColor(Color.white);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        glassCard.setOpaque(false);
        glassCard.setLayout(new BorderLayout(12, 12));

        // Time label
        timeLabel = new JLabel(formatMillis(0L), SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        timeLabel.setForeground(Color.white);
        timeLabel.setOpaque(false);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        JButton startPause = new JButton("Start");
        startPause.setFocusPainted(false);
        startPause.setPreferredSize(new Dimension(110, 40));
        startPause.addActionListener(e -> {
            if (!running) {
                start();
                startPause.setText("Pause");
            } else {
                pause();
                startPause.setText("Start");
            }
        });

        JButton reset = new JButton("Reset");
        reset.setPreferredSize(new Dimension(110, 40));
        reset.addActionListener(e -> {
            reset();
            startPause.setText("Start");
        });

        JButton lap = new JButton("Lap");
        lap.setPreferredSize(new Dimension(110, 40));
        lap.addActionListener(e -> recordLap());

        styleButton(startPause);
        styleButton(reset);
        styleButton(lap);

        buttons.add(startPause);
        buttons.add(lap);
        buttons.add(reset);

        // Laps area
        lapsModel = new DefaultListModel<>();
        JList<String> lapsList = new JList<>(lapsModel);
        JScrollPane lapScroll = new JScrollPane(lapsList);
        lapScroll.setPreferredSize(new Dimension(360, 220));
        lapScroll.setOpaque(false);
        lapScroll.getViewport().setOpaque(false);
        lapScroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Compose
        glassCard.add(timeLabel, BorderLayout.NORTH);
        glassCard.add(buttons, BorderLayout.CENTER);
        glassCard.add(lapScroll, BorderLayout.SOUTH);
        glassCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Shadow panel for 3D popout feeling
        JPanel shadowWrap = new JPanel(new BorderLayout());
        shadowWrap.setOpaque(false);
        shadowWrap.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        shadowWrap.add(glassCard, BorderLayout.CENTER);

        root.add(shadowWrap, BorderLayout.CENTER);
        frame.setContentPane(root);

        // Timer: update UI roughly every 25 ms
        swingTimer = new Timer(25, ae -> updateTimeLabel());
        frame.setVisible(true);
    }

    private void styleButton(JButton b) {
        b.setBackground(new Color(255, 255, 255, 200));
        b.setForeground(Color.black);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setFocusPainted(false);
    }

    private void start() {
        if (!running) {
            startNano = System.nanoTime();
            running = true;
            swingTimer.start();
        }
    }

    private void pause() {
        if (running) {
            long now = System.nanoTime();
            elapsedNanoBefore += (now - startNano);
            running = false;
            swingTimer.stop();
        }
    }

    private void reset() {
        running = false;
        startNano = 0L;
        elapsedNanoBefore = 0L;
        swingTimer.stop();
        timeLabel.setText(formatMillis(0L));
        lapsModel.clear();
    }

    private void recordLap() {
        long elapsedMs = getElapsedMillis();
        String s = String.format("Lap %d — %s", lapsModel.getSize() + 1, formatMillis(elapsedMs));
        lapsModel.addElement(s);
    }

    private void updateTimeLabel() {
        long elapsedMs = getElapsedMillis();
        timeLabel.setText(formatMillis(elapsedMs));
    }

    private long getElapsedMillis() {
        long totalNano = elapsedNanoBefore;
        if (running) {
            long now = System.nanoTime();
            totalNano += (now - startNano);
        }
        return totalNano / 1_000_000L;
    }

    private String formatMillis(long ms) {
        long hours = ms / (1000 * 60 * 60);
        long minutes = (ms / (1000 * 60)) % 60;
        long seconds = (ms / 1000) % 60;
        long centi = (ms % 1000) / 10; // centiseconds
        if (hours > 0) {
            return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centi);
        } else {
            return String.format("%02d:%02d.%02d", minutes, seconds, centi);
        }
    }
}
