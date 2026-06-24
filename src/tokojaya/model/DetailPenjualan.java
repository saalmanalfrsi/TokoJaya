package tokojaya.model;

/**
 * Satu baris item dalam sebuah nota penjualan.
 * Dipakai untuk menyimpan transaksi multi-item secara atomik
 * (lihat {@link tokojaya.dao.PenjualanDAO#simpanNota}).
 */
public class DetailPenjualan {
    private final String idBarang;
    private final int    jumlah;
    private final double subtotal;   // harga satuan * jumlah

    public DetailPenjualan(String idBarang, int jumlah, double subtotal) {
        this.idBarang = idBarang;
        this.jumlah   = jumlah;
        this.subtotal = subtotal;
    }

    public String getIdBarang() { return idBarang; }
    public int    getJumlah()   { return jumlah; }
    public double getSubtotal() { return subtotal; }
}
