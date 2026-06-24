package tokojaya.service;

import tokojaya.dao.BarangDAO;
import tokojaya.dao.PenjualanDAO;
import tokojaya.model.Barang;
import tokojaya.model.DetailPenjualan;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Logika bisnis transaksi/kasir:
 *  - hitung kembalian,
 *  - validasi uang bayar cukup,
 *  - validasi stok terkini,
 *  - simpan nota secara atomik (generate nomor nota otomatis).
 */
public class TransaksiService {

    private final BarangDAO    barangDAO = new BarangDAO();
    private final PenjualanDAO jualDAO   = new PenjualanDAO();

    /** Kembalian = uang bayar - total (bisa negatif bila kurang). */
    public double hitungKembalian(double total, double uangBayar) {
        return uangBayar - total;
    }

    /** Nomor nota unik, contoh: INV-20260609-1530-482 */
    public String generateNoNota() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());
        int rnd = (int) (Math.random() * 900) + 100;
        return "INV-" + stamp + "-" + rnd;
    }

    /**
     * Simpan transaksi. Mengembalikan nomor nota bila sukses.
     * @throws ValidasiException bila keranjang kosong, customer kosong,
     *         uang kurang, atau stok berubah/tidak cukup.
     */
    public String simpan(String idCustomer, int idUser, double total, double uangBayar,
                         List<DetailPenjualan> items) throws ValidasiException {

        if (items == null || items.isEmpty()) {
            throw new ValidasiException("Keranjang masih kosong.");
        }
        if (idCustomer == null || idCustomer.trim().isEmpty()) {
            throw new ValidasiException("Silakan pilih customer terlebih dahulu.");
        }
        if (uangBayar < total) {
            throw new ValidasiException(
                "Uang bayar kurang dari total belanja.\nKekurangan: " +
                tokojaya.util.Rupiah.format(total - uangBayar));
        }

        // Validasi stok terkini (mencegah oversell akibat data berubah)
        for (DetailPenjualan d : items) {
            Barang fresh = barangDAO.getById(d.getIdBarang());
            int stok = (fresh != null) ? fresh.getStok() : 0;
            if (d.getJumlah() > stok) {
                String nama = (fresh != null) ? fresh.getNamaBarang() : d.getIdBarang();
                throw new ValidasiException(
                    "Stok \"" + nama + "\" tidak cukup.\nTersedia: " + stok +
                    ", diminta: " + d.getJumlah() + ".");
            }
        }

        String noNota    = generateNoNota();
        double kembalian = hitungKembalian(total, uangBayar);

        boolean ok = jualDAO.simpanNota(noNota, new Date(), idCustomer, idUser,
                                        uangBayar, kembalian, items);
        if (!ok) {
            throw new ValidasiException("Gagal menyimpan transaksi (stok berubah). Silakan ulangi.");
        }
        return noNota;
    }
}
