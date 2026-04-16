
package sudokuvalidator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * History Panel — shows validation history from MySQL.
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class HistoryPanel extends JFrame {

    public HistoryPanel() {
        setTitle("Validation History — Sudoku App");
        setSize(800, 480);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(14, 14, 22));
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel title = new JLabel("  Validation History  (from MySQL)",
                                   SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(150, 180, 255));
        title.setBorder(new EmptyBorder(14, 16, 6, 0));
        title.setOpaque(true);
        title.setBackground(new Color(14, 14, 22));
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Date & Time", "Result", "Message"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ResultSet rs = DatabaseConnection.getValidationHistory();
        if (rs != null) {
            try {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("validated_at"),
                        rs.getString("result"),
                        rs.getString("result_message")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        JTable table = buildTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setMaxWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);

        // Colour rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                String res = (String) t.getValueAt(row, 2);
                setBackground(sel ? new Color(50,80,150)
                    : "Valid".equals(res) ? new Color(20,50,30)
                    : new Color(50,20,20));
                setForeground("Valid".equals(res)
                    ? new Color(100,230,120) : new Color(255,100,100));
                setBorder(new EmptyBorder(3,8,3,8));
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(makeCloseBtn(), BorderLayout.SOUTH);
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(new Color(18, 18, 28));
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setGridColor(new Color(35, 35, 55));
        t.getTableHeader().setBackground(new Color(28, 28, 48));
        t.getTableHeader().setForeground(new Color(150, 180, 255));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        return t;
    }

    private JPanel makeCloseBtn() {
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
}
