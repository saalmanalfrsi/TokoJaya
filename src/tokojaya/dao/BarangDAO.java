package tokojaya.dao;

import tokojaya.koneksi.Koneksi;
import tokojaya.model.Barang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    public List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_barang";
        try (Connection c = Koneksi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Barang(
                    rs.getString("id_barang"),
                    rs.getInt("id_kategori"),
                    rs.getString("nama_barang"),
                    rs.getString("satuan"),
                    rs.getDouble("harga_jual"),
                    rs.getInt("stok")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public Barang getById(String id) {
        Barang b = null;
        String sql = "SELECT * FROM tb_barang WHERE id_barang = ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                b = new Barang(
                    rs.getString("id_barang"),
                    rs.getInt("id_kategori"),
                    rs.getString("nama_barang"),
                    rs.getString("satuan"),
                    rs.getDouble("harga_jual"),
                    rs.getInt("stok")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return b;
    }

    // CEK apakah ID sudah ada di database
    public boolean idSudahAda(String id) {
        String sql = "SELECT COUNT(*) FROM tb_barang WHERE id_barang = ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean insert(Barang b) {
        String sql = "INSERT INTO tb_barang VALUES (?,?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getIdBarang());
            ps.setInt(2, b.getIdKategori());
            ps.setString(3, b.getNamaBarang());
            ps.setString(4, b.getSatuan());
            ps.setDouble(5, b.getHargaJual());
            ps.setInt(6, b.getStok());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean update(Barang b) {
        String sql = "UPDATE tb_barang SET id_kategori=?, nama_barang=?, " +
                     "satuan=?, harga_jual=?, stok=? WHERE id_barang=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getIdKategori());
            ps.setString(2, b.getNamaBarang());
            ps.setString(3, b.getSatuan());
            ps.setDouble(4, b.getHargaJual());
            ps.setInt(5, b.getStok());
            ps.setString(6, b.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM tb_barang WHERE id_barang = ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean kurangiStok(String idBarang, int jumlah, Connection c) {
        String sql = "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, idBarang);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}