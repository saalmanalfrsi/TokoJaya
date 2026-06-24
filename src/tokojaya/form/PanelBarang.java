package tokojaya.form;

import tokojaya.model.Barang;
import tokojaya.model.Kategori;
import tokojaya.service.BarangService;
import tokojaya.service.KategoriService;
import tokojaya.service.ValidasiException;
import tokojaya.util.Rupiah;
import tokojaya.util.Theme;
import tokojaya.util.UIFactory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel Data Barang.
 *
 * Fitur revisi:
 *  - Kategori DINAMIS dari DB (tidak lagi hardcode), bisa tambah/kelola dari sini.
 *  - Validasi terpusat lewat {@link BarangService}.
 *  - Pencarian realtime memakai satu {@link TableRowSorter} (dibuat sekali).
 *  - Komponen UI reusable & theme-aware (mendukung Dark/Light).
 */
public class PanelBarang extends JPanel {

    private JTextField txtId, txtNama, txtSatuan, txtHarga, txtStok, txtSearch;
    private JComboBox<Kategori> cmbKat;
    private JTable tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblStatus, lblCount;
    private final boolean isAdmin;

    private final BarangService    barangSvc = new BarangService();
    private final KategoriService  katSvc    = new KategoriService();
    private List<Kategori> daftarKategori;

    public PanelBarang(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());
        UIFactory.themed(this, () -> setBackground(Theme.BG));

        add(buatTopbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(290);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setResizeWeight(0.0);
        split.setLeftComponent(buatFormKiri());
        split.setRightComponent(buatTabelKanan());
        add(split, BorderLayout.CENTER);

        loadDataPublic();
    }

    // ===================== TOPBAR =====================
    private JPanel buatTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel tTitle = new JLabel("📦  Data Barang");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus = new JLabel("Siap");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Theme.SUCCESS);
        top.add(tTitle, BorderLayout.WEST);
        top.add(lblStatus, BorderLayout.EAST);
        UIFactory.themed(top, () -> {
            top.setBackground(Theme.CARD);
            top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(12, 20, 12, 20)));
            tTitle.setForeground(Theme.WHITE);
        });
        return top;
    }

    // ===================== FORM KIRI =====================
    private JPanel buatFormKiri() {
        JPanel form = new JPanel(new GridBagLayout());
        UIFactory.themed(form, () -> {
            form.setBackground(Theme.CARD);
            form.setBorder(new EmptyBorder(16, 16, 16, 16));
        });

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;

        txtId     = UIFactory.field("Contoh: B004");
        txtNama   = UIFactory.field("Nama produk");
        txtSatuan = UIFactory.field("Pcs / Kg / Lusin");
        txtHarga  = UIFactory.field("Contoh: 15000");
        txtStok   = UIFactory.field("Jumlah stok");

        cmbKat = new JComboBox<>();
        UIFactory.styleCombo(cmbKat);
        muatKategori(null);

        if (!isAdmin) {
            readonly(txtId); readonly(txtNama); readonly(txtSatuan);
            readonly(txtHarga); readonly(txtStok); cmbKat.setEnabled(false);
        }

        int y = 0;
        gc.gridy = y++; gc.insets = new Insets(0, 0, 10, 0);
        form.add(UIFactory.sectionLabel("INFORMASI BARANG"), gc);

        gc.insets = new Insets(0, 0, 2, 0);
        gc.gridy = y++; form.add(UIFactory.fieldLabel("ID Barang"), gc);
        gc.gridy = y++; form.add(txtId, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Nama Barang"), gc);
        gc.gridy = y++; form.add(txtNama, gc);

        // Kategori + tombol tambah/kelola
        gc.gridy = y++; form.add(UIFactory.fieldLabel("Kategori"), gc);
        JPanel rowKat = new JPanel(new BorderLayout(6, 0));
        rowKat.setOpaque(false);
        rowKat.add(cmbKat, BorderLayout.CENTER);
        if (isAdmin) {
            JButton btnTambahKat = UIFactory.button("＋", Theme.SUCCESS);
            JButton btnKelolaKat = UIFactory.ghostButton("✎", Theme.GOLD);
            btnTambahKat.setPreferredSize(new Dimension(42, UIFactory.TINGGI_FIELD));
            btnKelolaKat.setPreferredSize(new Dimension(42, UIFactory.TINGGI_FIELD));
            btnTambahKat.setToolTipText("Tambah kategori baru");
            btnKelolaKat.setToolTipText("Kelola kategori (ubah/hapus)");
            JPanel katBtns = new JPanel(new GridLayout(1, 2, 4, 0));
            katBtns.setOpaque(false);
            katBtns.add(btnTambahKat); katBtns.add(btnKelolaKat);
            rowKat.add(katBtns, BorderLayout.EAST);
            btnTambahKat.addActionListener(e -> tambahKategoriCepat());
            btnKelolaKat.addActionListener(e -> kelolaKategori());
        }
        gc.gridy = y++; form.add(rowKat, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Satuan"), gc);
        gc.gridy = y++; form.add(txtSatuan, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Harga Jual (Rp)"), gc);
        gc.gridy = y++; form.add(txtHarga, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Stok"), gc);
        gc.gridy = y++; form.add(txtStok, gc);

        gc.gridy = y++; gc.insets = new Insets(14, 0, 0, 0);
        if (isAdmin) {
            JPanel btnGrid = new JPanel(new GridLayout(2, 2, 6, 6));
            btnGrid.setOpaque(false);
            JButton bS = UIFactory.button("＋ Simpan", Theme.SUCCESS);
            JButton bE = UIFactory.button("✎ Edit",   Theme.GOLD);
            JButton bH = UIFactory.button("✕ Hapus",  Theme.DANGER);
            JButton bR = UIFactory.ghostButton("↺ Reset", Theme.MUTED);
            btnGrid.add(bS); btnGrid.add(bE); btnGrid.add(bH); btnGrid.add(bR);
            form.add(btnGrid, gc);
            bS.addActionListener(e -> simpan());
            bE.addActionListener(e -> edit());
            bH.addActionListener(e -> hapus());
            bR.addActionListener(e -> reset());
        } else {
            form.add(infoBoxLihatSaja(), gc);
        }

        gc.gridy = y; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel spacer = new JPanel(); spacer.setOpaque(false);
        form.add(spacer, gc);

        JPanel outer = new JPanel(new BorderLayout());
        UIFactory.themed(outer, () -> outer.setBackground(Theme.CARD));
        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    // ===================== TABEL KANAN =====================
    private JPanel buatTabelKanan() {
        JPanel right = new JPanel(new BorderLayout());
        UIFactory.themed(right, () -> right.setBackground(Theme.BG));

        // Baris pencarian
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        JLabel sIco = new JLabel("⌕  ");
        sIco.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch = UIFactory.field("Cari nama / ID / kategori barang...");
        searchRow.add(sIco, BorderLayout.WEST);
        searchRow.add(txtSearch, BorderLayout.CENTER);
        UIFactory.themed(searchRow, () -> {
            searchRow.setBackground(Theme.CARD);
            searchRow.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(10, 16, 10, 16)));
            sIco.setForeground(Theme.MUTED);
        });
        right.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Barang", "Kategori", "Satuan", "Harga Jual", "Stok"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        // Pewarnaan kolom stok: 0=merah, <5=kuning, selebihnya hijau.
        tabel = UIFactory.table(model, (t, row, col) -> {
            if (col != 5) return null;
            try {
                int s = Integer.parseInt(t.getValueAt(row, 5).toString());
                return s == 0 ? Theme.DANGER : (s < 5 ? Theme.WARN : Theme.SUCCESS);
            } catch (Exception e) { return null; }
        });
        int[] ws = {70, 180, 110, 70, 120, 55};
        for (int i = 0; i < ws.length; i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        // Satu sorter dipakai ulang (bukan dibuat tiap ketik).
        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        JScrollPane ts = new JScrollPane(tabel);
        ts.setBorder(null);
        UIFactory.themed(ts, () -> ts.getViewport().setBackground(Theme.CARD2));
        right.add(ts, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCount = new JLabel("0 barang");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        foot.add(lblCount);
        UIFactory.themed(foot, () -> {
            foot.setBackground(Theme.CARD);
            foot.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                    new EmptyBorder(7, 16, 7, 16)));
            lblCount.setForeground(Theme.MUTED);
        });
        right.add(foot, BorderLayout.SOUTH);

        // Klik baris -> isi form
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = tabel.getSelectedRow();
            if (viewRow < 0) return;
            int row = tabel.convertRowIndexToModel(viewRow);
            isiFormDariBaris(row);
        });

        // Pencarian realtime (DocumentListener lebih andal dari KeyListener)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        return right;
    }

    private void filter() {
        String q = txtSearch.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
        lblCount.setText(tabel.getRowCount() + " barang");
    }

    private void isiFormDariBaris(int row) {
        txtId.setText(str(row, 0));
        txtNama.setText(str(row, 1));
        String kat = str(row, 2);
        for (int i = 0; i < cmbKat.getItemCount(); i++) {
            if (cmbKat.getItemAt(i).getNamaKategori().equals(kat)) { cmbKat.setSelectedIndex(i); break; }
        }
        txtSatuan.setText(str(row, 3));
        Barang b = barangSvc.getById(str(row, 0));
        if (b != null) txtHarga.setText(String.valueOf((long) b.getHargaJual()));
        txtStok.setText(str(row, 5));
    }

    // ===================== DATA =====================
    /** Dipanggil MainWindow saat tab dibuka / setelah transaksi. */
    public void loadDataPublic() {
        muatKategori(kategoriTerpilih());
        model.setRowCount(0);
        List<Barang> list = barangSvc.getAll();
        for (Barang b : list) {
            model.addRow(new Object[]{
                b.getIdBarang(), b.getNamaBarang(), namaKategori(b.getIdKategori()),
                b.getSatuan(), Rupiah.format(b.getHargaJual()), b.getStok()
            });
        }
        if (lblCount != null) lblCount.setText(tabel.getRowCount() + " barang");
    }

    private String namaKategori(int idKategori) {
        if (daftarKategori != null)
            for (Kategori k : daftarKategori)
                if (k.getIdKategori() == idKategori) return k.getNamaKategori();
        return "-";
    }

    private void muatKategori(Integer idTerpilih) {
        daftarKategori = katSvc.getAll();
        cmbKat.removeAllItems();
        for (Kategori k : daftarKategori) cmbKat.addItem(k);
        if (idTerpilih != null) {
            for (int i = 0; i < cmbKat.getItemCount(); i++)
                if (cmbKat.getItemAt(i).getIdKategori() == idTerpilih) { cmbKat.setSelectedIndex(i); return; }
        }
    }

    private Integer kategoriTerpilih() {
        Kategori k = (Kategori) cmbKat.getSelectedItem();
        return k == null ? null : k.getIdKategori();
    }

    // ===================== AKSI =====================
    private void simpan() {
        Barang b = bacaForm();
        if (b == null) return;
        try {
            barangSvc.simpan(b);
            status("✓ Barang disimpan!", Theme.SUCCESS);
            loadDataPublic(); reset();
        } catch (ValidasiException ex) { status("⚠ " + ex.getMessage(), Theme.WARN); }
    }

    private void edit() {
        if (txtId.getText().trim().isEmpty()) { status("⚠ Pilih dari tabel!", Theme.WARN); return; }
        Barang b = bacaForm();
        if (b == null) return;
        try {
            barangSvc.ubah(b);
            status("✓ Barang diperbarui!", Theme.SUCCESS);
            loadDataPublic(); reset();
        } catch (ValidasiException ex) { status("⚠ " + ex.getMessage(), Theme.WARN); }
    }

    private void hapus() {
        if (txtId.getText().trim().isEmpty()) { status("⚠ Pilih dari tabel!", Theme.WARN); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Hapus \"" + txtNama.getText() + "\"?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            barangSvc.hapus(txtId.getText().trim());
            status("✓ Barang dihapus!", Theme.SUCCESS);
            loadDataPublic(); reset();
        } catch (ValidasiException ex) { status("✕ " + ex.getMessage(), Theme.DANGER); }
    }

    /** Baca form menjadi objek Barang; null bila harga/stok bukan angka. */
    private Barang bacaForm() {
        Kategori kat = (Kategori) cmbKat.getSelectedItem();
        if (kat == null) { status("⚠ Kategori belum dipilih/ada. Tambah kategori dulu.", Theme.WARN); return null; }
        double harga = Rupiah.parse(txtHarga.getText());
        if (harga < 0) { status("⚠ Harga harus berupa angka!", Theme.WARN); return null; }
        int stok;
        try { stok = Integer.parseInt(txtStok.getText().trim()); }
        catch (NumberFormatException e) { status("⚠ Stok harus berupa angka!", Theme.WARN); return null; }
        return new Barang(txtId.getText().trim(), kat.getIdKategori(),
                txtNama.getText().trim(), txtSatuan.getText().trim(), harga, stok);
    }

    private void reset() {
        txtId.setText(""); txtNama.setText(""); txtSatuan.setText("");
        txtHarga.setText(""); txtStok.setText("");
        if (cmbKat.getItemCount() > 0) cmbKat.setSelectedIndex(0);
        tabel.clearSelection();
        status("Form direset", Theme.MUTED);
    }

    // ----- kategori cepat -----
    private void tambahKategoriCepat() {
        String nama = JOptionPane.showInputDialog(this, "Nama kategori baru:", "Tambah Kategori",
                JOptionPane.PLAIN_MESSAGE);
        if (nama == null) return; // dibatalkan
        try {
            Kategori k = katSvc.tambah(nama);
            muatKategori(k.getIdKategori());          // refresh & pilih kategori baru
            status("✓ Kategori \"" + k.getNamaKategori() + "\" ditambahkan!", Theme.SUCCESS);
        } catch (ValidasiException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Kategori", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void kelolaKategori() {
        DialogKategori dlg = new DialogKategori(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isAdaPerubahan()) {
            muatKategori(kategoriTerpilih());
            loadDataPublic();
            status("✓ Daftar kategori diperbarui", Theme.SUCCESS);
        }
    }

    // ===================== HELPERS =====================
    private JPanel infoBoxLihatSaja() {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.alpha(Theme.SUCCESS, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Theme.alpha(Theme.SUCCESS, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel l1 = new JLabel("🔒  Mode Lihat Saja");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l1.setForeground(Theme.SUCCESS); l1.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l2 = new JLabel("Petugas tidak dapat mengubah data");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        UIFactory.themed(l2, () -> l2.setForeground(Theme.MUTED));
        l2.setAlignmentX(LEFT_ALIGNMENT);
        box.add(l1); box.add(Box.createVerticalStrut(3)); box.add(l2);
        return box;
    }

    private void readonly(JTextField f) {
        f.setEditable(false);
        f.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        UIFactory.themed(f, () -> f.setForeground(Theme.MUTED));
    }

    private void status(String m, Color c) {
        if (lblStatus != null) { lblStatus.setText(m); lblStatus.setForeground(c); }
    }

    private String str(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }
}
