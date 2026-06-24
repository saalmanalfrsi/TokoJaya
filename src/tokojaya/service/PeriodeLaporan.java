package tokojaya.service;

/**
 * Pilihan periode filter laporan. Setiap nilai menyediakan potongan
 * klausa SQL WHERE (berbasis kolom tgl_transaksi) sehingga filter
 * dilakukan di sisi database (lebih cepat & akurat).
 */
public enum PeriodeLaporan {

    SEMUA          ("Semua Waktu",      null),
    HARI_INI       ("Hari Ini",         "DATE(p.tgl_transaksi) = CURDATE()"),
    // Minggu berjalan (Senin–Minggu) sesuai pengaturan default MySQL mode 1.
    MINGGU_INI     ("Minggu Ini",       "YEARWEEK(p.tgl_transaksi, 1) = YEARWEEK(CURDATE(), 1)"),
    BULAN_INI      ("Bulan Ini",        "YEAR(p.tgl_transaksi)=YEAR(CURDATE()) AND MONTH(p.tgl_transaksi)=MONTH(CURDATE())"),
    BULAN_TERAKHIR ("1 Bulan Terakhir", "p.tgl_transaksi >= DATE_SUB(NOW(), INTERVAL 1 MONTH)");

    private final String label;
    private final String whereClause;

    PeriodeLaporan(String label, String whereClause) {
        this.label = label;
        this.whereClause = whereClause;
    }

    public String getLabel()       { return label; }
    /** Klausa WHERE (tanpa kata "WHERE"), atau null bila tanpa filter. */
    public String getWhereClause() { return whereClause; }

    @Override
    public String toString() { return label; }
}
