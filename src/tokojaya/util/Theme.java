package tokojaya.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Tema terpusat aplikasi — mendukung mode DARK (default) & LIGHT.
 *
 * <p>Warna "switchable" (background, kartu, teks, border) disimpan pada field
 * statis non-final yang ditimpa ulang saat mode berganti. Karena method
 * {@code paintComponent} pada panel membaca field ini secara langsung saat
 * menggambar, mengganti mode + {@code repaint()} otomatis memperbarui tampilan.
 *
 * <p>Warna aksen (GOLD, SUCCESS, DANGER, dll.) sengaja dibuat konstan agar
 * identitas visual tetap konsisten di kedua mode.
 */
public final class Theme {

    public enum Mode { DARK, LIGHT }

    private static Mode mode = Mode.DARK;
    private static final List<Runnable> listeners = new ArrayList<>();

    // ---- Warna yang berganti mengikuti mode ----
    public static Color BG;      // background utama window
    public static Color CARD;    // panel/kartu utama
    public static Color CARD2;   // baris tabel genap / kartu sekunder
    public static Color CARD3;   // header tabel / area input
    public static Color FIELD;   // background text field
    public static Color WHITE;   // teks primer (terang di dark, gelap di light)
    public static Color LIGHT;   // teks sekunder
    public static Color MUTED;   // teks redup / placeholder
    public static Color BORDER;  // garis pemisah
    public static Color ACTIVE;  // item navigasi aktif
    public static Color HOVER;   // item navigasi hover
    public static Color HEADER;  // background header/topbar

    // ---- Warna aksen (konstan di semua mode) ----
    public static final Color GOLD    = new Color(212, 175, 55);
    public static final Color GOLD2   = new Color(255, 215, 80);
    public static final Color SUCCESS = new Color(52, 211, 153);
    public static final Color DANGER  = new Color(251, 113, 133);
    public static final Color WARN    = new Color(251, 191, 36);
    public static final Color INFO    = new Color(130, 180, 255);

    static { applyPalette(); }

    private Theme() {}

    public static Mode getMode()    { return mode; }
    public static boolean isDark()  { return mode == Mode.DARK; }

    /** Ganti mode (dark <-> light) lalu beri tahu semua panel yang terdaftar. */
    public static void toggle() {
        mode = isDark() ? Mode.LIGHT : Mode.DARK;
        applyPalette();
        notifyListeners();
    }

    /** Daftarkan callback yang dipanggil setiap kali tema berganti. */
    public static void addListener(Runnable r) { if (r != null) listeners.add(r); }

    private static void notifyListeners() {
        // Salin dulu agar aman bila listener menambah/menghapus saat iterasi.
        for (Runnable r : new ArrayList<>(listeners)) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    private static void applyPalette() {
        if (mode == Mode.DARK) {
            BG     = new Color(15, 19, 27);
            CARD   = new Color(22, 28, 40);
            CARD2  = new Color(28, 36, 52);
            CARD3  = new Color(18, 24, 36);
            FIELD  = new Color(18, 24, 36);
            WHITE  = new Color(240, 244, 255);
            LIGHT  = new Color(180, 195, 220);
            MUTED  = new Color(100, 120, 155);
            BORDER = new Color(40, 52, 75);
            ACTIVE = new Color(30, 38, 58);
            HOVER  = new Color(24, 30, 45);
            HEADER = new Color(18, 24, 36);
        } else { // LIGHT
            BG     = new Color(236, 239, 244);
            CARD   = new Color(255, 255, 255);
            CARD2  = new Color(247, 249, 252);
            CARD3  = new Color(240, 243, 248);
            FIELD  = new Color(248, 250, 253);
            WHITE  = new Color(28, 34, 48);   // teks primer gelap
            LIGHT  = new Color(70, 82, 104);  // teks sekunder
            MUTED  = new Color(120, 132, 156);
            BORDER = new Color(214, 221, 232);
            ACTIVE = new Color(233, 238, 247);
            HOVER  = new Color(240, 243, 249);
            HEADER = new Color(243, 246, 250);
        }
    }

    /** Warna teks yang kontras di atas tombol beraksen terang (GOLD/SUCCESS). */
    public static Color onAccent() { return new Color(15, 20, 30); }

    /** Versi transparan dari sebuah warna (alpha 0-255). */
    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}
