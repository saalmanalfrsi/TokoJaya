package tokojaya.form;

import tokojaya.model.Customer;
import tokojaya.service.CustomerService;
import tokojaya.service.ValidasiException;
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
 * Panel Data Customer.
 *
 * Fitur revisi:
 *  - ID otomatis berurutan (CST001, CST002, ...) — di-generate saat panel dibuka
 *    & setiap kali form direset/simpan.
 *  - Kategori customer (combo) dengan LARANGAN nilai "anomali".
 *  - Validasi field wajib (lewat {@link CustomerService}).
 *  - Pencarian realtime (TableRowSorter tunggal).
 */
public class PanelCustomer extends JPanel {

    private JTextField txtId, txtNama, txtTelepon, txtSearch;
    private JTextArea  txtAlamat;
    private JComboBox<String> cmbKategori;
    private JTable tabel;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblStatus, lblCount;
    private final boolean isAdmin;

    private final CustomerService service = new CustomerService();

    public PanelCustomer(boolean isAdmin) {
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

        loadData();
        if (isAdmin) generateId();   // ID otomatis saat form dibuka
    }

    private JPanel buatTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel tTitle = new JLabel("👤  Data Customer");
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

        txtId      = UIFactory.field("CST001");
        txtNama    = UIFactory.field("Nama lengkap");
        txtTelepon = UIFactory.field("08xxxxxxxxxx");
        txtId.setEditable(false);                 // ID otomatis
        UIFactory.themed(txtId, () -> txtId.setForeground(Theme.GOLD));

        cmbKategori = new JComboBox<>(CustomerService.KATEGORI_VALID);
        cmbKategori.setEditable(true);            // boleh ketik kategori sendiri
        UIFactory.styleCombo(cmbKategori);

        txtAlamat = new JTextArea(3, 10);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UIFactory.themed(txtAlamat, () -> {
            txtAlamat.setBackground(Theme.FIELD);
            txtAlamat.setForeground(Theme.WHITE);
            txtAlamat.setCaretColor(Theme.GOLD);
            txtAlamat.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
        });
        JScrollPane sAlamat = new JScrollPane(txtAlamat);
        sAlamat.setBorder(null);
        sAlamat.setPreferredSize(new Dimension(0, 72));

        if (!isAdmin) {
            readonly(txtNama); readonly(txtTelepon);
            cmbKategori.setEnabled(false);
            txtAlamat.setEditable(false);
        }

        int y = 0;
        gc.gridy = y++; gc.insets = new Insets(0, 0, 10, 0);
        form.add(UIFactory.sectionLabel("DATA PELANGGAN"), gc);

        gc.insets = new Insets(0, 0, 2, 0);
        gc.gridy = y++; form.add(UIFactory.fieldLabel("ID Customer (otomatis)"), gc);
        gc.gridy = y++; form.add(txtId, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Nama Customer"), gc);
        gc.gridy = y++; form.add(txtNama, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Kategori"), gc);
        gc.gridy = y++; form.add(cmbKategori, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("Alamat"), gc);
        gc.gridy = y++; form.add(sAlamat, gc);

        gc.gridy = y++; form.add(UIFactory.fieldLabel("No. Telepon"), gc);
        gc.gridy = y++; form.add(txtTelepon, gc);

        gc.gridy = y++; gc.insets = new Insets(14, 0, 0, 0);
        if (isAdmin) {
            JPanel btnGrid = new JPanel(new GridLayout(2, 2, 6, 6));
            btnGrid.setOpaque(false);
            JButton bS = UIFactory.button("＋ Simpan", Theme.SUCCESS);
            JButton bE = UIFactory.button("✎ Edit",   Theme.GOLD);
            JButton bH = UIFactory.button("✕ Hapus",  Theme.DANGER);
            JButton bR = UIFactory.ghostButton("↺ Baru", Theme.MUTED);
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

    private JPanel buatTabelKanan() {
        JPanel right = new JPanel(new BorderLayout());
        UIFactory.themed(right, () -> right.setBackground(Theme.BG));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        JLabel sIco = new JLabel("⌕  ");
        sIco.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch = UIFactory.field("Cari nama / ID / kategori customer...");
        searchRow.add(sIco, BorderLayout.WEST);
        searchRow.add(txtSearch, BorderLayout.CENTER);
        UIFactory.themed(searchRow, () -> {
            searchRow.setBackground(Theme.CARD);
            searchRow.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(10, 16, 10, 16)));
            sIco.setForeground(Theme.MUTED);
        });
        right.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Customer", "Kategori", "Alamat", "Telepon"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel = UIFactory.table(model);
        int[] ws = {80, 170, 90, 220, 120};
        for (int i = 0; i < ws.length; i++) tabel.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
        sorter = new TableRowSorter<>(model);
        tabel.setRowSorter(sorter);

        JScrollPane ts = new JScrollPane(tabel);
        ts.setBorder(null);
        UIFactory.themed(ts, () -> ts.getViewport().setBackground(Theme.CARD2));
        right.add(ts, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCount = new JLabel("0 customer");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        foot.add(lblCount);
        UIFactory.themed(foot, () -> {
            foot.setBackground(Theme.CARD);
            foot.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                    new EmptyBorder(7, 16, 7, 16)));
            lblCount.setForeground(Theme.MUTED);
        });
        right.add(foot, BorderLayout.SOUTH);

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = tabel.getSelectedRow();
            if (viewRow < 0) return;
            int row = tabel.convertRowIndexToModel(viewRow);
            txtId.setText(str(row, 0));
            txtNama.setText(str(row, 1));
            cmbKategori.setSelectedItem(str(row, 2));
            txtAlamat.setText(str(row, 3));
            txtTelepon.setText(str(row, 4));
        });

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
        lblCount.setText(tabel.getRowCount() + " customer");
    }

    private void loadData() {
        model.setRowCount(0);
        List<Customer> list = service.getAll();
        for (Customer c : list) {
            model.addRow(new Object[]{c.getIdCustomer(), c.getNamaCustomer(),
                    c.getKategori(), c.getAlamat(), c.getTelepon()});
        }
        if (lblCount != null) lblCount.setText(tabel.getRowCount() + " customer");
    }

    private void generateId() {
        txtId.setText(service.generateNextId());
    }

    private Customer bacaForm() throws ValidasiException {

    String nama = txtNama.getText().trim();

    if (!nama.matches("^[a-zA-Z\\s]+$")) {
        throw new ValidasiException(
                "Nama customer hanya boleh berisi huruf dan spasi!");
    }

    Object kat = cmbKategori.getSelectedItem();

    return new Customer(
            txtId.getText().trim(),
            nama,
            txtAlamat.getText().trim(),
            txtTelepon.getText().trim(),
            kat == null ? "" : kat.toString().trim());
}

    private void simpan() {
        try {
            service.simpan(bacaForm());
            status("✓ Customer disimpan!", Theme.SUCCESS);
            loadData(); reset();
        } catch (ValidasiException ex) { peringatan(ex.getMessage()); }
    }

    private void edit() {
        if (txtId.getText().trim().isEmpty()) { status("⚠ Pilih dari tabel!", Theme.WARN); return; }
        try {
            service.ubah(bacaForm());
            status("✓ Customer diperbarui!", Theme.SUCCESS);
            loadData(); reset();
        } catch (ValidasiException ex) { peringatan(ex.getMessage()); }
    }

    private void hapus() {
        if (txtId.getText().trim().isEmpty()) { status("⚠ Pilih dari tabel!", Theme.WARN); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Hapus \"" + txtNama.getText() + "\"?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            service.hapus(txtId.getText().trim());
            status("✓ Customer dihapus!", Theme.SUCCESS);
            loadData(); reset();
        } catch (ValidasiException ex) { peringatan(ex.getMessage()); }
    }

    private void reset() {
        txtNama.setText(""); txtAlamat.setText(""); txtTelepon.setText("");
        cmbKategori.setSelectedIndex(0);
        tabel.clearSelection();
        generateId();                 // siapkan ID berikutnya
        status("Form siap untuk customer baru", Theme.MUTED);
    }

    private void peringatan(String pesan) {
        status("⚠ " + pesan.replace("\n", " "), Theme.WARN);
        JOptionPane.showMessageDialog(this, pesan, "Validasi", JOptionPane.WARNING_MESSAGE);
    }

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
