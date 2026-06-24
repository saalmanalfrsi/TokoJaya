package tokojaya.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility format & parsing nilai Rupiah agar konsisten di seluruh aplikasi
 * (menghindari duplikasi {@code String.format("Rp %,.0f", ...)} yang tersebar).
 */
public final class Rupiah {

    private static final DecimalFormat DF;
    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("id", "ID"));
        sym.setGroupingSeparator('.');
        DF = new DecimalFormat("#,##0", sym);
    }

    private Rupiah() {}

    /** 15000  ->  "Rp 15.000" */
    public static String format(double v) { return "Rp " + DF.format(v); }

    /** 15000  ->  "15.000" (tanpa prefix, untuk input field) */
    public static String angka(double v) { return DF.format(v); }

    /**
     * Ubah teks user ("Rp 15.000", "15000", "15.000") menjadi double.
     * Mengembalikan {@code -1} bila tidak valid.
     */
    public static double parse(String teks) {
        if (teks == null) return -1;
        String bersih = teks.replaceAll("[^0-9]", "");
        if (bersih.isEmpty()) return -1;
        try {
            return Double.parseDouble(bersih);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
