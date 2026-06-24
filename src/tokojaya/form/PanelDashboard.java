package tokojaya.form;

import tokojaya.koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelDashboard extends JPanel {

    private MainWindow mainWindow;
    private JLabel lblTotalBarang, lblTotalCustomer, lblTrxHariIni, lblPendapatanHariIni;
    private JLabel lblStokKritis, lblTotalTrx, lblTotalPendapatan;
    private JPanel grafikPanel;

    // Warna switchable — bersumber dari Theme (Dark/Light).
    private Color BG, CARD, CARD2, WHITE, LIGHT, MUTED, BORDER;
    private final Color GOLD    = tokojaya.util.Theme.GOLD;
    private final Color GOLD2   = tokojaya.util.Theme.GOLD2;
    private final Color SUCCESS = tokojaya.util.Theme.SUCCESS;
    private final Color DANGER  = tokojaya.util.Theme.DANGER;
    private final Color WARN    = tokojaya.util.Theme.WARN;
    private final Color BLUE    = tokojaya.util.Theme.INFO;

    { refreshColors(); }

    private void refreshColors() {
        BG     = tokojaya.util.Theme.BG;
        CARD   = tokojaya.util.Theme.CARD;
        CARD2  = tokojaya.util.Theme.CARD2;
        WHITE  = tokojaya.util.Theme.WHITE;
        LIGHT  = tokojaya.util.Theme.LIGHT;
        MUTED  = tokojaya.util.Theme.MUTED;
        BORDER = tokojaya.util.Theme.BORDER;
    }

    // Data grafik 7 hari
    private double[] grafikData = new double[7];
    private String[] grafikLabel = new String[7];

    public PanelDashboard(MainWindow mw) {
        this.mainWindow = mw;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        buildUI();
        refreshData();

        // Bangun ulang dashboard saat tema berganti agar warna konsisten.
        tokojaya.util.Theme.addListener(() -> {
            refreshColors();
            setBackground(BG);
            removeAll();
            buildUI();
            refreshData();
            revalidate();
            repaint();
        });
    }

    private void buildUI() {
        JScrollPane scroll = new JScrollPane(buatKonten());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buatKonten() {
        JPanel konten = new JPanel();
        konten.setLayout(new BoxLayout(konten, BoxLayout.Y_AXIS));
        konten.setBackground(BG);
        konten.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel lblJudul = new JLabel("Dashboard");
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 22)); lblJudul.setForeground(WHITE);
        JLabel lblTanggal = new JLabel(new SimpleDateFormat("EEEE, dd MMMM yyyy",
                new java.util.Locale("id", "ID")).format(new Date()));
        lblTanggal.setFont(new Font("SansSerif", Font.PLAIN, 12)); lblTanggal.setForeground(MUTED);
        JPanel hRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); hRight.setOpaque(false);
        JButton btnRefresh = buatBtnKecil("↺  Refresh", GOLD);
        btnRefresh.addActionListener(e -> refreshData());
        hRight.add(btnRefresh);
        header.add(lblJudul, BorderLayout.WEST); header.add(lblTanggal, BorderLayout.CENTER);
        header.add(hRight, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        konten.add(header); konten.add(Box.createVerticalStrut(20));

        // Baris statistik atas — 4 kartu
        JPanel row1 = new JPanel(new GridLayout(1, 4, 14, 0));
        row1.setOpaque(false); row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        lblTrxHariIni       = new JLabel("0");
        lblPendapatanHariIni = new JLabel("Rp 0");
        lblTotalBarang      = new JLabel("0");
        lblTotalCustomer    = new JLabel("0");
        row1.add(buatStatCard("🧾", "Transaksi Hari Ini", lblTrxHariIni,       GOLD,    "transaksi hari ini"));
        row1.add(buatStatCard("💰", "Pendapatan Hari Ini", lblPendapatanHariIni, SUCCESS, "total hari ini"));
        row1.add(buatStatCard("📦", "Total Barang",         lblTotalBarang,       BLUE,    "produk terdaftar"));
        row1.add(buatStatCard("👤", "Total Customer",       lblTotalCustomer,     new Color(196, 132, 252), "pelanggan terdaftar"));
        konten.add(row1); konten.add(Box.createVerticalStrut(14));

        // Baris statistik bawah — 3 kartu
        JPanel row2 = new JPanel(new GridLayout(1, 3, 14, 0));
        row2.setOpaque(false); row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        lblStokKritis     = new JLabel("0");
        lblTotalTrx       = new JLabel("0");
        lblTotalPendapatan = new JLabel("Rp 0");
        row2.add(buatStatCardKecil("⚠️", "Stok Kritis",       lblStokKritis,      WARN));
        row2.add(buatStatCardKecil("📈", "Total Semua Transaksi", lblTotalTrx,     BLUE));
        row2.add(buatStatCardKecil("💎", "Total Semua Pendapatan", lblTotalPendapatan, GOLD));
        konten.add(row2); konten.add(Box.createVerticalStrut(20));

        // Grafik pendapatan 7 hari
        JPanel grafikWrap = buatGrafikPanel();
        grafikWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        konten.add(grafikWrap); konten.add(Box.createVerticalStrut(14));

        // Akses cepat
        JPanel aksesWrap = buatAksesCepat();
        aksesWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        konten.add(aksesWrap);

        return konten;
    }

    private JPanel buatStatCard(String ico, String label, JLabel lblNilai, Color accent, String sub) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.fillRoundRect(0, getHeight()-6, getWidth(), 6, 0, 0);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
            }
        };
        card.setLayout(new BorderLayout(0, 4));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel icoLbl = new JLabel(ico + "  " + label);
        icoLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        icoLbl.setForeground(MUTED);

        lblNilai.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblNilai.setForeground(accent);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subLbl.setForeground(new Color(MUTED.getRed(), MUTED.getGreen(), MUTED.getBlue(), 150));

        JPanel bot = new JPanel(new BorderLayout()); bot.setOpaque(false);
        bot.add(lblNilai, BorderLayout.WEST); bot.add(subLbl, BorderLayout.EAST);

        card.add(icoLbl, BorderLayout.NORTH); card.add(bot, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatStatCardKecil(String ico, String label, JLabel lblNilai, Color accent) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD2); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        card.setLayout(new BorderLayout(8, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel icoLbl = new JLabel(ico);
        icoLbl.setFont(new Font("SansSerif", Font.PLAIN, 22));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2)); info.setOpaque(false);
        JLabel lLbl = new JLabel(label);
        lLbl.setFont(new Font("SansSerif", Font.PLAIN, 10)); lLbl.setForeground(MUTED);
        lblNilai.setFont(new Font("Monospaced", Font.BOLD, 18)); lblNilai.setForeground(accent);
        info.add(lLbl); info.add(lblNilai);

        card.add(icoLbl, BorderLayout.WEST); card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatGrafikPanel() {
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel lbl = new JLabel("📈  Pendapatan 7 Hari Terakhir");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13)); lbl.setForeground(WHITE);
        header.add(lbl, BorderLayout.WEST);
        wrap.add(header, BorderLayout.NORTH);

        grafikPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(40, 52, 75, 100));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);

                if (grafikData == null) return;

                int pad = 40, padB = 50, w = getWidth()-pad*2, h = getHeight()-padB-20;
                double maxVal = 1;
                for (double v : grafikData) if (v > maxVal) maxVal = v;

                // Grid lines
                g2.setColor(new Color(40, 52, 75, 150));
                g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10, new float[]{4, 4}, 0));
                for (int i = 0; i <= 4; i++) {
                    int y = 20 + (h * i / 4);
                    g2.drawLine(pad, y, pad+w, y);
                    // Label nilai
                    g2.setColor(MUTED); g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    long val = (long)(maxVal * (4-i) / 4);
                    String valStr = val >= 1000000 ? String.format("%.1fJt", val/1000000.0) :
                                    val >= 1000    ? String.format("%.0fRb", val/1000.0) : String.valueOf(val);
                    g2.drawString(valStr, 2, y + 4);
                    g2.setColor(new Color(40, 52, 75, 150));
                }

                // Batang grafik
                int barW = w / grafikData.length - 8;
                for (int i = 0; i < grafikData.length; i++) {
                    double ratio = maxVal == 0 ? 0 : grafikData[i] / maxVal;
                    int barH = (int)(h * ratio);
                    int x = pad + i * (w / grafikData.length) + 4;
                    int y = 20 + h - barH;

                    // Gradient bar
                    GradientPaint gp = new GradientPaint(x, y, GOLD2, x, y+barH, new Color(160, 120, 20));
                    g2.setPaint(gp);
                    g2.setStroke(new BasicStroke(1));
                    g2.fillRoundRect(x, y, barW, barH, 6, 6);

                    // Glow effect
                    g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 30));
                    g2.fillRoundRect(x-2, y-2, barW+4, barH+4, 8, 8);

                    // Label hari
                    g2.setColor(MUTED); g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    FontMetrics fm = g2.getFontMetrics();

                    String lbStr = "";
                        if (grafikLabel != null &&
                            i < grafikLabel.length &&
                            grafikLabel[i] != null) {
                            lbStr = grafikLabel[i];
                        }

                        g2.drawString(lbStr,
                            x + (barW - fm.stringWidth(lbStr)) / 2,
                            20 + h + 15);

                    // Nilai di atas bar
                    if (grafikData[i] > 0) {
                        g2.setColor(GOLD);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                        String valStr = grafikData[i] >= 1000000 ?
                                String.format("%.1fJt", grafikData[i]/1000000.0) :
                                String.format("%.0fRb", grafikData[i]/1000.0);
                        FontMetrics fm2 = g2.getFontMetrics();
                        g2.drawString(valStr, x + (barW - fm2.stringWidth(valStr))/2, y - 4);
                    }
                }
            }
        };
        grafikPanel.setPreferredSize(new Dimension(0, 220));
        grafikPanel.setOpaque(false);
        wrap.add(grafikPanel, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buatAksesCepat() {
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        JLabel lbl = new JLabel("⚡  Akses Cepat");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13)); lbl.setForeground(WHITE);
        lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 4, 12, 0)); btnRow.setOpaque(false);

        JButton bTrx = buatBtnAkses("🧾", "Transaksi Baru", SUCCESS);
        JButton bBrg = buatBtnAkses("📦", "Data Barang",     BLUE);
        JButton bCst = buatBtnAkses("👤", "Data Customer",   new Color(196, 132, 252));
        JButton bLap = buatBtnAkses("📊", "Laporan",          GOLD);

        bTrx.addActionListener(e -> mainWindow.navigateTo(3));
        bBrg.addActionListener(e -> mainWindow.navigateTo(1));
        bCst.addActionListener(e -> mainWindow.navigateTo(2));
        bLap.addActionListener(e -> mainWindow.navigateTo(4));

        btnRow.add(bTrx); btnRow.add(bBrg); btnRow.add(bCst); btnRow.add(bLap);
        wrap.add(btnRow, BorderLayout.CENTER);
        return wrap;
    }

    private JButton buatBtnAkses(String ico, String label, Color accent) {
        JButton b = new JButton() {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30) : CARD2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hov ? 120 : 60));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                int icoW = fm.stringWidth(ico);
                g2.drawString(ico, (getWidth()-icoW)/2, getHeight()/2 - 6);

                g2.setColor(hov ? WHITE : LIGHT);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm2 = g2.getFontMetrics();
                int lW = fm2.stringWidth(label);
                g2.drawString(label, (getWidth()-lW)/2, getHeight()/2 + 16);
            }
        };
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 8, 12, 8));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton buatBtnKecil(String t, Color accent) {
        JButton b = new JButton(t) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = hov ? new GradientPaint(0, 0, GOLD2, getWidth(), 0, accent)
                        : new GradientPaint(0, 0, accent, getWidth(), 0, new Color(160, 125, 30));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(new Color(20, 24, 36));
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try (Connection c = Koneksi.getConnection()) {
                if (c == null) return;

                // Transaksi & pendapatan hari ini
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                PreparedStatement ps1 = c.prepareStatement(
                    "SELECT COUNT(*) AS cnt, COALESCE(SUM(total_bayar),0) AS total " +
                    "FROM tb_penjualan WHERE DATE(tgl_transaksi)=?");
                ps1.setString(1, today);
                ResultSet r1 = ps1.executeQuery();
                if (r1.next()) {
                    lblTrxHariIni.setText(String.valueOf(r1.getInt("cnt")));
                    double tot = r1.getDouble("total");
                    lblPendapatanHariIni.setText(tot >= 1000000 ?
                            String.format("Rp %.1fJt", tot/1000000.0) :
                            String.format("Rp %,.0f", tot));
                }

                // Total barang
                ResultSet r2 = c.createStatement().executeQuery("SELECT COUNT(*) FROM tb_barang");
                if (r2.next()) lblTotalBarang.setText(String.valueOf(r2.getInt(1)));

                // Total customer
                ResultSet r3 = c.createStatement().executeQuery("SELECT COUNT(*) FROM tb_customer");
                if (r3.next()) lblTotalCustomer.setText(String.valueOf(r3.getInt(1)));

                // Stok kritis
                ResultSet r4 = c.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM tb_barang WHERE stok <= 5");
                if (r4.next()) {
                    int kritis = r4.getInt(1);
                    lblStokKritis.setText(String.valueOf(kritis));
                    lblStokKritis.setForeground(kritis > 0 ? WARN : SUCCESS);
                }

                // Total semua transaksi & pendapatan
                ResultSet r5 = c.createStatement().executeQuery(
                    "SELECT COUNT(*) AS cnt, COALESCE(SUM(total_bayar),0) AS total FROM tb_penjualan");
                if (r5.next()) {
                    lblTotalTrx.setText(String.valueOf(r5.getInt("cnt")));
                    double tot = r5.getDouble("total");
                    lblTotalPendapatan.setText(tot >= 1000000 ?
                            String.format("Rp %.1fJt", tot/1000000.0) :
                            String.format("Rp %,.0f", tot));
                }

                // Grafik 7 hari
                PreparedStatement ps6 = c.prepareStatement(
                    "SELECT DATE(tgl_transaksi) AS tgl, COALESCE(SUM(total_bayar),0) AS total " +
                    "FROM tb_penjualan WHERE tgl_transaksi >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                    "GROUP BY DATE(tgl_transaksi) ORDER BY tgl ASC");
                ResultSet r6 = ps6.executeQuery();
                java.util.Map<String, Double> dataMap = new java.util.LinkedHashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Calendar cal = java.util.Calendar.getInstance();
                for (int i = 6; i >= 0; i--) {
                    cal.setTime(new Date());
                    cal.add(java.util.Calendar.DAY_OF_MONTH, -i);
                    dataMap.put(sdf.format(cal.getTime()), 0.0);
                }
                while (r6.next()) {
                    dataMap.put(r6.getString("tgl"), r6.getDouble("total"));
                }
                int idx = 0;
                SimpleDateFormat sdfLabel = new SimpleDateFormat("EEE", new java.util.Locale("id","ID"));
                for (java.util.Map.Entry<String, Double> entry : dataMap.entrySet()) {
                    grafikData[idx]  = entry.getValue();
                    try {
                        grafikLabel[idx] = sdfLabel.format(sdf.parse(entry.getKey()));
                    } catch (Exception ignored) { grafikLabel[idx] = ""; }
                    idx++;
                }
                if (grafikPanel != null) grafikPanel.repaint();

            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }
}