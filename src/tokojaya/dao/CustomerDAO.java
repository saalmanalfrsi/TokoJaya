package tokojaya.dao;

import tokojaya.koneksi.Koneksi;
import tokojaya.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_customer ORDER BY id_customer";
        try (Connection c = Koneksi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                    rs.getString("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("alamat"),
                    rs.getString("telepon"),
                    rs.getString("kategori")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean idSudahAda(String id) {
        String sql = "SELECT COUNT(*) FROM tb_customer WHERE id_customer = ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Generate ID customer berurutan dengan format CST001, CST002, ...
     * Mengambil angka terbesar dari ID berpola "CST###" lalu +1.
     */
    public String generateNextId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(id_customer, 4) AS UNSIGNED)) AS maks " +
                     "FROM tb_customer WHERE id_customer LIKE 'CST%'";
        int next = 1;
        try (Connection c = Koneksi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) next = rs.getInt("maks") + 1; // getInt(null) -> 0, jadi mulai 1
        } catch (Exception e) { e.printStackTrace(); }
        return String.format("CST%03d", next);
    }

    public boolean insert(Customer cu) {
        String sql = "INSERT INTO tb_customer (id_customer, nama_customer, alamat, telepon, kategori) " +
                     "VALUES (?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getIdCustomer());
            ps.setString(2, cu.getNamaCustomer());
            ps.setString(3, cu.getAlamat());
            ps.setString(4, cu.getTelepon());
            ps.setString(5, cu.getKategori());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean update(Customer cu) {
        String sql = "UPDATE tb_customer SET nama_customer=?, alamat=?, telepon=?, kategori=? " +
                     "WHERE id_customer=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getNamaCustomer());
            ps.setString(2, cu.getAlamat());
            ps.setString(3, cu.getTelepon());
            ps.setString(4, cu.getKategori());
            ps.setString(5, cu.getIdCustomer());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM tb_customer WHERE id_customer = ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
