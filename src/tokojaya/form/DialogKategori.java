package tokojaya.form;

import tokojaya.model.Kategori;
import tokojaya.service.KategoriService;
import tokojaya.service.ValidasiException;
import tokojaya.util.Theme;
import tokojaya.util.UIFactory;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog kelola kategori barang: tambah, ubah nama, hapus.
 * Dipanggil dari PanelBarang. Setelah ditutup, pemanggil me-refresh combo kategori.
 */
public class DialogKategori extends JDialog {

    private final KategoriService service = new KategoriService();
    private final DefaultListModel<Kategori> listModel = new DefaultListModel<>();
    private final JList<Kategori> list = new JList<>(listModel);
    private boolean adaPerubahan = false;

    public DialogKategori(Window parent) {
        super(parent, "Kelola Kategori Barang", ModalityType.APPLICATION_MODAL);
        setSize(420, 460);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(Theme.CARD);

        JLabel judul = new JLabel("📂  Kategori Barang");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 15));
        judul.setForeground(Theme.WHITE);
        root.add(judul, BorderLayout.NORTH);

        // Daftar kategori
        list.setBackground(Theme.FIELD);
        list.setForeground(Theme.WHITE);
        list.setSelectionBackground(Theme.alpha(Theme.GOLD, 60));
        list.setSelectionForeground(Theme.WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setFixedCellHeight(30);
        list.setBorder(new EmptyBorder(4, 6, 4, 6));
        JScrollPane sc = new JScrollPane(list);
        sc.setBorder(new LineBorder(Theme.BORDER, 1, true));
        root.add(sc, BorderLayout.CENTER);

        // Panel input + tombol
        JPanel bawah = new JPanel(new BorderLayout(0, 10));
        bawah.setOpaque(false);

        JTextField txtNama = UIFactory.field("Nama kategori baru...");
        JButton btnTambah = UIFactory.button("＋ Tambah", Theme.SUCCESS);
        JPanel rowTambah = new JPanel(new BorderLayout(8, 0));
        rowTambah.setOpaque(false);
        rowTambah.add(txtNama, BorderLayout.CENTER);
        btnTambah.setPreferredSize(new Dimension(120, UIFactory.TINGGI_FIELD));
        rowTambah.add(btnTambah, BorderLayout.EAST);

        JButton btnUbah  = UIFactory.ghostButton("✎ Ubah Nama", Theme.GOLD);
        JButton btnHapus = UIFactory.ghostButton("✕ Hapus", Theme.DANGER);
        JButton btnTutup = UIFactory.ghostButton("Tutup", Theme.LIGHT);
        JPanel rowAksi = new JPanel(new GridLayout(1, 3, 8, 0));
        rowAksi.setOpaque(false);
        rowAksi.add(btnUbah); rowAksi.add(btnHapus); rowAksi.add(btnTutup);

        bawah.add(rowTambah, BorderLayout.NORTH);
        bawah.add(rowAksi, BorderLayout.SOUTH);
        root.add(bawah, BorderLayout.SOUTH);
        setContentPane(root);

        // Aksi
        btnTambah.addActionListener(e -> {
            try {
                Kategori k = service.tambah(txtNama.getText());
                txtNama.setText("");
                adaPerubahan = true;
                muat();
                pilih(k);
            } catch (ValidasiException ex) { pesan(ex.getMessage(), JOptionPane.WARNING_MESSAGE); }
        });
        btnUbah.addActionListener(e -> {
            Kategori k = list.getSelectedValue();
            if (k == null) { pesan("Pilih kategori yang akan diubah.", JOptionPane.WARNING_MESSAGE); return; }
            String baru = JOptionPane.showInputDialog(this, "Ubah nama kategori:", k.getNamaKategori());
            if (baru == null) return;
            try {
                service.ubah(k.getIdKategori(), baru);
                adaPerubahan = true;
                muat();
            } catch (ValidasiException ex) { pesan(ex.getMessage(), JOptionPane.WARNING_MESSAGE); }
        });
        btnHapus.addActionListener(e -> {
            Kategori k = list.getSelectedValue();
            if (k == null) { pesan("Pilih kategori yang akan dihapus.", JOptionPane.WARNING_MESSAGE); return; }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Hapus kategori \"" + k.getNamaKategori() + "\"?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                service.hapus(k.getIdKategori());
                adaPerubahan = true;
                muat();
            } catch (ValidasiException ex) { pesan(ex.getMessage(), JOptionPane.WARNING_MESSAGE); }
        });
        btnTutup.addActionListener(e -> dispose());

        muat();
    }

    private void muat() {
        listModel.clear();
        List<Kategori> all = service.getAll();
        for (Kategori k : all) listModel.addElement(k);
    }

    private void pilih(Kategori k) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getIdKategori() == k.getIdKategori()) { list.setSelectedIndex(i); break; }
        }
    }

    private void pesan(String m, int tipe) {
        JOptionPane.showMessageDialog(this, m, "Kategori", tipe);
    }

    /** True bila ada penambahan/ubah/hapus — agar pemanggil refresh combo. */
    public boolean isAdaPerubahan() { return adaPerubahan; }
}
