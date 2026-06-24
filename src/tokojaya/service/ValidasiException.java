package tokojaya.service;

/**
 * Dilempar oleh service ketika input tidak valid. Pesan-nya sudah ramah-pengguna
 * sehingga UI cukup menampilkannya langsung di dialog/label status.
 */
public class ValidasiException extends Exception {
    public ValidasiException(String pesan) { super(pesan); }
}
