package tokojaya.model;

import java.util.Date;

/**
 * Satu baris laporan penjualan (dikelompokkan per nota).
 */
public class LaporanRow {
    private final String noNota;
    private final Date   tanggal;
    private final String customer;
    private final int    totalQty;
    private final double total;       // total belanja nota
    private final double uangBayar;   // uang yang dibayar
    private final double kembalian;   // kembalian

    public LaporanRow(String noNota, Date tanggal, String customer,
                      int totalQty, double total, double uangBayar, double kembalian) {
        this.noNota    = noNota;
        this.tanggal   = tanggal;
        this.customer  = customer;
        this.totalQty  = totalQty;
        this.total     = total;
        this.uangBayar = uangBayar;
        this.kembalian = kembalian;
    }

    public String getNoNota()    { return noNota; }
    public Date   getTanggal()   { return tanggal; }
    public String getCustomer()  { return customer; }
    public int    getTotalQty()  { return totalQty; }
    public double getTotal()     { return total; }
    public double getUangBayar() { return uangBayar; }
    public double getKembalian() { return kembalian; }
}
