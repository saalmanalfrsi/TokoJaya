package tokojaya.model;

public class Barang {
    private String idBarang;
    private int idKategori;
    private String namaBarang;
    private String satuan;
    private double hargaJual;
    private int stok;

    public Barang() {}

    public Barang(String idBarang, int idKategori, String namaBarang,
                  String satuan, double hargaJual, int stok) {
        this.idBarang   = idBarang;
        this.idKategori = idKategori;
        this.namaBarang = namaBarang;
        this.satuan     = satuan;
        this.hargaJual  = hargaJual;
        this.stok       = stok;
    }

    public String getIdBarang()       { return idBarang; }
    public void setIdBarang(String v) { this.idBarang = v; }

    public int getIdKategori()       { return idKategori; }
    public void setIdKategori(int v) { this.idKategori = v; }

    public String getNamaBarang()       { return namaBarang; }
    public void setNamaBarang(String v) { this.namaBarang = v; }

    public String getSatuan()       { return satuan; }
    public void setSatuan(String v) { this.satuan = v; }

    public double getHargaJual()       { return hargaJual; }
    public void setHargaJual(double v) { this.hargaJual = v; }

    public int getStok()       { return stok; }
    public void setStok(int v) { this.stok = v; }

    @Override
    public String toString() { return idBarang + " - " + namaBarang; }
}