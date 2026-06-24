package tokojaya.model;

import java.util.Date;

public class Penjualan {
    private int    idJual;
    private String noNota;        // BARU: nomor nota (1 nota bisa beberapa baris item)
    private Date   tglTransaksi;
    private String idCustomer;
    private String idBarang;
    private int    jumlahBeli;
    private double totalBayar;    // subtotal baris (harga * jumlah)
    private double uangBayar;     // BARU: uang yang dibayarkan customer (per nota)
    private double kembalian;     // BARU: kembalian (per nota)
    private int    idUser;

    public Penjualan() {}

    public Penjualan(int idJual, String noNota, Date tglTransaksi, String idCustomer,
                     String idBarang, int jumlahBeli, double totalBayar,
                     double uangBayar, double kembalian, int idUser) {
        this.idJual       = idJual;
        this.noNota       = noNota;
        this.tglTransaksi = tglTransaksi;
        this.idCustomer   = idCustomer;
        this.idBarang     = idBarang;
        this.jumlahBeli   = jumlahBeli;
        this.totalBayar   = totalBayar;
        this.uangBayar    = uangBayar;
        this.kembalian    = kembalian;
        this.idUser       = idUser;
    }

    public int getIdJual()       { return idJual; }
    public void setIdJual(int v) { this.idJual = v; }

    public String getNoNota()       { return noNota; }
    public void setNoNota(String v) { this.noNota = v; }

    public Date getTglTransaksi()       { return tglTransaksi; }
    public void setTglTransaksi(Date v) { this.tglTransaksi = v; }

    public String getIdCustomer()       { return idCustomer; }
    public void setIdCustomer(String v) { this.idCustomer = v; }

    public String getIdBarang()       { return idBarang; }
    public void setIdBarang(String v) { this.idBarang = v; }

    public int getJumlahBeli()       { return jumlahBeli; }
    public void setJumlahBeli(int v) { this.jumlahBeli = v; }

    public double getTotalBayar()       { return totalBayar; }
    public void setTotalBayar(double v) { this.totalBayar = v; }

    public double getUangBayar()       { return uangBayar; }
    public void setUangBayar(double v) { this.uangBayar = v; }

    public double getKembalian()       { return kembalian; }
    public void setKembalian(double v) { this.kembalian = v; }

    public int getIdUser()       { return idUser; }
    public void setIdUser(int v) { this.idUser = v; }
}
