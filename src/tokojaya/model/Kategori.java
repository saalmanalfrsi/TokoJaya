package tokojaya.model;

public class Kategori {
    private int    idKategori;
    private String namaKategori;

    public Kategori() {}
    public Kategori(int idKategori, String namaKategori) {
        this.idKategori   = idKategori;
        this.namaKategori = namaKategori;
    }

    public int    getIdKategori()            { return idKategori; }
    public void   setIdKategori(int v)       { this.idKategori = v; }
    public String getNamaKategori()          { return namaKategori; }
    public void   setNamaKategori(String v)  { this.namaKategori = v; }

    @Override
    public String toString() { return namaKategori; }
}