package gui;

import game.CardGame;
import game.StorageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

/**
 * Created by Andrew on 28/12/13.
 */
public class CardFrame implements ActionListener, WindowListener {
    private JFrame frame;
    private CardPanel panel;
    private CardGame game;
    private Object lock = new Object();
    private String howToPlay = "TODO";

    public CardFrame() {
        frame = new JFrame("Tom");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(this);

        JMenuBar menuBar = new JMenuBar();
        frame.add(menuBar, BorderLayout.PAGE_START);
        JMenu jMenu = new JMenu("Menu");
        menuBar.add(jMenu);

        JMenuItem restartMenuItem = new JMenuItem("Restart");
        jMenu.add(restartMenuItem);
        restartMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, InputEvent.CTRL_MASK));
        restartMenuItem.addActionListener(this);

        JMenuItem helpMenuItem = new JMenuItem("Help");
        jMenu.add(helpMenuItem);
        helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, InputEvent.CTRL_MASK));
        helpMenuItem.addActionListener(this);

        JMenuItem undoMenuItem = new JMenuItem("Undo");
        jMenu.add(undoMenuItem);
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        undoMenuItem.addActionListener(this);

        JMenuItem statMenuItem = new JMenuItem("Stats");
        jMenu.add(statMenuItem);
        statMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        statMenuItem.addActionListener(this);

    }

    public static void main(String args[]) throws InvocationTargetException, InterruptedException {
        new Thread() {
            public void run() {
                CardFrame frame = new CardFrame();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                frame.restartGame();
            }
        }.start();
//        final CardFrame[] frame = new CardFrame[1];
//
//
//        frame[0].startGame();
    }

    public void restartGame() {
        //Make a new thread to ensure we don't run it on the EDT

        new Thread() {
            public void run() {
                if (panel != null) {
                    panel.removeComponentListener(panel);
                    panel.removeMouseMotionListener(panel);
                    panel.removeMouseListener(panel);
                    frame.remove(panel);
                }

                game = new CardGame(CardFrame.this);
                panel = new CardPanel(game);

                frame.add(panel, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                //  frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                startGame();
            }

        }.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Restart")) {
            StorageManager.loss();
            restartGame();
        } else if (command.equals("Help")) {
            showHelp();
        } else if (command.equals("Undo")) {
            synchronized (lock) {
                game.undo();
                panel.repaint();
            }
        } else if (command.equals("Stats")) {
            showStats();
        }
    }

    public static void showStats() {
        final JFrame frame = new JFrame("Stats");
        frame.setLayout(new BorderLayout());

        final JPanel textPanel = createStatText();
        frame.add(textPanel);
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StorageManager.reset();
                frame.remove(textPanel);
                frame.add(createStatText());
                frame.pack();
            }
        });
        frame.add(reset,BorderLayout.PAGE_END);
        frame.add(textPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createStatText() {
        JPanel panel = new JPanel();
        StringBuilder sb = new StringBuilder();
        sb.append("Wins:" + StorageManager.getWins() + " ");
        sb.append("Losses: " + StorageManager.getLosses() + " ");
        double ratio = StorageManager.getRatio() * 100;
        sb.append("W/L Ratio: " + new DecimalFormat("#.##").format(ratio) + " %" + "\n\n");
        int bestTime = StorageManager.getBestTime();
        sb.append("Best Time: " + (bestTime == Integer.MAX_VALUE ? "N/A" : bestTime / 1000 + " s") + "\n\n");
        int lowestMoves = StorageManager.getLowestMoves();
        sb.append("Lowest # Moves: " + (lowestMoves == Integer.MAX_VALUE ? "N/A" : lowestMoves + " moves"));
        JTextArea textArea = new JTextArea(sb.toString(), 5, 30);
        textArea.setEditable(false);
        panel.add(textArea);
        return panel;
    }

    private void showHelp() {
        JFrame frame = new JFrame("How to Play");
        JPanel panel = new JPanel();
        JTextArea textArea = new JTextArea(howToPlay, 20, 50);
        textArea.setEditable(false);
        panel.add(textArea);
        // panel.setPreferredSize(new Dimension(300, 500));

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void startGame() {
        game.dealGame(panel);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        StorageManager.loss();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
