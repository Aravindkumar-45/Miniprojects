import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class SupermarketSwingWithMySQL {

    // ---------- DB Config (update if needed) ----------
    private static final String DB_URL = "jdbc:mysql://localhost:3306/supermarket";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "aravind@161145";

    // ---------- Theme (Theme B: Dark Grey + Blue) ----------
    private static final Color BG_DARK = new Color(34, 40, 49);        // main dark bg
    private static final Color PANEL_LIGHT = new Color(44, 51, 63);     // panel
    private static final Color ACCENT_BLUE = new Color(0, 123, 255);    // primary accent
    private static final Color BTN_BLUE = new Color(10, 96, 210);
    private static final Color TEXT_ON_DARK = Color.WHITE;
    private static final Color MUTED = new Color(170, 170, 170);

    // ---------- Runtime state ----------
    private JFrame frame;
    private String loggedInUser = "";
    private String loggedInRole = "";

    // Login fields (shared UI)
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JComboBox<String> loginRoleBox;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // optional: set a nicer look and feel but keep Swing controls as-is
                UIManager.put("Button.focus", BTN_BLUE);
            } catch (Exception ignored) {}
            new SupermarketSwingWithMySQL().showLoginPage();
        });
    }

    // ---------------- Login Page ----------------
    private void showLoginPage() {
        frame = new JFrame("Supermarket - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 560);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(null);

        // Title
        JLabel title = new JLabel("Supermarket Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ACCENT_BLUE);
        title.setBounds(60, 30, 400, 36);
        frame.add(title);

        // Card-like panel for inputs
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(PANEL_LIGHT);
        card.setBounds(40, 90, 420, 360);
        card.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        frame.add(card);

        JLabel userLbl = new JLabel("User ID:");
        userLbl.setForeground(TEXT_ON_DARK);
        userLbl.setBounds(30, 30, 100, 24);
        card.add(userLbl);

        loginUserField = new JTextField();
        loginUserField.setBounds(140, 30, 240, 28);
        card.add(loginUserField);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(TEXT_ON_DARK);
        passLbl.setBounds(30, 74, 100, 24);
        card.add(passLbl);

        loginPassField = new JPasswordField();
        loginPassField.setBounds(140, 74, 240, 28);
        card.add(loginPassField);

        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setForeground(TEXT_ON_DARK);
        roleLbl.setBounds(30, 118, 100, 24);
        card.add(roleLbl);

        loginRoleBox = new JComboBox<>(new String[]{"Admin", "Cashier", "User"});
        loginRoleBox.setBounds(140, 118, 240, 28);
        card.add(loginRoleBox);

        JButton loginBtn = new JButton("Login");
        stylePrimaryButton(loginBtn);
        loginBtn.setBounds(60, 180, 120, 36);
        card.add(loginBtn);

        JButton newUserBtn = new JButton("New User");
        stylePrimaryButton(newUserBtn);
        newUserBtn.setBounds(240, 180, 140, 36);
        card.add(newUserBtn);

        JLabel status = new JLabel("", SwingConstants.CENTER);
        status.setForeground(MUTED);
        status.setBounds(20, 240, 380, 24);
        card.add(status);

        loginBtn.addActionListener(e -> attemptLogin(status));
        newUserBtn.addActionListener(e -> showNewUserDialog());

        frame.setVisible(true);
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(BTN_BLUE);
        b.setForeground(TEXT_ON_DARK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
    }

    private void attemptLogin(JLabel statusLabel) {
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword()).trim();
        String role = (String) loginRoleBox.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }

        String sql = "SELECT 1 FROM users WHERE user_id=? AND password=? AND role=?";
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loggedInUser = user;
                    loggedInRole = role;
                    frame.dispose(); // close login
                    showDashboard(); // open dashboard
                } else {
                    statusLabel.setText("Invalid credentials or role.");
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("DB Error: " + e.getMessage());
        }
    }

    // ---------------- New User (dialog) ----------------
    private void showNewUserDialog() {
        JDialog dlg = new JDialog(frame, "Create New User", true);
        dlg.setSize(460, 360);
        dlg.setLocationRelativeTo(frame);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(PANEL_LIGHT);

        JLabel title = new JLabel("Create New User", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT_BLUE);
        title.setBounds(80, 10, 300, 28);
        dlg.add(title);

        JLabel userLbl = new JLabel("User ID:");
        userLbl.setBounds(40, 70, 100, 24);
        userLbl.setForeground(TEXT_ON_DARK);
        dlg.add(userLbl);
        JTextField uid = new JTextField();
        uid.setBounds(150, 70, 240, 26);
        dlg.add(uid);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setBounds(40, 110, 100, 24);
        passLbl.setForeground(TEXT_ON_DARK);
        dlg.add(passLbl);
        JPasswordField pwd = new JPasswordField();
        pwd.setBounds(150, 110, 240, 26);
        dlg.add(pwd);

        JLabel roleLbl = new JLabel("Role:");
        roleLbl.setBounds(40, 150, 100, 24);
        roleLbl.setForeground(TEXT_ON_DARK);
        dlg.add(roleLbl);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Cashier", "User"});
        roleBox.setBounds(150, 150, 240, 26);
        dlg.add(roleBox);

        JButton create = new JButton("Create");
        stylePrimaryButton(create);
        create.setBounds(150, 200, 160, 36);
        dlg.add(create);

        JLabel status = new JLabel("", SwingConstants.CENTER);
        status.setBounds(40, 250, 360, 24);
        status.setForeground(MUTED);
        dlg.add(status);

        create.addActionListener(e -> {
            String u = uid.getText().trim();
            String p = new String(pwd.getPassword()).trim();
            String r = (String) roleBox.getSelectedItem();
            if (u.isEmpty() || p.isEmpty()) {
                status.setText("Fill all fields.");
                return;
            }
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement("INSERT INTO users(user_id, password, role) VALUES (?,?,?)")) {
                ps.setString(1, u);
                ps.setString(2, p);
                ps.setString(3, r);
                ps.executeUpdate();
                status.setForeground(new Color(0, 200, 0));
                status.setText("User created. Close this window and login.");
            } catch (SQLException ex) {
                status.setForeground(Color.RED);
                status.setText("Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // ---------------- Dashboard ----------------
    private void showDashboard() {
        JFrame dash = new JFrame("Supermarket - Dashboard (" + loggedInRole + ")");
        dash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dash.setSize(1000, 640);
        dash.setLocationRelativeTo(null);
        dash.setLayout(new BorderLayout());
        dash.getContentPane().setBackground(BG_DARK);

        // Left menu container (vertical)
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(240, 0));
        left.setBackground(new Color(28, 33, 41));
        left.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        JLabel logo = new JLabel("<html><div style='text-align:center;color:#FFFFFF;font-weight:bold;'>SUPERMARKET</div></html>", SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(220, 48));
        logo.setForeground(TEXT_ON_DARK);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setBorder(BorderFactory.createEmptyBorder(8,0,20,0));
        left.add(logo);

        JButton addProductBtn = styledMenuButton("Add Product");
        JButton viewProductsBtn = styledMenuButton("View Products");
        JButton createBillBtn = styledMenuButton("Create Bill");
        JButton refreshBtn = styledMenuButton("Refresh Dashboard");
        JButton logoutBtn = styledDangerButton("Logout");

        left.add(addProductBtn);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(viewProductsBtn);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(createBillBtn);
        left.add(Box.createRigidArea(new Dimension(0, 12)));
        left.add(refreshBtn);
        left.add(Box.createVerticalGlue());
        left.add(logoutBtn);

        dash.add(left, BorderLayout.WEST);

        // center panel
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.setBackground(BG_DARK);

        JLabel welcome = new JLabel("Welcome, " + loggedInUser + " (" + loggedInRole + ")");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(TEXT_ON_DARK);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(welcome);
        center.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 16));
        cards.setBackground(BG_DARK);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JPanel totalProductsCard = infoCard("Total Products", "0");
        JPanel todaysSalesCard = infoCard("Today's Sales (₹)", "0.00");
        JPanel todaysCustomersCard = infoCard("Today's Customers", "0");

        cards.add(totalProductsCard);
        cards.add(todaysSalesCard);
        cards.add(todaysCustomersCard);

        center.add(cards);
        center.add(Box.createVerticalGlue());

        dash.add(center, BorderLayout.CENTER);

        // wire actions
        addProductBtn.addActionListener(e -> showAddProductWindow());
        // viewProductsBtn: pass editable true only for admin
        viewProductsBtn.addActionListener(e -> showProductListWindow("Admin".equalsIgnoreCase(loggedInRole)));
        createBillBtn.addActionListener(e -> showCreateBillWindow());
        refreshBtn.addActionListener(e -> refreshDashboardValues(totalProductsCard, todaysSalesCard, todaysCustomersCard));
        logoutBtn.addActionListener(e -> {
            dash.dispose();
            loggedInUser = "";
            loggedInRole = "";
            showLoginPage();
        });

        // role-based restrictions:
        if ("Cashier".equalsIgnoreCase(loggedInRole)) {
            addProductBtn.setEnabled(false); // cashier cannot add product
        }
        if ("User".equalsIgnoreCase(loggedInRole)) {
            addProductBtn.setEnabled(false);
            // Users can view products and create bill but cannot edit/delete
        }

        dash.setVisible(true);

        // initial refresh
        refreshDashboardValues(totalProductsCard, todaysSalesCard, todaysCustomersCard);
    }

    private JButton styledMenuButton(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setBackground(PANEL_LIGHT.darker());
        b.setForeground(TEXT_ON_DARK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton styledDangerButton(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setBackground(new Color(200, 40, 40));
        b.setForeground(TEXT_ON_DARK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JPanel infoCard(String title, String valueText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60,60,60)), BorderFactory.createEmptyBorder(12,12,12,12)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLbl.setForeground(MUTED);

        JLabel valueLbl = new JLabel(valueText, SwingConstants.RIGHT);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(TEXT_ON_DARK);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        // store label for updates
        card.putClientProperty("valueLabel", valueLbl);
        return card;
    }

    private void refreshDashboardValues(JPanel totalProductsCard, JPanel todaysSalesCard, JPanel todaysCustomersCard) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // total products
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM products");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ((JLabel) totalProductsCard.getClientProperty("valueLabel")).setText(String.valueOf(rs.getInt(1)));
                }
            } catch (Exception ignored) {}

            // today's sales
            try (PreparedStatement ps = con.prepareStatement("SELECT IFNULL(SUM(total_amount),0) FROM bills WHERE bill_date = ?")) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ((JLabel) todaysSalesCard.getClientProperty("valueLabel")).setText(String.format("%.2f", rs.getDouble(1)));
                    }
                }
            } catch (Exception ignored) {}

            // today's customers
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bills WHERE bill_date = ?")) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ((JLabel) todaysCustomersCard.getClientProperty("valueLabel")).setText(String.valueOf(rs.getInt(1)));
                    }
                }
            } catch (Exception ignored) {}

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error refreshing dashboard: " + e.getMessage());
        }
    }
    // ---------------- Add Product ----------------
    private void showAddProductWindow() {
        JFrame add = new JFrame("Add Product");
        add.setSize(520, 520);
        add.setLocationRelativeTo(null);
        add.getContentPane().setBackground(BG_DARK);
        add.setLayout(null);

        JPanel p = new JPanel(null);
        p.setBackground(PANEL_LIGHT);
        p.setBounds(20, 20, 460, 440);
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add.add(p);

        JLabel idLbl = new JLabel("Product ID:");
        idLbl.setForeground(TEXT_ON_DARK);
        idLbl.setBounds(20, 20, 120, 26);
        p.add(idLbl);
        JTextField idField = new JTextField();
        idField.setBounds(160, 20, 280, 26);
        p.add(idField);

        JLabel nameLbl = new JLabel("Name:");
        nameLbl.setForeground(TEXT_ON_DARK);
        nameLbl.setBounds(20, 64, 120, 26);
        p.add(nameLbl);
        JTextField nameField = new JTextField();
        nameField.setBounds(160, 64, 280, 26);
        p.add(nameField);

        JLabel priceLbl = new JLabel("Price:");
        priceLbl.setForeground(TEXT_ON_DARK);
        priceLbl.setBounds(20, 108, 120, 26);
        p.add(priceLbl);
        JTextField priceField = new JTextField();
        priceField.setBounds(160, 108, 280, 26);
        p.add(priceField);

        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setForeground(TEXT_ON_DARK);
        qtyLbl.setBounds(20, 152, 120, 26);
        p.add(qtyLbl);
        JTextField qtyField = new JTextField();
        qtyField.setBounds(160, 152, 280, 26);
        p.add(qtyField);

        JLabel descLbl = new JLabel("Description:");
        descLbl.setForeground(TEXT_ON_DARK);
        descLbl.setBounds(20, 196, 120, 26);
        p.add(descLbl);
        JTextArea descArea = new JTextArea();
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descSP = new JScrollPane(descArea);
        descSP.setBounds(160, 196, 280, 160);
        p.add(descSP);

        JButton saveBtn = new JButton("Save Product");
        stylePrimaryButton(saveBtn);
        saveBtn.setBounds(160, 370, 160, 36);
        p.add(saveBtn);

        JLabel status = new JLabel("", SwingConstants.CENTER);
        status.setForeground(MUTED);
        status.setBounds(20, 410, 420, 24);
        p.add(status);

        saveBtn.addActionListener(e -> {
            // validate and insert; on success close the window
            try {
                int pid = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                String desc = descArea.getText().trim();

                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = con.prepareStatement("INSERT INTO products(product_id, product_name, price, quantity, description) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, pid);
                    ps.setString(2, name);
                    ps.setDouble(3, price);
                    ps.setInt(4, qty);
                    ps.setString(5, desc);
                    ps.executeUpdate();

                    add.dispose();
                } catch (SQLException ex) {
                    status.setForeground(Color.RED);
                    status.setText("DB Error: " + ex.getMessage());
                }
            } catch (NumberFormatException nfe) {
                status.setForeground(Color.RED);
                status.setText("Enter valid numeric values for ID, Price, Quantity.");
            }
        });

        add.setVisible(true);
    }

    // ---------------- View Products (editableFlag: true => admin can edit/delete; false => view-only) ----------------
    private void showProductListWindow(boolean editableFlag) {
        JFrame list = new JFrame("View Products");
        list.setSize(980, 560);
        list.setLocationRelativeTo(null);
        list.setLayout(new BorderLayout());
        list.getContentPane().setBackground(BG_DARK);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(BG_DARK);
        JLabel searchLbl = new JLabel("Search (ID or name):");
        searchLbl.setForeground(TEXT_ON_DARK);
        JTextField searchField = new JTextField(28);
        JButton refresh = new JButton("Refresh");
        stylePrimaryButton(refresh);
        top.add(searchLbl);
        top.add(searchField);
        top.add(refresh);
        list.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Price", "Quantity", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        JScrollPane sp = new JScrollPane(table);
        list.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_DARK);
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBtns.setBackground(BG_DARK);
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        stylePrimaryButton(editBtn);
        stylePrimaryButton(deleteBtn);

        // If user is not allowed to edit/delete, remove those buttons
        if (editableFlag) {
            leftBtns.add(editBtn);
            leftBtns.add(deleteBtn);
        }

        bottom.add(leftBtns, BorderLayout.WEST);

        JLabel totalLbl = new JLabel("Total: 0");
        totalLbl.setForeground(TEXT_ON_DARK);
        totalLbl.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        bottom.add(totalLbl, BorderLayout.EAST);

        list.add(bottom, BorderLayout.SOUTH);

        Runnable loadAll = () -> {
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT product_id, product_name, price, quantity, IFNULL(description,'') AS description FROM products")) {
                    int cnt = 0;
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("product_id"));
                        row.add(rs.getString("product_name"));
                        row.add(rs.getDouble("price"));
                        row.add(rs.getInt("quantity"));
                        row.add(rs.getString("description"));
                        model.addRow(row);
                        cnt++;
                    }
                    totalLbl.setText("Total: " + cnt);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(list, "Error loading products: " + ex.getMessage());
                }
            });
        };

        loadAll.run();

        DocumentListener searchListener = new DocumentListener() {
            void apply() {
                String q = searchField.getText().trim();
                if (q.isEmpty()) { loadAll.run(); return; }
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                         PreparedStatement ps = con.prepareStatement("SELECT product_id, product_name, price, quantity, IFNULL(description,'') AS description FROM products WHERE product_name LIKE ? OR product_id LIKE ?")) {
                        ps.setString(1, "%" + q + "%");
                        ps.setString(2, "%" + q + "%");
                        try (ResultSet rs = ps.executeQuery()) {
                            int cnt = 0;
                            while (rs.next()) {
                                Vector<Object> row = new Vector<>();
                                row.add(rs.getInt("product_id"));
                                row.add(rs.getString("product_name"));
                                row.add(rs.getDouble("price"));
                                row.add(rs.getInt("quantity"));
                                row.add(rs.getString("description"));
                                model.addRow(row);
                                cnt++;
                            }
                            totalLbl.setText("Total: " + cnt);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(list, "Search error: " + ex.getMessage());
                    }
                });
            }
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
        };
        searchField.getDocument().addDocumentListener(searchListener);

        refresh.addActionListener(e -> loadAll.run());

        deleteBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(list, "Select a product first.");
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            int confirm = JOptionPane.showConfirmDialog(list, "Delete product ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    loadAll.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(list, "Delete error: " + ex.getMessage());
                }
            }
        });

        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(list, "Select a product first."); return; }
            int id = (int) model.getValueAt(sel, 0);
            String name = (String) model.getValueAt(sel, 1);
            double price = (double) model.getValueAt(sel, 2);
            int qty = (int) model.getValueAt(sel, 3);
            String desc = (String) model.getValueAt(sel, 4);

            showEditProductWindow(id, name, price, qty, desc, loadAll, list);
        });

        list.setVisible(true);
    }

    // ---------------- Edit Product Window ----------------
    private void showEditProductWindow(int pid, String nm, double price, int qty, String desc,
                                       Runnable reload, JFrame parent) {

        JDialog dlg = new JDialog(parent, "Edit Product " + pid, true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(BG_DARK);

        JPanel p = new JPanel(null);
        p.setBackground(PANEL_LIGHT);
        p.setBounds(20, 20, 460, 440);
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        dlg.add(p);

        JLabel idL = new JLabel("Product ID:");
        idL.setForeground(TEXT_ON_DARK);
        idL.setBounds(20, 20, 120, 26);
        p.add(idL);
        JTextField idF = new JTextField(String.valueOf(pid));
        idF.setEditable(false);
        idF.setBounds(160, 20, 260, 26);
        p.add(idF);

        JLabel nameL = new JLabel("Name:");
        nameL.setForeground(TEXT_ON_DARK);
        nameL.setBounds(20, 64, 120, 26);
        p.add(nameL);
        JTextField nameF = new JTextField(nm);
        nameF.setBounds(160, 64, 260, 26);
        p.add(nameF);

        JLabel priceL = new JLabel("Price:");
        priceL.setForeground(TEXT_ON_DARK);
        priceL.setBounds(20, 108, 120, 26);
        p.add(priceL);
        JTextField priceF = new JTextField(String.valueOf(price));
        priceF.setBounds(160, 108, 260, 26);
        p.add(priceF);

        JLabel qtyL = new JLabel("Quantity:");
        qtyL.setForeground(TEXT_ON_DARK);
        qtyL.setBounds(20, 152, 120, 26);
        p.add(qtyL);
        JTextField qtyF = new JTextField(String.valueOf(qty));
        qtyF.setBounds(160, 152, 260, 26);
        p.add(qtyF);

        JLabel descL = new JLabel("Description:");
        descL.setForeground(TEXT_ON_DARK);
        descL.setBounds(20, 196, 120, 26);
        p.add(descL);
        JTextArea descA = new JTextArea(desc);
        JScrollPane descSP = new JScrollPane(descA);
        descSP.setBounds(160, 196, 260, 160);
        p.add(descSP);

        JButton save = new JButton("Save Changes");
        stylePrimaryButton(save);
        save.setBounds(160, 370, 160, 36);
        p.add(save);

        save.addActionListener(ev -> {
            try {
                String n = nameF.getText().trim();
                double pr = Double.parseDouble(priceF.getText().trim());
                int qtt = Integer.parseInt(qtyF.getText().trim());
                String dsc = descA.getText().trim();
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = con.prepareStatement("UPDATE products SET product_name=?, price=?, quantity=?, description=? WHERE product_id=?")) {
                    ps.setString(1, n);
                    ps.setDouble(2, pr);
                    ps.setInt(3, qtt);
                    ps.setString(4, dsc);
                    ps.setInt(5, pid);
                    ps.executeUpdate();
                    dlg.dispose();
                    reload.run();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dlg, "Enter valid numeric values.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, "DB Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }
    // ---------------- Create Bill ----------------
    private void showCreateBillWindow() {
        JFrame billFrame = new JFrame("Create Bill");
        billFrame.setSize(980, 620);
        billFrame.setLocationRelativeTo(null);
        billFrame.setLayout(new BorderLayout());
        billFrame.getContentPane().setBackground(BG_DARK);

        // left: product list with search
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BG_DARK);
        leftPanel.setPreferredSize(new Dimension(560, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_DARK);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(TEXT_ON_DARK);
        JTextField searchField = new JTextField(30);
        JButton refresh = new JButton("Refresh");
        stylePrimaryButton(refresh);
        searchPanel.add(searchLbl);
        searchPanel.add(searchField);
        searchPanel.add(refresh);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        String[] prodCols = {"ID", "Name", "Price", "Quantity", "Description"};
        DefaultTableModel prodModel = new DefaultTableModel(prodCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable prodTable = new JTable(prodModel);
        prodTable.setRowHeight(26);
        leftPanel.add(new JScrollPane(prodTable), BorderLayout.CENTER);

        // right: cart + totals
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(PANEL_LIGHT);
        rightPanel.setPreferredSize(new Dimension(400, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        String[] cartCols = {"Sl", "ID", "Name", "Qty", "Price", "Subtotal"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setRowHeight(26);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartBottom = new JPanel(new BorderLayout());
        cartBottom.setBackground(PANEL_LIGHT);

        JLabel totalCostLbl = new JLabel("Total: ₹0.00", SwingConstants.RIGHT);
        totalCostLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalCostLbl.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        cartBottom.add(totalCostLbl, BorderLayout.CENTER);

        JButton finalizeBtn = new JButton("Finalize Bill");
        stylePrimaryButton(finalizeBtn);
        finalizeBtn.setPreferredSize(new Dimension(150, 36));
        cartBottom.add(finalizeBtn, BorderLayout.SOUTH);

        rightPanel.add(cartBottom, BorderLayout.SOUTH);

        billFrame.add(leftPanel, BorderLayout.CENTER);
        billFrame.add(rightPanel, BorderLayout.EAST);

        // load products
        Runnable loadProducts = () -> {
            SwingUtilities.invokeLater(() -> {
                prodModel.setRowCount(0);
                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT product_id, product_name, price, quantity, IFNULL(description,'') AS description FROM products WHERE quantity > 0")) {
                    while (rs.next()) {
                        prodModel.addRow(new Object[]{
                                rs.getInt("product_id"),
                                rs.getString("product_name"),
                                rs.getDouble("price"),
                                rs.getInt("quantity"),
                                rs.getString("description")
                        });
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(billFrame, "Error loading products: " + ex.getMessage());
                }
            });
        };

        loadProducts.run();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void apply() {
                String q = searchField.getText().trim();
                SwingUtilities.invokeLater(() -> {
                    prodModel.setRowCount(0);
                    try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                         PreparedStatement ps = con.prepareStatement("SELECT product_id, product_name, price, quantity, IFNULL(description,'') AS description FROM products WHERE quantity > 0 AND (product_name LIKE ? OR product_id LIKE ?)")) {
                        ps.setString(1, "%" + q + "%");
                        ps.setString(2, "%" + q + "%");
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                prodModel.addRow(new Object[]{
                                        rs.getInt("product_id"),
                                        rs.getString("product_name"),
                                        rs.getDouble("price"),
                                        rs.getInt("quantity"),
                                        rs.getString("description")
                                });
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(billFrame, "Search error: " + ex.getMessage());
                    }
                });
            }
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
        });

        refresh.addActionListener(e -> loadProducts.run());

        // double-click product to add to cart (asks quantity)
        prodTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = prodTable.getSelectedRow();
                    if (row == -1) return;
                    int pid = (int) prodModel.getValueAt(row, 0);
                    String name = (String) prodModel.getValueAt(row, 1);
                    double price = (double) prodModel.getValueAt(row, 2);
                    int available = (int) prodModel.getValueAt(row, 3);

                    String qtyStr = JOptionPane.showInputDialog(billFrame, "Enter quantity for '" + name + "' (Available: " + available + "):", "1");
                    if (qtyStr == null) return;
                    try {
                        int q = Integer.parseInt(qtyStr.trim());
                        if (q <= 0) { JOptionPane.showMessageDialog(billFrame, "Quantity must be > 0."); return; }
                        if (q > available) { JOptionPane.showMessageDialog(billFrame, "Only " + available + " available."); return; }

                        int sl = cartModel.getRowCount() + 1;
                        double subtotal = q * price;
                        cartModel.addRow(new Object[]{sl, pid, name, q, price, subtotal});
                        updateCartTotal(cartModel, totalCostLbl);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(billFrame, "Enter a valid number.");
                    }
                }
            }
        });

        // finalize -> ask customer name, insert bill & items, update stock, show preview popup with bill number & date
        finalizeBtn.addActionListener(e -> {
            if (cartModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(billFrame, "Add items to cart first.");
                return;
            }

            String customer = JOptionPane.showInputDialog(billFrame, "Enter Customer Name:", "Walk-in");
            if (customer == null) return; // user cancelled
            if (customer.trim().isEmpty()) customer = "Walk-in";

            double total = getCartTotal(cartModel);

            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                con.setAutoCommit(false);
                int billId = -1;
                // insert bill: bills table should have auto-increment id, bill_date, customer_name, total_amount
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO bills (customer_name, bill_date, total_amount) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, customer);
                    ps.setDate(2, Date.valueOf(LocalDate.now()));
                    ps.setDouble(3, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) billId = keys.getInt(1);
                    }
                }

                if (billId == -1) {
                    con.rollback();
                    JOptionPane.showMessageDialog(billFrame, "Failed to generate bill id.");
                    return;
                }

                try (PreparedStatement psItem = con.prepareStatement("INSERT INTO bill_items (bill_id, product_id, product_name, quantity, price, subtotal) VALUES (?,?,?,?,?,?)");
                     PreparedStatement psUpd = con.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE product_id = ?")) {

                    for (int r = 0; r < cartModel.getRowCount(); r++) {
                        int pid = (int) cartModel.getValueAt(r, 1);
                        String name = (String) cartModel.getValueAt(r, 2);
                        int qty = (int) cartModel.getValueAt(r, 3);
                        double price = (double) cartModel.getValueAt(r, 4);
                        double subtotal = (double) cartModel.getValueAt(r, 5);

                        psItem.setInt(1, billId);
                        psItem.setInt(2, pid);
                        psItem.setString(3, name);
                        psItem.setInt(4, qty);
                        psItem.setDouble(5, price);
                        psItem.setDouble(6, subtotal);
                        psItem.executeUpdate();

                        psUpd.setInt(1, qty);
                        psUpd.setInt(2, pid);
                        psUpd.executeUpdate();
                    }
                }

                con.commit();

                // show final bill in new popup with billId and date
                showBillPreview(billId, customer, LocalDate.now(), cartModel, getCartTotal(cartModel));

                // close create bill window
                billFrame.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(billFrame, "Error finalizing bill: " + ex.getMessage());
            }
        });

        billFrame.setVisible(true);
    }

    // Build and show a Bill Preview window (Option A: in new popup) containing Bill Number, Date, Customer Name
    private void showBillPreview(int billId, String customer, LocalDate date, DefaultTableModel cartModel, double total) {
        JFrame preview = new JFrame("Bill - ID: " + billId);
        preview.setSize(700, 600);
        preview.setLocationRelativeTo(null);
        preview.setLayout(new BorderLayout());

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        sb.append(String.format("%40s\n", "SUPERMARKET\n"));
        sb.append("Bill No: " + billId + "\n");
        sb.append("Date: " + date.format(df) + "\n");
        sb.append("Customer: " + customer + "\n\n");
        sb.append(String.format("%-6s %-8s %-30s %-6s %-10s %-10s\n", "Sl", "ID", "Name", "Qty", "Price", "Subtotal"));
        sb.append("-------------------------------------------------------------------------------\n");
        for (int r = 0; r < cartModel.getRowCount(); r++) {
            int sl = (int) cartModel.getValueAt(r, 0);
            int pid = (int) cartModel.getValueAt(r, 1);
            String name = (String) cartModel.getValueAt(r, 2);
            int qty = (int) cartModel.getValueAt(r, 3);
            double price = (double) cartModel.getValueAt(r, 4);
            double subtotal = (double) cartModel.getValueAt(r, 5);
            sb.append(String.format("%-6d %-8d %-30s %-6d %-10.2f %-10.2f\n", sl, pid, (name.length() > 29 ? name.substring(0,29) : name), qty, price, subtotal));
        }
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append(String.format("%-60s Total: ₹%.2f\n", "", total));

        ta.setText(sb.toString());
        JScrollPane sp = new JScrollPane(ta);
        preview.add(sp, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        stylePrimaryButton(close);
        bot.add(close);
        preview.add(bot, BorderLayout.SOUTH);

        close.addActionListener(e -> preview.dispose());
        preview.setVisible(true);
    }

    // ---------- Helpers ----------
    private void updateCartTotal(DefaultTableModel cartModel, JLabel totalLabel) {
        double t = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Object val = cartModel.getValueAt(i, 5);
            if (val instanceof Number) t += ((Number) val).doubleValue();
        }
        totalLabel.setText("Total: ₹" + String.format("%.2f", t));
    }

    private double getCartTotal(DefaultTableModel cartModel) {
        double t = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            Object val = cartModel.getValueAt(i, 5);
            if (val instanceof Number) t += ((Number) val).doubleValue();
        }
        return t;
    }
}
