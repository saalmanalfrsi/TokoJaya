package tokojaya.service;

import tokojaya.dao.KategoriDAO;
import tokojaya.model.Kategori;
import java.util.List;

/**
 * Logika bisnis kategori barang: validasi nama tidak kosong / tidak duplikat,
 * lalu mendelegasikan penyimpanan ke {@link KategoriDAO}.
 */
public class KategoriService {

    private final KategoriDAO dao = new KategoriDAO();

    public List<Kategori> getAll() { return dao.getAll(); }

    /** Tambah kategori baru. Mengembalikan objek kategori yang tersimpan. */
    public Kategori tambah(String nama) throws ValidasiException {
        String n = bersihkan(nama);
        if (dao.namaSudahAda(n, -1)) {
            throw new ValidasiException("Kategori \"" + n + "\" sudah ada.");
        }
        int id = dao.insert(n);
        if (id < 0) throw new ValidasiException("Gagal menyimpan kategori ke database.");
        return new Kategori(id, n);
    }

    /** Ubah nama kategori yang sudah ada. */
    public void ubah(int idKategori, String namaBaru) throws ValidasiException {
        String n = bersihkan(namaBaru);
        if (dao.namaSudahAda(n, idKategori)) {
            throw new ValidasiException("Kategori \"" + n + "\" sudah dipakai kategori lain.");
        }
        if (!dao.update(idKategori, n)) {
            throw new ValidasiException("Gagal memperbarui kategori.");
        }
    }

    /** Hapus kategori; ditolak bila masih dipakai barang. */
    public void hapus(int idKategori) throws ValidasiException {
        int dipakai = dao.jumlahBarangMemakai(idKategori);
        if (dipakai > 0) {
            throw new ValidasiException(
                "Kategori tidak bisa dihapus karena masih dipakai oleh " + dipakai + " barang.");
        }
        if (!dao.delete(idKategori)) {
            throw new ValidasiException("Gagal menghapus kategori.");
        }
    }

    /** Validasi dasar: tidak kosong & panjang wajar. */
    private String bersihkan(String nama) throws ValidasiException {
        String n = (nama == null) ? "" : nama.trim();
        if (n.isEmpty()) {
            throw new ValidasiException("Nama kategori tidak boleh kosong.");
        }
        if (n.length() > 50) {
            throw new ValidasiException("Nama kategori terlalu panjang (maks 50 karakter).");
        }
        return n;
    }
}
