package tokojaya.form;

import tokojaya.model.LaporanRow;
import tokojaya.service.LaporanService;
import tokojaya.service.PeriodeLaporan;
import tokojaya.util.PdfExporter;
import tokojaya.util.Rupiah;
import tokojaya.util.Theme;
import tokojaya.util.UIFactory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel Laporan Penjualan (dikelompokkan per nota).
 *
 * Fitur revisi:
 *  - Kolom KEMBALIAN & UANG BAYAR.
 *  - Filter periode: Hari ini / Minggu ini / Bulan ini / 1 bulan terakhir / Semua.
 *  - Export & cetak PDF profesional (OpenPDF).
 *  - Pencarian realtime (TableRowSorter tunggal).
 */
public class PanelLaporan extends JPanel {

    private JTable tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<PeriodeLaporan> cmbPeriode;
    private JLabel lblCount, lblTotal;
    private final JLabel[] statVal = new JLabel[3];

    private final LaporanService service = new LaporanService();
    private List<LaporanRow> dataSekarang;   // data hasil filter terakhir (untuk PDF)

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yy HH:mm");

    public PanelLaporan() {
        setLayout(new BorderLayout());
        UIFactory.themed(this, () -> setBackground(Theme.BG));

        add(buatHeader(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);

        loadData();
    }

    private JComponent buatHeader() {
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);

        // Baris judul + kontrol
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("📊  Laporan Penjualan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel kontrol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kontrol.setOpaque(false);

        cmbPeriode = new JComboBox<>(PeriodeLaporan.values());
        UIFactory.styleCombo(cmbPeriode);
        cmbPeriode.setPreferredSize(new Dimension(160, UIFactory.TINGGI_FIELD));

        txtSearch = UIFactory.field("Cari nota / customer...");
        txtSearch.setPreferredSize(new Dimension(180, UIFactory.TINGGI_FIELD));

        JButton btnRefresh = UIFactory.ghostButton("↺ Refresh", Theme.GOLD);
        JButton btnPdf     = UIFactory.button("🖨 Export PDF", Theme.GOLD);
        btnRefresh.setPreferredSize(new Dimension(110, UIFactory.TINGGI_FIELD));
        btnPdf.setPreferredSize(new Dimension(140, UIFactory.TINGGI_FIELD));

        kontrol.add(new JLabel("Periode:") {{ setForeground(Theme.MUTED);
            setFont(new Font("Segoe UI", Font.PLAIN, 11)); }});
        kontrol.add(cmbPeriode);
        kontrol.add(txtSearch);
        kontrol.add(btnRefresh);
        kontrol.add(btnPdf);

        top.add(title, BorderLayout.WEST);
        top.add(kontrol, BorderLayout.EAST);
        UIFactory.themed(top, () -> {
            top.setBackground(Theme.CARD);
            top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(12, 22, 12, 22)));
            title.setForeground(Theme.WHITE);
        });

        // Kartu statistik
        JPanel stats = new JPanel(new GridLayout(1, 3, 1, 0));
        UIFactory.themed(stats, () -> {
            stats.setBackground(Theme.BORDER);
            stats.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        });
        stats.add(statCard("TOTAL TRANSAKSI",  Theme.INFO,    0));
        stats.add(statCard("TOTAL PENDAPATAN", Theme.GOLD,    1));
        stats.add(statCard("RATA-RATA / TRX",  Theme.SUCCESS, 2));

        headerWrap.add(top, BorderLayout.NORTH);
        headerWrap.add(stats, BorderLayout.SOUTH);

        // Events
        cmbPeriode.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> loadData());
        btnPdf.addActionListener(e -> exportPdf());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        return headerWrap;
    }

    private JComponent buatTabel() {
        String[] cols = {"No", "No. Nota", "Tanggal", "Customer", "Qty", "Total", "Bayar", "Kembalian"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        // Warnai kolom Total (emas) & Kembalian (hijau).
        tabel = UIFactory.table(model, (t, row, col) -> {
            if (col == 5) return Theme.GOLD;
            if (col == 7) return Theme.SUCCESS;
            return null;
        });
        tabel.setRowHeight(36);
        int[] ws = {40, 150, 110, 150, 45, 120, 120, 120};
        for (int i = 0; i < ws.length; i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i : new int[]{4, 5, 6, 7}) tabel.getColumnModel().getColumn(i).setCellRenderer(kanan);

        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        JScrollPane sc = new JScrollPane(tabel);
        sc.setBorder(null);
        UIFactory.themed(sc, () -> sc.getViewport().setBackground(Theme.CARD2));
        return sc;
    }

    private JComponent buatFooter() {
        JPanel foot = new JPanel(new BorderLayout());
        lblCount = new JLabel("0 transaksi");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal = new JLabel("Total: Rp 0");
        lblTotal.setFont(new Font("Consolas", Font.BOLD, 14));
        foot.add(lblCount, BorderLayout.WEST);
        foot.add(lblTotal, BorderLayout.EAST);
        UIFactory.themed(foot, () -> {
            foot.setBackground(Theme.CARD);
            foot.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                    new EmptyBorder(12, 22, 12, 22)));
            lblCount.setForeground(Theme.MUTED);
            lblTotal.setForeground(Theme.GOLD2);
        });
        return foot;
    }

    private JPanel statCard(String label, Color accent, int idx) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(Theme.alpha(accent, 200));
        JLabel val = new JLabel("-");
        val.setFont(new Font("Consolas", Font.BOLD, 18));
        statVal[idx] = val;
        card.add(lbl); card.add(val);
        UIFactory.themed(card, () -> {
            card.setBackground(Theme.CARD3);
            card.setBorder(new EmptyBorder(14, 22, 14, 22));
            val.setForeground(Theme.WHITE);
        });
        return card;
    }

    // ===================== DATA =====================
    private PeriodeLaporan periodeTerpilih() {
        Object p = cmbPeriode.getSelectedItem();
        return (p instanceof PeriodeLaporan) ? (PeriodeLaporan) p : PeriodeLaporan.SEMUA;
    }

    private void loadData() {
        dataSekarang = service.getLaporan(periodeTerpilih());
        model.setRowCount(0);
        int no = 1;
        for (LaporanRow r : dataSekarang) {
            model.addRow(new Object[]{
                no++,
                r.getNoNota(),
                r.getTanggal() == null ? "-" : SDF.format(r.getTanggal()),
                r.getCustomer(),
                r.getTotalQty(),
                Rupiah.format(r.getTotal()),
                Rupiah.format(r.getUangBayar()),
                Rupiah.format(r.getKembalian())
            });
        }
        LaporanService.Ringkasan rk = service.hitungRingkasan(dataSekarang);
        statVal[0].setText(String.valueOf(rk.jumlahTransaksi));
        statVal[1].setText(Rupiah.format(rk.totalPendapatan));
        statVal[2].setText(Rupiah.format(rk.rataRata));
        lblTotal.setText("Total Pendapatan: " + Rupiah.format(rk.totalPendapatan));
        filter();
    }

    private void filter() {
        String q = txtSearch.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
        lblCount.setText(tabel.getRowCount() + " transaksi");
    }

    // ===================== PDF =====================
    private void exportPdf() {
        if (dataSekarang == null || dataSekarang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diexport pada periode ini.",
                    "Export PDF", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan PDF");
        String namaDefault = "Laporan_Penjualan_" +
                new SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date()) + ".pdf";
        chooser.setSelectedFile(new File(namaDefault));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }
        try {
            PdfExporter.exportLaporan(file, dataSekarang, periodeTerpilih(),
                    service.hitungRingkasan(dataSekarang));
            int buka = JOptionPane.showConfirmDialog(this,
                    "PDF berhasil disimpan:\n" + file.getAbsolutePath() + "\n\nBuka sekarang?",
                    "Export Berhasil", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (buka == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Dipanggil MainWindow agar laporan ter-refresh saat tab dibuka. */
    public void refreshData() { loadData(); }
}
