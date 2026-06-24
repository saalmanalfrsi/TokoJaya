package tokojaya.form;

import tokojaya.dao.BarangDAO;
import tokojaya.dao.CustomerDAO;
import tokojaya.model.Barang;
import tokojaya.model.Customer;
import tokojaya.model.DetailPenjualan;
import tokojaya.service.TransaksiService;
import tokojaya.service.ValidasiException;
import tokojaya.util.Rupiah;
import tokojaya.util.Theme;
import tokojaya.util.UIFactory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Transaksi / Kasir.
 *
 * Fitur revisi:
 *  - Input UANG BAYAR dengan perhitungan KEMBALIAN realtime.
 *  - Tombol nominal cepat (uang pas / pecahan umum).
 *  - Validasi uang bayar < total.
 *  - Penyimpanan atomik per-nota (no_nota, uang_bayar, kembalian) via service.
 *  - Tampilan lebih rapi & theme-aware.
 */
public class PanelTransaksi extends JPanel {

    private JComboBox<Customer> cmbCustomer;
    private JComboBox<Barang>   cmbBarang;
    private JTextField txtHarga, txtStok, txtJumlah, txtBayar;
    private JLabel lblBar, lblBarSt, lblStatus;
    private JLabel lblSubtotal, lblTotal, lblItemCount, lblKembalian;

    private JTable tabelKeranjang;
    private DefaultTableModel modelKeranjang;
    private final List<ItemKeranjang> keranjang = new ArrayList<>();

    private final int idUser;
    private final MainWindow mainWindow;

    private final BarangDAO        bDAO  = new BarangDAO();
    private final CustomerDAO      cDAO  = new CustomerDAO();
    private final TransaksiService trxSvc = new TransaksiService();

    /** Item dalam keranjang. Dipakai juga oleh Dialog* (akses field package-private). */
    public static class ItemKeranjang {
        String idBarang, namaBarang, satuan;
        double harga;
        int jumlah, stokMax;

        ItemKeranjang(String idBarang, String namaBarang, String satuan,
                      double harga, int jumlah, int stokMax) {
            this.idBarang = idBarang; this.namaBarang = namaBarang; this.satuan = satuan;
            this.harga = harga; this.jumlah = jumlah; this.stokMax = stokMax;
        }
        double subtotal() { return harga * jumlah; }
    }

    public PanelTransaksi(int idUser, MainWindow mainWindow) {
        this.idUser = idUser;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        UIFactory.themed(this, () -> setBackground(Theme.BG));

        add(buatTopbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setResizeWeight(0.35);
        split.setLeftComponent(buatPanelInput());
        split.setRightComponent(buatPanelKeranjang());
        add(split, BorderLayout.CENTER);

        if (cmbBarang.getItemCount() > 0) {
            cmbBarang.setSelectedIndex(0);
            updateBarang();
        }
        updateTotal();
    }

    private JPanel buatTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel tTitle = new JLabel("🧾  Transaksi Penjualan");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus = new JLabel("Siap");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Theme.SUCCESS);
        String tgl = new java.text.SimpleDateFormat("EEE, dd MMM yyyy",
                new java.util.Locale("id", "ID")).format(new java.util.Date());
        JLabel lTgl = new JLabel(tgl);
        lTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel tr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tr.setOpaque(false);
        tr.add(lTgl); tr.add(lblStatus);
        top.add(tTitle, BorderLayout.WEST);
        top.add(tr, BorderLayout.EAST);
        UIFactory.themed(top, () -> {
            top.setBackground(Theme.CARD);
            top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(12, 20, 12, 20)));
            tTitle.setForeground(Theme.WHITE);
            lTgl.setForeground(Theme.MUTED);
        });
        return top;
    }

    // ===================== PANEL KIRI: input item =====================
    private JPanel buatPanelInput() {
        JPanel wrap = new JPanel(new BorderLayout());
        UIFactory.themed(wrap, () -> wrap.setBackground(Theme.CARD));

        JPanel header = new JPanel(new BorderLayout());
        JLabel h = new JLabel("＋  Tambah Item");
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.add(h, BorderLayout.WEST);
        UIFactory.themed(header, () -> {
            header.setBackground(Theme.CARD3);
            header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(10, 16, 10, 16)));
            h.setForeground(Theme.WHITE);
        });
        wrap.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        UIFactory.themed(form, () -> {
            form.setBackground(Theme.CARD);
            form.setBorder(new EmptyBorder(16, 16, 16, 16));
        });
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0; gc.gridx = 0;

        cmbCustomer = new JComboBox<>();
        UIFactory.styleCombo(cmbCustomer);
        for (Customer c : cDAO.getAll()) cmbCustomer.addItem(c);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 2, 0);
        form.add(UIFactory.sectionLabel("CUSTOMER"), gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 14, 0);
        form.add(cmbCustomer, gc);

        cmbBarang = new JComboBox<>();
        UIFactory.styleCombo(cmbBarang);
        for (Barang b : bDAO.getAll()) cmbBarang.addItem(b);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 2, 0);
        form.add(UIFactory.sectionLabel("PILIH BARANG"), gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 10, 0);
        form.add(cmbBarang, gc);

        txtHarga = UIFactory.field("—"); txtHarga.setEditable(false);
        txtHarga.setFont(new Font("Consolas", Font.BOLD, 13));
        UIFactory.themed(txtHarga, () -> txtHarga.setForeground(Theme.GOLD2));
        txtStok = UIFactory.field("—"); txtStok.setEditable(false);
        JPanel rowHS = new JPanel(new GridLayout(1, 2, 8, 0));
        rowHS.setOpaque(false);
        rowHS.add(groupField("Harga Satuan", txtHarga));
        rowHS.add(groupField("Stok Tersedia", txtStok));
        gc.gridy = 4; gc.insets = new Insets(0, 0, 6, 0);
        form.add(rowHS, gc);

        lblBar = new JLabel("░░░░░░░░░░");
        lblBar.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblBarSt = new JLabel("");
        lblBarSt.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JPanel stRow = new JPanel(new BorderLayout(4, 0));
        stRow.setOpaque(false);
        stRow.add(lblBar, BorderLayout.CENTER);
        stRow.add(lblBarSt, BorderLayout.EAST);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        form.add(stRow, gc);

        txtJumlah = UIFactory.field("1"); txtJumlah.setText("1");
        gc.gridy = 6; gc.insets = new Insets(0, 0, 2, 0);
        form.add(UIFactory.fieldLabel("Jumlah Beli"), gc);
        gc.gridy = 7; gc.insets = new Insets(0, 0, 14, 0);
        form.add(txtJumlah, gc);

        JButton btnTambah = UIFactory.button("＋  Tambah ke Keranjang", Theme.SUCCESS);
        btnTambah.setPreferredSize(new Dimension(0, 44));
        gc.gridy = 8; gc.insets = new Insets(0, 0, 0, 0);
        form.add(btnTambah, gc);

        gc.gridy = 9; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        form.add(spacer, gc);

        wrap.add(form, BorderLayout.CENTER);

        cmbBarang.addActionListener(e -> updateBarang());
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        return wrap;
    }

    // ===================== PANEL KANAN: keranjang + bayar =====================
    private JPanel buatPanelKeranjang() {
        JPanel wrap = new JPanel(new BorderLayout());
        UIFactory.themed(wrap, () -> wrap.setBackground(Theme.BG));

        // Header keranjang
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("🛒  Keranjang Belanja");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblItemCount = new JLabel("0 item");
        lblItemCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JButton btnHapusItem = UIFactory.ghostButton("✕ Hapus Item", Theme.DANGER);
        btnHapusItem.setPreferredSize(new Dimension(120, 30));
        JPanel hr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        hr.setOpaque(false);
        hr.add(lblItemCount); hr.add(btnHapusItem);
        header.add(title, BorderLayout.WEST);
        header.add(hr, BorderLayout.EAST);
        UIFactory.themed(header, () -> {
            header.setBackground(Theme.CARD);
            header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(10, 16, 10, 16)));
            title.setForeground(Theme.WHITE);
            lblItemCount.setForeground(Theme.MUTED);
        });
        wrap.add(header, BorderLayout.NORTH);

        // Tabel keranjang
        String[] cols = {"Nama Barang", "Satuan", "Harga", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; } // hanya Qty
        };
        tabelKeranjang = UIFactory.table(modelKeranjang, (t, row, col) -> col == 4 ? Theme.GOLD : null);
        int[] ws = {200, 65, 120, 50, 130};
        for (int i = 0; i < ws.length; i++) tabelKeranjang.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tabelKeranjang.getColumnModel().getColumn(2).setCellRenderer(kanan);
        tabelKeranjang.getColumnModel().getColumn(4).setCellRenderer(kanan);

        JScrollPane sc = new JScrollPane(tabelKeranjang);
        sc.setBorder(null);
        UIFactory.themed(sc, () -> sc.getViewport().setBackground(Theme.CARD2));
        wrap.add(sc, BorderLayout.CENTER);

        wrap.add(buatPanelBayar(), BorderLayout.SOUTH);

        // Events
        btnHapusItem.addActionListener(e -> hapusItemTerpilih());
        modelKeranjang.addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                editQty(e.getFirstRow());
            }
        });
        return wrap;
    }

    private JPanel buatPanelBayar() {
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        UIFactory.themed(bottom, () -> {
            bottom.setBackground(Theme.CARD);
            bottom.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                    new EmptyBorder(14, 16, 14, 16)));
        });

        // Ringkasan angka
        JPanel ringkas = new JPanel(new GridBagLayout());
        ringkas.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.gridx = 0; g.insets = new Insets(2, 0, 2, 0);

        lblSubtotal  = nilai("Rp 0", 12, false);
        lblTotal     = nilai("Rp 0", 22, true);
        lblKembalian = nilai("Rp 0", 14, false);

        int y = 0;
        g.gridy = y++; ringkas.add(barisRingkas("Subtotal", lblSubtotal), g);
        g.gridy = y++; ringkas.add(barisRingkas("Total Item", lblItemCountSummary()), g);
        g.gridy = y++; ringkas.add(sep(), g);
        g.gridy = y++; ringkas.add(barisRingkas("TOTAL BELANJA", lblTotal), g);

        // Input uang bayar + tombol cepat
        txtBayar = UIFactory.field("Masukkan uang bayar...");
        txtBayar.setFont(new Font("Consolas", Font.BOLD, 14));
        JPanel bayarRow = new JPanel(new BorderLayout(8, 0));
        bayarRow.setOpaque(false);
        bayarRow.add(UIFactory.fieldLabel("UANG BAYAR"), BorderLayout.NORTH);
        bayarRow.add(txtBayar, BorderLayout.CENTER);

        JPanel cepat = new JPanel(new GridLayout(1, 4, 6, 0));
        cepat.setOpaque(false);
        JButton bPas = UIFactory.ghostButton("Uang Pas", Theme.SUCCESS);
        JButton b50  = UIFactory.ghostButton("50rb", Theme.LIGHT);
        JButton b100 = UIFactory.ghostButton("100rb", Theme.LIGHT);
        JButton b200 = UIFactory.ghostButton("200rb", Theme.LIGHT);
        for (JButton b : new JButton[]{bPas, b50, b100, b200}) b.setPreferredSize(new Dimension(0, 30));
        cepat.add(bPas); cepat.add(b50); cepat.add(b100); cepat.add(b200);

        g.gridy = y++; g.insets = new Insets(10, 0, 4, 0); ringkas.add(bayarRow, g);
        g.gridy = y++; g.insets = new Insets(0, 0, 6, 0);  ringkas.add(cepat, g);
        g.gridy = y++; g.insets = new Insets(2, 0, 2, 0);  ringkas.add(barisRingkas("KEMBALIAN", lblKembalian), g);

        bottom.add(ringkas, BorderLayout.CENTER);

        // Tombol aksi
        JPanel aksi = new JPanel(new GridLayout(1, 3, 8, 0));
        aksi.setOpaque(false);
        aksi.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnBersih = UIFactory.ghostButton("🗑 Bersihkan", Theme.DANGER);
        JButton btnBayar  = UIFactory.button("💳 Proses Bayar", Theme.GOLD);
        JButton btnCetak  = UIFactory.ghostButton("🖨 Cetak Struk", Theme.LIGHT);
        for (JButton b : new JButton[]{btnBersih, btnBayar, btnCetak}) b.setPreferredSize(new Dimension(0, 42));
        aksi.add(btnBersih); aksi.add(btnBayar); aksi.add(btnCetak);
        bottom.add(aksi, BorderLayout.SOUTH);

        // Events
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { hitungKembalian(); }
            public void removeUpdate(DocumentEvent e)  { hitungKembalian(); }
            public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });
        bPas.addActionListener(e -> { txtBayar.setText(String.valueOf((long) totalKeranjang())); });
        b50.addActionListener(e  -> txtBayar.setText("50000"));
        b100.addActionListener(e -> txtBayar.setText("100000"));
        b200.addActionListener(e -> txtBayar.setText("200000"));
        btnBersih.addActionListener(e -> bersihkanKeranjang());
        btnBayar.addActionListener(e  -> prosesBayar());
        btnCetak.addActionListener(e  -> cetakStruk());

        return bottom;
    }

    // Label "Total Item" pakai komponen sendiri yang disimpan agar bisa diupdate.
    private JLabel lblTotalItem;
    private JLabel lblItemCountSummary() {
        lblTotalItem = nilai("0 item", 12, false);
        return lblTotalItem;
    }

    // ===================== LOGIKA KERANJANG =====================
    private void tambahKeKeranjang() {
        Barang b = (Barang) cmbBarang.getSelectedItem();
        if (b == null) { status("⚠ Pilih barang!", Theme.WARN); return; }
        int jumlah;
        try {
            jumlah = Integer.parseInt(txtJumlah.getText().trim());
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { status("⚠ Jumlah harus angka positif!", Theme.WARN); return; }

        Barang fresh = bDAO.getById(b.getIdBarang());
        int stokDb = (fresh != null) ? fresh.getStok() : b.getStok();
        if (stokDb == 0) {
            JOptionPane.showMessageDialog(this, "Stok barang HABIS!", "Stok", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idx = -1;
        for (int i = 0; i < keranjang.size(); i++)
            if (keranjang.get(i).idBarang.equals(b.getIdBarang())) { idx = i; break; }

        if (idx >= 0) {
            int totalBaru = keranjang.get(idx).jumlah + jumlah;
            if (totalBaru > stokDb) {
                JOptionPane.showMessageDialog(this,
                        "Stok tidak cukup!\nTersedia: " + stokDb + ", di keranjang: " +
                        keranjang.get(idx).jumlah, "Stok", JOptionPane.WARNING_MESSAGE);
                return;
            }
            keranjang.get(idx).jumlah = totalBaru;
            keranjang.get(idx).stokMax = stokDb;
            modelKeranjang.setValueAt(totalBaru, idx, 3);
            modelKeranjang.setValueAt(Rupiah.format(keranjang.get(idx).subtotal()), idx, 4);
        } else {
            if (jumlah > stokDb) {
                JOptionPane.showMessageDialog(this, "Stok tidak cukup!\nTersedia: " + stokDb,
                        "Stok", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ItemKeranjang item = new ItemKeranjang(b.getIdBarang(), b.getNamaBarang(),
                    fresh != null ? fresh.getSatuan() : b.getSatuan(), b.getHargaJual(), jumlah, stokDb);
            keranjang.add(item);
            modelKeranjang.addRow(new Object[]{item.namaBarang, item.satuan,
                    Rupiah.format(item.harga), item.jumlah, Rupiah.format(item.subtotal())});
        }
        updateTotal();
        txtJumlah.setText("1");
        status("✓ " + b.getNamaBarang() + " ditambahkan!", Theme.SUCCESS);
    }

    private void editQty(int row) {
        if (row < 0 || row >= keranjang.size()) return;
        try {
            int qty = Integer.parseInt(modelKeranjang.getValueAt(row, 3).toString().trim());
            if (qty <= 0) { hapusItem(row); return; }
            if (qty > keranjang.get(row).stokMax) {
                JOptionPane.showMessageDialog(this, "Stok maksimal: " + keranjang.get(row).stokMax,
                        "Stok", JOptionPane.WARNING_MESSAGE);
                modelKeranjang.setValueAt(keranjang.get(row).jumlah, row, 3);
                return;
            }
            keranjang.get(row).jumlah = qty;
            modelKeranjang.setValueAt(Rupiah.format(keranjang.get(row).subtotal()), row, 4);
            updateTotal();
        } catch (NumberFormatException ex) {
            modelKeranjang.setValueAt(keranjang.get(row).jumlah, row, 3);
        }
    }

    private void hapusItemTerpilih() {
        int row = tabelKeranjang.getSelectedRow();
        if (row < 0) { status("⚠ Pilih item yang ingin dihapus!", Theme.WARN); return; }
        hapusItem(row);
    }

    private void hapusItem(int row) {
        if (row < 0 || row >= keranjang.size()) return;
        String nama = keranjang.get(row).namaBarang;
        keranjang.remove(row);
        modelKeranjang.removeRow(row);
        updateTotal();
        status("✓ " + nama + " dihapus dari keranjang", Theme.MUTED);
    }

    private void bersihkanKeranjang() {
        if (keranjang.isEmpty()) return;
        int ok = JOptionPane.showConfirmDialog(this, "Bersihkan semua item?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        keranjang.clear();
        modelKeranjang.setRowCount(0);
        txtBayar.setText("");
        updateTotal();
        status("Keranjang dikosongkan", Theme.MUTED);
    }

    private double totalKeranjang() {
        return keranjang.stream().mapToDouble(ItemKeranjang::subtotal).sum();
    }

    private void updateTotal() {
        double total = totalKeranjang();
        int totalItem = keranjang.stream().mapToInt(i -> i.jumlah).sum();
        lblSubtotal.setText(Rupiah.format(total));
        lblTotal.setText(Rupiah.format(total));
        lblItemCount.setText(keranjang.size() + " item");
        if (lblTotalItem != null) lblTotalItem.setText(totalItem + " item");
        hitungKembalian();
    }

    /** Hitung & tampilkan kembalian secara realtime. */
    private void hitungKembalian() {
        double total = totalKeranjang();
        double bayar = Rupiah.parse(txtBayar.getText());
        if (bayar < 0) bayar = 0;
        double kembali = bayar - total;
        if (kembali < 0) {
            lblKembalian.setText("Kurang " + Rupiah.format(-kembali));
            lblKembalian.setForeground(Theme.DANGER);
        } else {
            lblKembalian.setText(Rupiah.format(kembali));
            lblKembalian.setForeground(Theme.SUCCESS);
        }
    }

    // ===================== PROSES BAYAR =====================
    private void prosesBayar() {
        if (keranjang.isEmpty()) {
            new DialogPesan((Frame) SwingUtilities.getWindowAncestor(this), "Keranjang Kosong",
                    "Tambahkan barang ke keranjang dulu.", DialogPesan.Tipe.WARN).tampilkan();
            return;
        }
        Customer cu = (Customer) cmbCustomer.getSelectedItem();
        double total = totalKeranjang();
        double bayar = Rupiah.parse(txtBayar.getText());
        if (bayar < 0) bayar = 0;

        // Konfirmasi (dialog menampilkan item, customer, total)
        DialogKonfirmasi.Hasil hasil = new DialogKonfirmasi(
                (Frame) SwingUtilities.getWindowAncestor(this), "Konfirmasi Pembayaran",
                new ArrayList<>(keranjang), cu != null ? cu.getNamaCustomer() : "-", total).tampilkan();
        if (hasil != DialogKonfirmasi.Hasil.YA) return;

        // Bangun detail & simpan via service (validasi bayar/stok di dalamnya)
        List<DetailPenjualan> detail = new ArrayList<>();
        for (ItemKeranjang i : keranjang)
            detail.add(new DetailPenjualan(i.idBarang, i.jumlah, i.subtotal()));

        try {
            String idCust = cu != null ? cu.getIdCustomer() : null;
            trxSvc.simpan(idCust, idUser, total, bayar, detail);

            double kembali = bayar - total;
            status("✓ Transaksi berhasil!", Theme.SUCCESS);

            // Snapshot untuk struk sebelum keranjang dikosongkan
            List<ItemKeranjang> snapshot = new ArrayList<>(keranjang);
            String snapCust = cu != null ? cu.getNamaCustomer() : "-";

            keranjang.clear();
            modelKeranjang.setRowCount(0);
            txtBayar.setText("");
            updateTotal();
            refreshDaftarBarang();
            if (mainWindow != null) mainWindow.refreshPanelBarang();

            new DialogSukses((Frame) SwingUtilities.getWindowAncestor(this),
                    snapCust, snapshot, total).tampilkan();

            int cetak = JOptionPane.showConfirmDialog(this,
                    "Kembalian: " + Rupiah.format(kembali) + "\n\nCetak struk belanja?",
                    "Struk", JOptionPane.YES_NO_OPTION);
            if (cetak == JOptionPane.YES_OPTION) {
                new DialogStruk((Frame) SwingUtilities.getWindowAncestor(this),
                        snapshot, snapCust, total).tampilkan();
            }
        } catch (ValidasiException ex) {
            new DialogPesan((Frame) SwingUtilities.getWindowAncestor(this), "Transaksi Gagal",
                    ex.getMessage(), DialogPesan.Tipe.WARN).tampilkan();
        }
    }

    private void cetakStruk() {
        if (keranjang.isEmpty()) {
            new DialogPesan((Frame) SwingUtilities.getWindowAncestor(this), "Keranjang Kosong",
                    "Tidak ada item untuk dicetak.", DialogPesan.Tipe.WARN).tampilkan();
            return;
        }
        Customer cu = (Customer) cmbCustomer.getSelectedItem();
        new DialogStruk((Frame) SwingUtilities.getWindowAncestor(this),
                new ArrayList<>(keranjang), cu != null ? cu.getNamaCustomer() : "-",
                totalKeranjang()).tampilkan();
    }

    // ===================== BARANG/STOK =====================
    private void updateBarang() {
        Barang b = (Barang) cmbBarang.getSelectedItem();
        if (b == null) return;
        Barang fresh = bDAO.getById(b.getIdBarang());
        if (fresh != null) b = fresh;
        txtHarga.setText(Rupiah.format(b.getHargaJual()));
        txtStok.setText(b.getStok() + " unit");
        int s = b.getStok();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(i < Math.min(10, s) ? "▓" : "░");
        lblBar.setText(sb.toString());
        if (s == 0)       { lblBar.setForeground(Theme.DANGER);  lblBarSt.setText("HABIS");    lblBarSt.setForeground(Theme.DANGER); }
        else if (s < 5)   { lblBar.setForeground(Theme.WARN);    lblBarSt.setText("KRITIS");   lblBarSt.setForeground(Theme.WARN); }
        else              { lblBar.setForeground(Theme.SUCCESS); lblBarSt.setText("TERSEDIA"); lblBarSt.setForeground(Theme.SUCCESS); }
    }

    /** Muat ulang combo barang & customer (mis. setelah transaksi / pindah tab). */
    public void refreshDaftarBarang() {
        Barang sebelum = (Barang) cmbBarang.getSelectedItem();
        cmbBarang.removeAllItems();
        for (Barang b : bDAO.getAll()) cmbBarang.addItem(b);
        if (sebelum != null) {
            for (int i = 0; i < cmbBarang.getItemCount(); i++)
                if (cmbBarang.getItemAt(i).getIdBarang().equals(sebelum.getIdBarang())) {
                    cmbBarang.setSelectedIndex(i); break;
                }
        }
        // Refresh daftar customer juga (mungkin ada penambahan)
        Customer cSebelum = (Customer) cmbCustomer.getSelectedItem();
        cmbCustomer.removeAllItems();
        for (Customer c : cDAO.getAll()) cmbCustomer.addItem(c);
        if (cSebelum != null) {
            for (int i = 0; i < cmbCustomer.getItemCount(); i++)
                if (cmbCustomer.getItemAt(i).getIdCustomer().equals(cSebelum.getIdCustomer())) {
                    cmbCustomer.setSelectedIndex(i); break;
                }
        }
        updateBarang();
    }

    // ===================== HELPERS UI =====================
    private JPanel groupField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(UIFactory.fieldLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JLabel nilai(String teks, int size, boolean total) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Consolas", total ? Font.BOLD : Font.PLAIN, size));
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        UIFactory.themed(l, () -> l.setForeground(total ? Theme.GOLD2 : Theme.LIGHT));
        return l;
    }

    private JPanel barisRingkas(String label, JLabel nilai) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        boolean tebal = label.startsWith("TOTAL") || label.equals("KEMBALIAN") || label.equals("UANG BAYAR");
        l.setFont(new Font("Segoe UI", tebal ? Font.BOLD : Font.PLAIN, tebal ? 13 : 12));
        UIFactory.themed(l, () -> l.setForeground(tebal ? Theme.WHITE : Theme.MUTED));
        p.add(l, BorderLayout.WEST);
        p.add(nilai, BorderLayout.EAST);
        return p;
    }

    private JComponent sep() {
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(0, 1));
        UIFactory.themed(s, () -> s.setBackground(Theme.BORDER));
        return s;
    }

    private void status(String m, Color c) {
        if (lblStatus != null) { lblStatus.setText(m); lblStatus.setForeground(c); }
    }
}
