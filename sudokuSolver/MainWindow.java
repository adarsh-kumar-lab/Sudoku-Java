
package sudokuvalidator;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window.
 * Contains three tabs:
 *   Tab 1 — GAME       (puzzle generator + solver + levels + DB)
 *   Tab 2 — VALIDATOR  (manual entry + validation + history)
 *   Tab 3 — LEADERBOARD (top scores from MySQL)
 *
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class MainWindow extends JFrame {

    private String playerName;
    private GamePanel gamePanel;

    public MainWindow(String playerName) {
        this.playerName = playerName;

        setTitle("Sudoku Game — Group 1 | Centurion University of Technology & Management");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBackground(new Color(10, 10, 18));

        // Confirm exit + close DB
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int opt = JOptionPane.showConfirmDialog(
                    MainWindow.this,
                    "Exit the game?",
                    "Exit", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    DatabaseConnection.close();
                    System.exit(0);
                }
            }
        });

        buildUI();
        pack();
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(10, 10, 18));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(14, 14, 28));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel appTitle = new JLabel("SUDOKU");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appTitle.setForeground(new Color(100, 160, 255));

        JLabel subtitle = new JLabel(
            "Puzzle Game + Solver + Validator  ·  "
            + "Group 1 | B.Tech CSE Sec-E | Centurion University");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(80, 80, 110));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setBackground(new Color(14,14,28));
        titleGroup.add(appTitle);
        titleGroup.add(subtitle);

        JLabel playerLabel = new JLabel("Player: " + playerName + "  ");
        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        playerLabel.setForeground(new Color(150, 180, 255));

        header.add(titleGroup,   BorderLayout.WEST);
        header.add(playerLabel,  BorderLayout.EAST);

        // ── Tabs ─────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(new Color(14, 14, 28));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gamePanel = new GamePanel(this, playerName);
        ValidatorPanel validatorPanel = new ValidatorPanel();

        tabs.addTab(" 🎮  GAME ",       gamePanel);
        tabs.addTab(" ✔   VALIDATOR ", validatorPanel);
        tabs.addTab(" 🏆  LEADERBOARD ", buildLeaderboardTab());

        // Tab colours
        tabs.setBackgroundAt(0, new Color(20, 40, 80));
        tabs.setBackgroundAt(1, new Color(20, 60, 30));
        tabs.setBackgroundAt(2, new Color(60, 50, 10));

        root.add(header, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildLeaderboardTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(14,14,22));
        p.setBorder(new EmptyBorder(20,20,20,20));

        JLabel info = new JLabel(
            "Top scores from all players — sorted by fastest time",
            SwingConstants.CENTER);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(new Color(100,100,140));
        info.setBorder(new EmptyBorder(0,0,14,0));

        JButton open = new JButton("🏆  Open Full Leaderboard");
        open.setFont(new Font("Segoe UI", Font.BOLD, 14));
        open.setBackground(new Color(22,22,38));
        open.setForeground(new Color(250,204,21));
        open.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(250,204,21), 1),
            new EmptyBorder(12,28,12,28)));
        open.setFocusPainted(false);
        open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        open.addActionListener(e -> new LeaderboardPanel().setVisible(true));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(14,14,22));
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(new Color(14,14,22));

        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
            "<html><div style='text-align:center;color:#6464a0;font-size:13px;'>"
            + "Play games to add your scores to the leaderboard.<br>"
            + "Top scores per difficulty are ranked by time taken.</div></html>");
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setBorder(new EmptyBorder(10,0,24,0));

        open.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(trophy);
        col.add(info);
        col.add(desc);
        col.add(open);
        center.add(col);
        p.add(center, BorderLayout.CENTER);
        return p;
    }
}
