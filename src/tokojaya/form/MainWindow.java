package tokojaya.form;

import tokojaya.dao.BarangDAO;
import tokojaya.model.Barang;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow extends JFrame {

    private JPanel      contentArea;
    private CardLayout  cardLayout;
    private String      namaUser, levelUser;
    private int         idUser;
    private JPanel[]    navItems;
    private JLabel[]    navLabels, navSubs;
    private int         activeIdx = 0;
    private JLabel      lblJam;
    private JLabel      lblBreadcrumb, lblUserName, lblUserLevel;

    public PanelBarang    panelBarang;
    public PanelTransaksi panelTransaksi;
    public PanelLaporan   panelLaporan;
    public PanelDashboard panelDashboard;

    // Warna chrome window — bersumber dari Theme (berganti saat Dark/Light di-toggle).
    private Color C_BG, C_SIDEBAR, C_TOPBAR, C_CARD, C_CARD2,
                  C_WHITE, C_LIGHT, C_MUTED, C_BORDER, C_ACTIVE, C_HOVER;
    private final Color C_GOLD    = tokojaya.util.Theme.GOLD;
    private final Color C_GOLD2   = tokojaya.util.Theme.GOLD2;
    private final Color C_SUCCESS = tokojaya.util.Theme.SUCCESS;
    private final Color C_DANGER  = tokojaya.util.Theme.DANGER;

    { refreshColors(); }  // inisialisasi palet sebelum komponen dibangun

    /** Salin palet aktif dari Theme ke field chrome window. */
    private void refreshColors() {
        C_BG      = tokojaya.util.Theme.BG;
        C_SIDEBAR = tokojaya.util.Theme.CARD;
        C_TOPBAR  = tokojaya.util.Theme.CARD;
        C_CARD    = tokojaya.util.Theme.CARD2;
        C_CARD2   = tokojaya.util.Theme.CARD3;
        C_WHITE   = tokojaya.util.Theme.WHITE;
        C_LIGHT   = tokojaya.util.Theme.LIGHT;
        C_MUTED   = tokojaya.util.Theme.MUTED;
        C_BORDER  = tokojaya.util.Theme.BORDER;
        C_ACTIVE  = tokojaya.util.Theme.ACTIVE;
        C_HOVER   = tokojaya.util.Theme.HOVER;
    }

    public MainWindow(String namaUser, String levelUser, int idUser) {
        this.namaUser  = namaUser;
        this.levelUser = levelUser;
        this.idUser    = idUser;

        setTitle("Toko Berkah Jaya — POS System");
        setSize(1180, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));

        boolean isAdmin = "Admin".equals(levelUser);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        root.add(buatTopbar(),  BorderLayout.NORTH);
        root.add(buatSidebar(), BorderLayout.WEST);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(C_BG);

        panelDashboard = new PanelDashboard(this);
        panelBarang    = new PanelBarang(isAdmin);
        panelTransaksi = new PanelTransaksi(idUser, this);
        panelLaporan   = new PanelLaporan();

        contentArea.add(panelDashboard,                  "dashboard");
        contentArea.add(panelBarang,                     "barang");
        contentArea.add(new PanelCustomer(isAdmin),      "customer");
        contentArea.add(panelTransaksi,                  "transaksi");
        contentArea.add(panelLaporan,                    "laporan");

        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);

        startClock();
        navigateTo(0);
        cekStokKritis();

        // Perbarui chrome window setiap kali tema berganti.
        tokojaya.util.Theme.addListener(this::applyTheme);
    }

    /** Terapkan ulang warna chrome (sidebar/topbar/nav) saat Dark/Light di-toggle. */
    private void applyTheme() {
        refreshColors();
        if (lblBreadcrumb != null) lblBreadcrumb.setForeground(C_WHITE);
        if (lblUserName  != null)  lblUserName.setForeground(C_WHITE);
        if (lblUserLevel != null)  lblUserLevel.setForeground(C_MUTED);
        if (navLabels != null) {
            for (int i = 0; i < navLabels.length; i++) {
                navLabels[i].setForeground(i == activeIdx ? C_WHITE : C_LIGHT);
                navSubs[i].setForeground(i == activeIdx ? C_GOLD : C_MUTED);
            }
        }
        SwingUtilities.invokeLater(() -> {
            getContentPane().revalidate();
            getContentPane().repaint();
        });
    }

    private void startClock() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (lblJam != null) {
                    SwingUtilities.invokeLater(() -> {
                        lblJam.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    });
                }
            }
        }, 0, 1000);
    }

    private void cekStokKritis() {
        SwingUtilities.invokeLater(() -> {
            BarangDAO dao = new BarangDAO();
            List<Barang> list = dao.getAll();
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Barang b : list) {
                if (b.getStok() <= 5) {
                    sb.append("  • ").append(b.getNamaBarang())
                      .append(" → Stok: ").append(b.getStok()).append("\n");
                    count++;
                }
            }
            if (count > 0) {
                JOptionPane.showMessageDialog(this,
                    "⚠  Perhatian! " + count + " barang memiliki stok kritis:\n\n" + sb.toString(),
                    "🔔 Peringatan Stok Kritis", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    public void refreshPanelBarang() {
        panelBarang.loadDataPublic();
        if (panelDashboard != null) panelDashboard.refreshData();
    }

    public void refreshDashboard() {
        if (panelDashboard != null) panelDashboard.refreshData();
    }

    private JPanel buatTopbar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                g.setColor(C_TOPBAR); g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(C_BORDER); g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftGroup.setOpaque(false);
        JLabel dot = new JLabel("◆ ");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10)); dot.setForeground(C_GOLD);
        JLabel lblPage = new JLabel("Dashboard");
        lblPage.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblPage.setForeground(C_WHITE); lblPage.setName("breadcrumb");
        lblBreadcrumb = lblPage;
        leftGroup.add(dot); leftGroup.add(lblPage);
        bar.add(leftGroup, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // Jam realtime
        lblJam = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        lblJam.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblJam.setForeground(C_GOLD);
        JPanel jamBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(212, 175, 55, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(212, 175, 55, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        jamBox.setOpaque(false);
        JLabel icoJam = new JLabel("⏰");
        icoJam.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jamBox.add(icoJam); jamBox.add(lblJam);

        boolean isAdmin = "Admin".equals(levelUser);

        JLabel badge = new JLabel(" " + levelUser + " ") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isAdmin) {
                    g2.setColor(new Color(212, 175, 55, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(212, 175, 55, 150));
                } else {
                    g2.setColor(new Color(52, 211, 153, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(52, 211, 153, 150));
                }
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(isAdmin ? C_GOLD : C_SUCCESS);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(212, 175, 55, 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
            }
        };
        chip.setOpaque(false);

        JPanel ava = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = isAdmin ? C_GOLD2 : C_SUCCESS;
                Color c2 = isAdmin ? C_GOLD  : new Color(30, 160, 100);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp); g2.fillOval(0, 0, 32, 32);
                g2.setColor(new Color(20, 24, 36));
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String ini = namaUser.length() > 1 ? namaUser.substring(0, 2).toUpperCase() : "U";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (32 - fm.stringWidth(ini)) / 2, (32 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        ava.setOpaque(false); ava.setPreferredSize(new Dimension(32, 32));

        JPanel uinfo = new JPanel(new GridLayout(2, 1, 0, 1)); uinfo.setOpaque(false);
        JLabel ln = new JLabel(namaUser);
        ln.setFont(new Font("SansSerif", Font.BOLD, 12)); ln.setForeground(C_WHITE);
        JLabel lv = new JLabel(levelUser);
        lv.setFont(new Font("SansSerif", Font.PLAIN, 10)); lv.setForeground(C_MUTED);
        lblUserName = ln; lblUserLevel = lv;
        uinfo.add(ln); uinfo.add(lv);
        chip.add(ava); chip.add(uinfo);

        JButton btnOut = new JButton("Keluar") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(251, 113, 133, 30) : C_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hov ? new Color(251, 113, 133, 150) : C_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                super.paintComponent(g);
            }
        };
        btnOut.setFont(new Font("SansSerif", Font.PLAIN, 12)); btnOut.setForeground(C_DANGER);
        btnOut.setContentAreaFilled(false); btnOut.setFocusPainted(false);
        btnOut.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOut.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Logout", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { dispose(); new FormLogin().setVisible(true); }
        });

        // Tombol toggle tema Dark/Light
        JButton btnTema = new JButton(tokojaya.util.Theme.isDark() ? "☀" : "🌙") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(212, 175, 55, 40) : C_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(212, 175, 55, hov ? 150 : 70));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                super.paintComponent(g);
            }
        };
        btnTema.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnTema.setForeground(C_GOLD);
        btnTema.setContentAreaFilled(false); btnTema.setFocusPainted(false);
        btnTema.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnTema.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTema.setToolTipText("Ganti tema Dark/Light");
        btnTema.addActionListener(e -> {
            tokojaya.util.Theme.toggle();
            btnTema.setText(tokojaya.util.Theme.isDark() ? "☀" : "🌙");
        });

        right.add(jamBox); right.add(badge); right.add(chip); right.add(btnTema); right.add(btnOut);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buatSidebar() {
        JPanel sb = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(C_SIDEBAR); g2.fillRect(0, 0, getWidth(), getHeight());
                GradientPaint gp = new GradientPaint(getWidth()-1, 0, new Color(212, 175, 55, 100),
                        getWidth()-1, getHeight(), new Color(212, 175, 55, 0));
                g2.setPaint(gp); g2.fillRect(getWidth()-1, 0, 1, getHeight());
            }
        };
        sb.setOpaque(false);
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(225, 0));

        JPanel logoArea = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(18, 22, 32)); g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(C_BORDER); g.fillRect(0, getHeight()-1, getWidth(), 1);
            }
        };
        logoArea.setOpaque(false);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        logoArea.setPreferredSize(new Dimension(225, 80));
        logoArea.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel li = new JPanel(); li.setOpaque(false);
        li.setLayout(new BoxLayout(li, BoxLayout.Y_AXIS));
        li.setBorder(new EmptyBorder(18, 0, 18, 0));
        JLabel lN = new JLabel("BERKAH JAYA");
        lN.setFont(new Font("SansSerif", Font.BOLD, 15)); lN.setForeground(C_WHITE);
        lN.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lS = new JLabel("Point of Sale");
        lS.setFont(new Font("SansSerif", Font.PLAIN, 10)); lS.setForeground(C_GOLD);
        lS.setAlignmentX(Component.LEFT_ALIGNMENT);
        li.add(lN); li.add(Box.createVerticalStrut(3)); li.add(lS);
        logoArea.add(li, BorderLayout.CENTER);

        boolean isAdmin = "Admin".equals(levelUser);

        JPanel aksesWrap = new JPanel(new BorderLayout());
        aksesWrap.setOpaque(false);
        aksesWrap.setBorder(new EmptyBorder(10, 12, 4, 12));
        aksesWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JPanel aksesBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isAdmin) {
                    g2.setColor(new Color(212, 175, 55, 18)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(212, 175, 55, 60));
                } else {
                    g2.setColor(new Color(52, 211, 153, 15)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(52, 211, 153, 55));
                }
                g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        aksesBox.setOpaque(false);
        aksesBox.setLayout(new BoxLayout(aksesBox, BoxLayout.Y_AXIS));
        aksesBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel la1 = new JLabel(isAdmin ? "🔓  Akses Penuh — Admin" : "🔒  Akses Terbatas — Petugas");
        la1.setFont(new Font("SansSerif", Font.BOLD, 11));
        la1.setForeground(isAdmin ? C_GOLD : C_SUCCESS);
        la1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel la2 = new JLabel(isAdmin ? "Semua fitur tersedia" : "Transaksi & laporan saja");
        la2.setFont(new Font("SansSerif", Font.PLAIN, 10)); la2.setForeground(C_MUTED);
        la2.setAlignmentX(Component.LEFT_ALIGNMENT);
        aksesBox.add(la1); aksesBox.add(Box.createVerticalStrut(2)); aksesBox.add(la2);
        aksesWrap.add(aksesBox, BorderLayout.CENTER);

        JLabel lblNav = new JLabel("NAVIGASI");
        lblNav.setFont(new Font("SansSerif", Font.BOLD, 9));
        lblNav.setForeground(new Color(C_MUTED.getRed(), C_MUTED.getGreen(), C_MUTED.getBlue(), 140));
        lblNav.setBorder(new EmptyBorder(14, 20, 8, 20));
        lblNav.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] menus = {
            {"🏠", "Dashboard",     "Ringkasan & statistik"},
            {"📦", "Data Barang",   "Kelola produk & stok"},
            {"👤", "Data Customer", "Kelola pelanggan"},
            {"🧾", "Transaksi",     "Proses penjualan"},
            {"📊", "Laporan",       "Riwayat penjualan"},
        };
        navItems  = new JPanel[menus.length];
        navLabels = new JLabel[menus.length];
        navSubs   = new JLabel[menus.length];
        for (int i = 0; i < menus.length; i++)
            navItems[i] = buatNavItem(menus[i][0], menus[i][1], menus[i][2], i);

        JLabel ver = new JLabel("v2.0.0  ·  2026");
        ver.setFont(new Font("SansSerif", Font.PLAIN, 10));
        ver.setForeground(new Color(C_MUTED.getRed(), C_MUTED.getGreen(), C_MUTED.getBlue(), 90));
        ver.setBorder(new EmptyBorder(0, 20, 18, 20));
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);

        sb.add(logoArea); sb.add(aksesWrap); sb.add(lblNav);
        for (JPanel n : navItems) sb.add(n);
        sb.add(Box.createVerticalGlue()); sb.add(ver);
        return sb;
    }

    private JPanel buatNavItem(String ico, String label, String sub, int idx) {
        JPanel item = new JPanel(new BorderLayout()) {
            private boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { navigateTo(idx); }
                });
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean act = (activeIdx == idx);
                if (act) {
                    g2.setColor(C_ACTIVE); g2.fillRect(0, 0, getWidth(), getHeight());
                    GradientPaint gp = new GradientPaint(0, 0, C_GOLD2, 0, getHeight(), C_GOLD);
                    g2.setPaint(gp); g2.fillRect(0, 0, 3, getHeight());
                } else if (hov) {
                    g2.setColor(C_HOVER); g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        item.setPreferredSize(new Dimension(225, 58));
        item.setBorder(new EmptyBorder(0, 18, 0, 14));

        JLabel icoL = new JLabel(ico);
        icoL.setFont(new Font("SansSerif", Font.PLAIN, 18));
        icoL.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel tp = new JPanel(new GridLayout(2, 1, 0, 2)); tp.setOpaque(false);
        JLabel lMain = new JLabel(label);
        lMain.setFont(new Font("SansSerif", Font.BOLD, 13));
        lMain.setForeground(activeIdx == idx ? C_WHITE : C_LIGHT);
        JLabel lSub = new JLabel(sub);
        lSub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lSub.setForeground(activeIdx == idx ? C_GOLD : C_MUTED);
        tp.add(lMain); tp.add(lSub);
        navLabels[idx] = lMain; navSubs[idx] = lSub;

        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("SansSerif", Font.BOLD, 20));
        arrow.setForeground(new Color(C_GOLD.getRed(), C_GOLD.getGreen(), C_GOLD.getBlue(), activeIdx == idx ? 220 : 60));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false); left.setBorder(new EmptyBorder(10, 0, 10, 0));
        left.add(icoL); left.add(tp);
        item.add(left, BorderLayout.CENTER); item.add(arrow, BorderLayout.EAST);
        return item;
    }

    public void navigateTo(int idx) {
        activeIdx = idx;
        String[] cards  = {"dashboard", "barang", "customer", "transaksi", "laporan"};
        String[] titles = {"Dashboard", "Data Barang", "Data Customer", "Transaksi Penjualan", "Laporan Penjualan"};
        cardLayout.show(contentArea, cards[idx]);

        if (idx == 0) panelDashboard.refreshData();
        if (idx == 1) panelBarang.loadDataPublic();
        if (idx == 3) panelTransaksi.refreshDaftarBarang();
        if (idx == 4) panelLaporan.refreshData();

        BorderLayout bl = (BorderLayout) ((JPanel) getContentPane()).getLayout();
        JPanel topbar = (JPanel) bl.getLayoutComponent(BorderLayout.NORTH);
        for (Component c : topbar.getComponents()) {
            if (c instanceof JPanel) {
                for (Component cc : ((JPanel) c).getComponents()) {
                    if (cc instanceof JLabel && "breadcrumb".equals(cc.getName()))
                        ((JLabel) cc).setText(titles[idx]);
                }
            }
        }
        for (int i = 0; i < navItems.length; i++) {
            navLabels[i].setForeground(i == idx ? C_WHITE : C_LIGHT);
            navSubs[i].setForeground(i == idx ? C_GOLD : C_MUTED);
            navItems[i].repaint();
        }
    }
}