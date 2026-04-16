
package sudokuvalidator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Leaderboard Panel — shows top players from MySQL.
 * Filterable by difficulty.
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class LeaderboardPanel extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public LeaderboardPanel() {
        setTitle("🏆 Leaderboard — Sudoku Game");
        setSize(700, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(14, 14, 22));
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        loadData("ALL");
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(14,14,22));
        p.setBorder(new EmptyBorder(14,16,6,16));

        JLabel title = new JLabel("🏆  Leaderboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(250,204,21));
        p.add(title, BorderLayout.WEST);

        // Filter buttons
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filters.setBackground(new Color(14,14,22));
        String[] labels = {"ALL","EASY","MEDIUM","HARD","EXPERT"};
        Color[]  colors = {new Color(180,180,255),new Color(74,222,128),
                           new Color(250,204,21),new Color(251,146,60),
                           new Color(248,113,113)};
        for (int i = 0; i < labels.length; i++) {
            final String lbl = labels[i];
            JButton b = new JButton(lbl);
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setBackground(new Color(22,22,38));
            b.setForeground(colors[i]);
            b.setBorder(BorderFactory.createLineBorder(colors[i], 1));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> loadData(lbl));
            filters.add(b);
        }
        p.add(filters, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Rank","Player","Difficulty","Time","Mistakes","Hints","Date"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setBackground(new Color(18,18,28));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(35,35,55));
        table.getTableHeader().setBackground(new Color(28,28,48));
        table.getTableHeader().setForeground(new Color(250,204,21));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(80);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(70);
        table.getColumnModel().getColumn(5).setMaxWidth(60);

        // Colour top 3
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            Color[] rankColors = {new Color(255,215,0),new Color(192,192,192),new Color(205,127,50)};
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setBackground(sel ? new Color(50,80,150) : new Color(18,18,28));
                setForeground(row < 3 ? rankColors[row] : Color.WHITE);
                setBorder(new EmptyBorder(3,10,3,10));
                if (col == 0 && row == 0) setText("🥇");
                if (col == 0 && row == 1) setText("🥈");
                if (col == 0 && row == 2) setText("🥉");
                return this;
            }
        });

        return new JScrollPane(table);
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(new Color(14,14,22));
        JButton btn = new JButton("Close");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(28,28,48));
        btn.setForeground(new Color(150,180,255));
        btn.setBorder(new EmptyBorder(8,20,8,20));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> dispose());
        p.add(btn);
        return p;
    }

    private void loadData(String diff) {
        model.setRowCount(0);
        ResultSet rs = DatabaseConnection.getLeaderboard(diff);
        if (rs == null) return;
        int rank = 1;
        try {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rank++,
                    rs.getString("player_name"),
                    rs.getString("difficulty").toUpperCase(),
                    SudokuEngine.formatTime(rs.getInt("time_taken")),
                    rs.getInt("mistakes"),
                    rs.getInt("hints_used"),
                    rs.getString("played_at").substring(0,16)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
