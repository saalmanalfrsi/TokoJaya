package tokojaya.service;

import tokojaya.dao.BarangDAO;
import tokojaya.model.Barang;
import java.util.List;

/**
 * Logika bisnis barang: validasi field & angka, lalu delegasi ke {@link BarangDAO}.
 */
public class BarangService {

    private final BarangDAO dao = new BarangDAO();

    public List<Barang> getAll()           { return dao.getAll(); }
    public Barang getById(String id)        { return dao.getById(id); }

    public void simpan(Barang b) throws ValidasiException {
        validasi(b);
        if (dao.idSudahAda(b.getIdBarang())) {
            throw new ValidasiException("ID \"" + b.getIdBarang() + "\" sudah ada. Gunakan tombol Edit.");
        }
        if (!dao.insert(b)) throw new ValidasiException("Gagal menyimpan barang.");
    }

    public void ubah(Barang b) throws ValidasiException {
        validasi(b);
        if (!dao.update(b)) throw new ValidasiException("Gagal memperbarui barang.");
    }

    public void hapus(String id) throws ValidasiException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidasiException("Pilih barang dari tabel terlebih dahulu.");
        }
        if (!dao.delete(id)) {
            throw new ValidasiException("Gagal menghapus. Barang mungkin masih dipakai pada transaksi.");
        }
    }

    private void validasi(Barang b) throws ValidasiException {
        if (kosong(b.getIdBarang()))   throw new ValidasiException("ID barang wajib diisi.");
        if (kosong(b.getNamaBarang())) throw new ValidasiException("Nama barang wajib diisi.");
        if (kosong(b.getSatuan()))     throw new ValidasiException("Satuan wajib diisi.");
        if (b.getIdKategori() <= 0)    throw new ValidasiException("Kategori wajib dipilih.");
        if (b.getHargaJual() < 0)      throw new ValidasiException("Harga jual tidak boleh negatif.");
        if (b.getStok() < 0)           throw new ValidasiException("Stok tidak boleh negatif.");
    }

    private boolean kosong(String s) { return s == null || s.trim().isEmpty(); }
}
