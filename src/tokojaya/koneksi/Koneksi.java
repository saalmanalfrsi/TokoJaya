package tokojaya.koneksi;

import java.sql.Connection;
import java.sql.DriverManager;

public class Koneksi {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB   = "db_toko_berkah";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Jakarta"
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8";

            conn = DriverManager.getConnection(url, USER, PASS);

        } catch (Exception e) {
            System.out.println("KONEKSI GAGAL: " + e.getMessage());
        }

        return conn;
    }
}