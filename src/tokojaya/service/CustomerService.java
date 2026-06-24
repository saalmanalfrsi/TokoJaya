package tokojaya.service;

import tokojaya.dao.CustomerDAO;
import tokojaya.model.Customer;
import java.util.List;

/**
 * Logika bisnis customer:
 *  - generate ID otomatis berurutan (CST001, CST002, ...),
 *  - validasi field wajib tidak kosong,
 *  - LARANG kategori "anomali" (requirement khusus).
 */
public class CustomerService {

    private final CustomerDAO dao = new CustomerDAO();

    /** Kategori yang dilarang dipakai (case-insensitive). */
    public static final String KATEGORI_DILARANG = "anomali";

    /** Pilihan kategori valid yang ditawarkan di UI. */
    public static final String[] KATEGORI_VALID = {"Umum", "Member", "Grosir", "VIP"};

    public List<Customer> getAll()    { return dao.getAll(); }
    public String generateNextId()    { return dao.generateNextId(); }

    public void simpan(Customer cu) throws ValidasiException {
        validasi(cu, true);
        if (!dao.insert(cu)) {
            throw new ValidasiException("Gagal menyimpan. ID \"" + cu.getIdCustomer() + "\" mungkin sudah ada.");
        }
    }

    public void ubah(Customer cu) throws ValidasiException {
        validasi(cu, false);
        if (!dao.update(cu)) {
            throw new ValidasiException("Gagal memperbarui data customer.");
        }
    }

    public void hapus(String id) throws ValidasiException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidasiException("Pilih customer dari tabel terlebih dahulu.");
        }
        if (!dao.delete(id)) {
            throw new ValidasiException("Gagal menghapus. Customer mungkin masih memiliki transaksi.");
        }
    }

    /**
     * Aturan validasi customer.
     * @param cekIdBaru bila true, ID wajib unik (untuk operasi simpan/tambah).
     */
    private void validasi(Customer cu, boolean cekIdBaru) throws ValidasiException {
        if (kosong(cu.getIdCustomer()))   throw new ValidasiException("ID customer belum ter-generate.");
        if (kosong(cu.getNamaCustomer())) throw new ValidasiException("Nama customer wajib diisi.");
        if (kosong(cu.getAlamat()))       throw new ValidasiException("Alamat wajib diisi.");
        if (kosong(cu.getTelepon()))      throw new ValidasiException("No. telepon wajib diisi.");
        if (kosong(cu.getKategori()))     throw new ValidasiException("Kategori customer wajib dipilih.");

        // Larangan kategori "anomali"
        if (cu.getKategori().trim().equalsIgnoreCase(KATEGORI_DILARANG)) {
            throw new ValidasiException(
                "Kategori \"anomali\" tidak diperbolehkan.\nSilakan pilih kategori lain.");
        }
        // Telepon sebaiknya angka
        if (!cu.getTelepon().trim().matches("[0-9+\\-\\s]{6,20}")) {
            throw new ValidasiException("No. telepon tidak valid (6-20 digit angka).");
        }
        if (cekIdBaru && dao.idSudahAda(cu.getIdCustomer())) {
            throw new ValidasiException("ID \"" + cu.getIdCustomer() + "\" sudah dipakai.");
        }
    }

    private boolean kosong(String s) { return s == null || s.trim().isEmpty(); }
}
