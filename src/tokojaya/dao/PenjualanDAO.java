package tokojaya.dao;

import tokojaya.koneksi.Koneksi;
import tokojaya.model.DetailPenjualan;
import tokojaya.model.LaporanRow;
import tokojaya.service.PeriodeLaporan;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PenjualanDAO {

    /**
     * Simpan SELURUH item satu nota + kurangi stok dalam satu transaksi atomik.
     * Bila salah satu langkah gagal, semua di-rollback (data tetap konsisten).
     *
     * @param noNota     nomor nota (sama untuk semua item)
     * @param tgl        waktu transaksi
     * @param idCustomer pelanggan
     * @param idUser     kasir
     * @param uangBayar  uang yang dibayarkan (per nota)
     * @param kembalian  kembalian (per nota)
     * @param items      daftar item
     */
    public boolean simpanNota(String noNota, Date tgl, String idCustomer, int idUser,
                              double uangBayar, double kembalian, List<DetailPenjualan> items) {

        String sqlInsert = "INSERT INTO tb_penjualan " +
                "(no_nota, tgl_transaksi, id_customer, id_barang, jumlah_beli, total_bayar, uang_bayar, kembalian, id_user) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        String sqlStok = "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";

        Connection c = null;
        try {
            c = Koneksi.getConnection();
            c.setAutoCommit(false);

            try (PreparedStatement psIns = c.prepareStatement(sqlInsert);
                 PreparedStatement psStk = c.prepareStatement(sqlStok)) {

                for (DetailPenjualan d : items) {
                    // 1. Insert baris item
                    psIns.setString(1, noNota);
                    psIns.setTimestamp(2, new Timestamp(tgl.getTime()));
                    psIns.setString(3, idCustomer);
                    psIns.setString(4, d.getIdBarang());
                    psIns.setInt(5, d.getJumlah());
                    psIns.setDouble(6, d.getSubtotal());
                    psIns.setDouble(7, uangBayar);   // disimpan sama di tiap baris (per nota)
                    psIns.setDouble(8, kembalian);
                    psIns.setInt(9, idUser);
                    psIns.executeUpdate();

                    // 2. Kurangi stok (syarat stok >= jumlah -> cegah stok minus)
                    psStk.setInt(1, d.getJumlah());
                    psStk.setString(2, d.getIdBarang());
                    psStk.setInt(3, d.getJumlah());
                    if (psStk.executeUpdate() == 0) {
                        c.rollback(); // stok tak cukup / barang hilang -> batalkan semua
                        return false;
                    }
                }
            }

            c.commit();
            return true;

        } catch (Exception e) {
            try { if (c != null) c.rollback(); } catch (Exception ex) { /* ignore */ }
            e.printStackTrace();
            return false;
        } finally {
            try { if (c != null) c.close(); } catch (Exception e) { /* ignore */ }
        }
    }

    /**
     * Ambil laporan penjualan dikelompokkan per NOTA, dengan filter periode.
     * Kolom uang_bayar & kembalian bernilai sama untuk satu nota sehingga
     * cukup diambil dengan MAX(...).
     */
    public List<LaporanRow> getLaporan(PeriodeLaporan periode) {
        List<LaporanRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(p.no_nota, CONCAT('OLD-', p.id_jual)) AS nota, " +
            "       MAX(p.tgl_transaksi) AS tgl, " +
            "       MAX(c.nama_customer) AS cust, " +
            "       SUM(p.jumlah_beli)   AS qty, " +
            "       SUM(p.total_bayar)   AS total, " +
            "       MAX(p.uang_bayar)    AS bayar, " +
            "       MAX(p.kembalian)     AS kembali " +
            "FROM tb_penjualan p " +
            "JOIN tb_customer c ON p.id_customer = c.id_customer ");

        if (periode != null && periode.getWhereClause() != null) {
            sql.append("WHERE ").append(periode.getWhereClause()).append(' ');
        }
        sql.append("GROUP BY nota ORDER BY tgl DESC");

        try (Connection c = Koneksi.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) {
                list.add(new LaporanRow(
                    rs.getString("nota"),
                    rs.getTimestamp("tgl"),
                    rs.getString("cust"),
                    rs.getInt("qty"),
                    rs.getDouble("total"),
                    rs.getDouble("bayar"),
                    rs.getDouble("kembali")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
