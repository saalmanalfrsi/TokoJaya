package tokojaya.form;

import tokojaya.koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

public class FormLogin extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JLabel         lblStatus;

    private final Color C_BG     = new Color(15, 19, 27);
    private final Color C_CARD   = new Color(24, 30, 44);
    private final Color C_GOLD   = new Color(212, 175, 55);
    private final Color C_GOLD2  = new Color(255, 215, 80);
    private final Color C_WHITE  = new Color(240, 244, 255);
    private final Color C_LIGHT  = new Color(180, 195, 220);
    private final Color C_MUTED  = new Color(100, 120, 155);
    private final Color C_BORDER = new Color(40, 52, 75);
    private final Color C_FIELD  = new Color(18, 24, 36);
    private final Color C_DANGER = new Color(251, 113, 133);
    private final Color C_SUCCESS= new Color(52, 211, 153);

    public FormLogin() {
        setTitle("Login — Toko Berkah Jaya POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));

        // Root panel dengan background gradient
        JPanel root = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Background utama
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(12, 15, 23),
                        0, getHeight(), new Color(20, 26, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Dekorasi lingkaran gold
                g2.setColor(new Color(212, 175, 55, 18));
                g2.fillOval(-150, -150, 500, 500);
                g2.setColor(new Color(212, 175, 55, 10));
                g2.fillOval(getWidth() - 300, getHeight() - 300, 550, 550);

                // Grid dots dekorasi
                g2.setColor(new Color(212, 175, 55, 12));
                for (int x = 0; x < getWidth(); x += 40) {
                    for (int y = 0; y < getHeight(); y += 40) {
                        g2.fillOval(x - 1, y - 1, 3, 3);
                    }
                }
            }
        };
        root.setOpaque(false);

        // Panel kiri — branding
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(18, 22, 34),
                        getWidth(), getHeight(), new Color(14, 18, 28));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Garis kanan
                GradientPaint border = new GradientPaint(
                        0, 0, new Color(212, 175, 55, 0),
                        0, getHeight() / 2f, new Color(212, 175, 55, 120));
                g2.setPaint(border);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight() / 2);
                GradientPaint border2 = new GradientPaint(
                        0, getHeight() / 2f, new Color(212, 175, 55, 120),
                        0, (float) getHeight(), new Color(212, 175, 55, 0));
                g2.setPaint(border2);
                g2.fillRect(getWidth() - 1, getHeight() / 2, 1, getHeight() / 2);
            }
        };

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setBorder(new EmptyBorder(0, 60, 0, 60));

        // Logo
        JPanel logoCircle = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(4, 6, getWidth() - 4, getHeight() - 4);
                GradientPaint gp = new GradientPaint(0, 0, C_GOLD2, getWidth(), getHeight(), C_GOLD);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth() - 2, getHeight() - 2);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(5, 5, getWidth() - 12, getHeight() - 12);
                g2.setColor(new Color(20, 24, 36));
                g2.setFont(new Font("SansSerif", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                String tx = "BJ";
                g2.drawString(tx, (getWidth() - 2 - fm.stringWidth(tx)) / 2,
                        (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        logoCircle.setOpaque(false);
        logoCircle.setPreferredSize(new Dimension(100, 100));
        logoCircle.setMaximumSize(new Dimension(100, 100));
        logoCircle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNama = new JLabel("TOKO BERKAH JAYA");
        lblNama.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblNama.setForeground(C_WHITE);
        lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Point of Sale System");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblSub.setForeground(C_GOLD);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Divider gold
        JPanel divLeft = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, C_GOLD2,
                        getWidth() * 0.6f, 0, new Color(212, 175, 55, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
            }
        };
        divLeft.setOpaque(false);
        divLeft.setPreferredSize(new Dimension(0, 2));
        divLeft.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        divLeft.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Features list
        String[] features = {
            "✓  Manajemen stok barang realtime",
            "✓  Transaksi kasir dengan kembalian",
            "✓  Laporan penjualan rinci",
            "✓  Multi-user: Admin & Petugas",
            "✓  Dashboard statistik harian"
        };

        leftContent.add(Box.createVerticalGlue());
        leftContent.add(logoCircle);
        leftContent.add(Box.createVerticalStrut(24));
        leftContent.add(lblNama);
        leftContent.add(Box.createVerticalStrut(6));
        leftContent.add(lblSub);
        leftContent.add(Box.createVerticalStrut(28));
        leftContent.add(divLeft);
        leftContent.add(Box.createVerticalStrut(28));
        for (String f : features) {
            JLabel lf = new JLabel(f);
            lf.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lf.setForeground(C_LIGHT);
            lf.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftContent.add(lf);
            leftContent.add(Box.createVerticalStrut(10));
        }
        leftContent.add(Box.createVerticalStrut(20));
        JLabel copy = new JLabel("© 2026 Toko Berkah Jaya. All rights reserved.");
        copy.setFont(new Font("SansSerif", Font.PLAIN, 10));
        copy.setForeground(new Color(70, 90, 120));
        copy.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftContent.add(copy);
        leftContent.add(Box.createVerticalGlue());

        leftPanel.add(leftContent, new GridBagConstraints());

        // Panel kanan — form login
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(C_GOLD.getRed(), C_GOLD.getGreen(), C_GOLD.getBlue(), 65));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 500));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;

        JLabel lblLogin = new JLabel("Masuk ke Sistem");
        lblLogin.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblLogin.setForeground(C_WHITE);
        lblLogin.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 0; gc.insets = new Insets(36, 36, 4, 36);
        card.add(lblLogin, gc);

        JLabel lblDesc = new JLabel("Silakan login dengan akun Anda");
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDesc.setForeground(C_MUTED);
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 1; gc.insets = new Insets(0, 36, 28, 36);
        card.add(lblDesc, gc);

        // Username
        JLabel lblU = new JLabel("Username");
        lblU.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblU.setForeground(C_MUTED);
        gc.gridy = 2; gc.insets = new Insets(0, 36, 5, 36);
        card.add(lblU, gc);
        txtUsername = new JTextField();
        styleField(txtUsername);
        gc.gridy = 3; gc.insets = new Insets(0, 36, 16, 36);
        card.add(txtUsername, gc);

        // Password
        JLabel lblP = new JLabel("Password");
        lblP.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblP.setForeground(C_MUTED);
        gc.gridy = 4; gc.insets = new Insets(0, 36, 5, 36);
        card.add(lblP, gc);
        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('●');
        styleField(txtPassword);
        gc.gridy = 5; gc.insets = new Insets(0, 36, 8, 36);
        card.add(txtPassword, gc);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(C_DANGER);
        gc.gridy = 6; gc.insets = new Insets(0, 36, 8, 36);
        card.add(lblStatus, gc);

        JButton btnLogin = new JButton("MASUK") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = hov ?
                        new GradientPaint(0, 0, C_GOLD2, getWidth(), 0, C_GOLD) :
                        new GradientPaint(0, 0, C_GOLD, getWidth(), 0, new Color(160, 125, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setForeground(new Color(20, 24, 36));
        btnLogin.setContentAreaFilled(false); btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 48));
        btnLogin.setBorder(BorderFactory.createEmptyBorder());
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gc.gridy = 7; gc.insets = new Insets(0, 36, 36, 36);
        card.add(btnLogin, gc);

        rightPanel.add(card, new GridBagConstraints());

        // Layout split: kiri 55% kanan 45%
        GridBagConstraints rootGc = new GridBagConstraints();
        rootGc.fill = GridBagConstraints.BOTH;
        rootGc.weighty = 1.0;

        rootGc.gridx = 0; rootGc.weightx = 0.55;
        root.add(leftPanel, rootGc);

        rootGc.gridx = 1; rootGc.weightx = 0.45;
        root.add(rightPanel, rootGc);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(C_BG);
        bg.add(root, BorderLayout.CENTER);
        setContentPane(bg);

        // Events
        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) login();
            }
        };
        txtUsername.addKeyListener(enter);
        txtPassword.addKeyListener(enter);
        btnLogin.addActionListener(e -> login());

        setVisible(true);
    }

    private void styleField(JTextField f) {
        f.setBackground(C_FIELD);
        f.setForeground(C_WHITE);
        f.setCaretColor(C_GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        f.setPreferredSize(new Dimension(0, 46));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(new Color(212, 175, 55, 160), 1, true),
                        new EmptyBorder(11, 14, 11, 14)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(C_BORDER, 1, true),
                        new EmptyBorder(11, 14, 11, 14)));
            }
        });
    }

    private void login() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("⚠  Username dan password wajib diisi!"); return;
        }
        try {
            Connection c = Koneksi.getConnection();
            if (c == null) { lblStatus.setText("✕  Koneksi database gagal!"); return; }
            PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM tb_user WHERE username=? AND password=?");
            ps.setString(1, user); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nama  = rs.getString("nama_lengkap");
                String level = rs.getString("level");
                int    id    = rs.getInt("id_user");
                c.close(); dispose();
                new MainWindow(nama, level, id).setVisible(true);
            } else {
                lblStatus.setText("✕  Username atau password salah!");
                txtPassword.setText("");
            }
        } catch (Exception ex) {
            lblStatus.setText("✕  Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FormLogin::new);
    }
}