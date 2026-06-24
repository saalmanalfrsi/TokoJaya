package tokojaya.model;

public class Customer {
    private String idCustomer;
    private String namaCustomer;
    private String alamat;
    private String telepon;
    private String kategori;   // BARU: kategori pelanggan (mis. Umum, Member, Grosir)

    public Customer() {}

    public Customer(String idCustomer, String namaCustomer,
                    String alamat, String telepon, String kategori) {
        this.idCustomer   = idCustomer;
        this.namaCustomer = namaCustomer;
        this.alamat       = alamat;
        this.telepon      = telepon;
        this.kategori     = kategori;
    }

    public String getIdCustomer()       { return idCustomer; }
    public void setIdCustomer(String v) { this.idCustomer = v; }

    public String getNamaCustomer()       { return namaCustomer; }
    public void setNamaCustomer(String v) { this.namaCustomer = v; }

    public String getAlamat()       { return alamat; }
    public void setAlamat(String v) { this.alamat = v; }

    public String getTelepon()       { return telepon; }
    public void setTelepon(String v) { this.telepon = v; }

    public String getKategori()       { return kategori; }
    public void setKategori(String v) { this.kategori = v; }

    @Override
    public String toString() { return idCustomer + " - " + namaCustomer; }
}
