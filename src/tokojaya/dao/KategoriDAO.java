package tokojaya.dao;

import tokojaya.koneksi.Koneksi;
import tokojaya.model.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Akses data tabel tb_kategori (kategori barang).
 * Sebelumnya kategori di-hardcode di PanelBarang — kini dinamis dari DB.
 */
public class KategoriDAO {

    public List<Kategori> getAll() {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT id_kategori, nama_kategori FROM tb_kategori ORDER BY nama_kategori";
        try (Connection c = Koneksi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kategori(rs.getInt("id_kategori"), rs.getString("nama_kategori")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Cek apakah nama kategori sudah ada (case-insensitive). */
    public boolean namaSudahAda(String nama, int kecualiId) {
        String sql = "SELECT COUNT(*) FROM tb_kategori WHERE LOWER(nama_kategori)=LOWER(?) AND id_kategori<>?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nama.trim());
            ps.setInt(2, kecualiId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Insert kategori baru; mengembalikan id yang ter-generate, atau -1 bila gagal. */
    public int insert(String nama) {
        String sql = "INSERT INTO tb_kategori (nama_kategori) VALUES (?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama.trim());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public boolean update(int id, String namaBaru) {
        String sql = "UPDATE tb_kategori SET nama_kategori=? WHERE id_kategori=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, namaBaru.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Hapus kategori. Gagal (mengembalikan false) bila masih dipakai barang. */
    public boolean delete(int id) {
        String sql = "DELETE FROM tb_kategori WHERE id_kategori=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Jumlah barang yang masih memakai kategori ini (untuk cek sebelum hapus). */
    public int jumlahBarangMemakai(int idKategori) {
        String sql = "SELECT COUNT(*) FROM tb_barang WHERE id_kategori=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idKategori);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}
