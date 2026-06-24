package tokojaya.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Pabrik komponen UI yang dipakai bersama semua panel.
 * Fix: font emoji support untuk Windows (Segoe UI Emoji).
 */
public final class UIFactory {

    private UIFactory() {}

    // ===== FIX UTAMA: Font yang support emoji =====
    private static final Font EMOJI_FONT;
    private static final Font FONT;
    private static final Font FONT_BOLD;

    static {
        // Cari font terbaik yang support emoji di OS ini
        String[] candidates = {
            "Segoe UI Emoji",    // Windows 8+  ← ini yang dibutuhkan
            "Segoe UI Symbol",   // Windows 7
            "Noto Color Emoji",  // Linux
            "Apple Color Emoji", // macOS
        };

        Font found = null;
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, 13);
            // Kalau nama font tidak dikenali, Java fallback ke "Dialog"
            if (!f.getFamily().equalsIgnoreCase("Dialog")) {
                found = f;
                break;
            }
        }

        // Kalau tidak ada emoji font, fallback ke Segoe UI biasa
        EMOJI_FONT = (found != null) ? found : new Font("Segoe UI", Font.PLAIN, 13);
        FONT       = new Font("Segoe UI", Font.PLAIN,  12);
        FONT_BOLD  = new Font("Segoe UI", Font.BOLD,   12);
    }

    /** Font yang support emoji — pakai untuk label/button yang mengandung icon. */
    public static Font emojiFont(int size, int style) {
        return EMOJI_FONT.deriveFont(style, (float) size);
    }

    // Tinggi standar komponen agar konsisten di seluruh aplikasi.
    public static final int TINGGI_FIELD  = 36;
    public static final int TINGGI_TOMBOL = 38;

    /** Jalankan applier sekarang & setiap kali tema berganti. */
    public static <T extends JComponent> T themed(T comp, Runnable applier) {
        applier.run();
        Theme.addListener(() -> { applier.run(); comp.repaint(); });
        return comp;
    }

    // ===================== TEXT FIELD =====================

    public static JTextField field(String placeholder) {
        JTextField tx = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Theme.alpha(Theme.MUTED, 130));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 11, getHeight() / 2 + 5);
                }
            }
        };
        tx.setFont(FONT);
        tx.setCaretColor(Theme.GOLD);
        tx.setPreferredSize(new Dimension(0, TINGGI_FIELD));
        Runnable apply = () -> {
            tx.setBackground(Theme.FIELD);
            tx.setForeground(Theme.WHITE);
            tx.setCaretColor(Theme.GOLD);
            tx.setBorder(borderNormal());
        };
        themed(tx, apply);
        tx.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tx.setBorder(borderFokus()); }
            @Override public void focusLost(FocusEvent e)   { tx.setBorder(borderNormal()); }
        });
        return tx;
    }

    private static Border borderNormal() {
        return new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(6, 11, 6, 11));
    }
    private static Border borderFokus() {
        return new CompoundBorder(
            new LineBorder(Theme.alpha(Theme.GOLD, 160), 2, true),
            new EmptyBorder(5, 10, 5, 10));
    }

    // ===================== TOMBOL =====================

    /**
     * Tombol utama dengan aksen warna penuh.
     * Teks yang mengandung emoji otomatis pakai font emoji.
     */
    public static JButton button(String teks, Color aksen) {
        JButton b = new JButton(teks) {
            private boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hov ? aksen.brighter() : aksen;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        // Pakai emoji font supaya icon tampil benar
        b.setFont(hasEmoji(teks) ? emojiFont(12, Font.BOLD) : FONT_BOLD);
        b.setForeground(Theme.onAccent());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 14, 0, 14));
        b.setPreferredSize(new Dimension(0, TINGGI_TOMBOL));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Tombol outline/ghost — transparan dengan border tipis. */
    public static JButton ghostButton(String teks, Color aksenTeks) {
        JButton b = new JButton(teks) {
            private boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Theme.alpha(aksenTeks, 40) : Theme.CARD2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Theme.alpha(aksenTeks, hov ? 170 : 90));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
        };
        b.setFont(hasEmoji(teks) ? emojiFont(12, Font.BOLD) : FONT_BOLD);
        b.setForeground(aksenTeks);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 14, 0, 14));
        b.setPreferredSize(new Dimension(0, TINGGI_TOMBOL));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ===================== TABEL =====================

    public interface CellColor {
        Color fg(JTable t, int row, int col);
    }

    public static JTable table(DefaultTableModel model) {
        return table(model, null);
    }

    public static JTable table(DefaultTableModel model, CellColor colorizer) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row);
                c.setBackground(sel ? Theme.alpha(Theme.GOLD, 45)
                    : (row % 2 == 0 ? Theme.CARD2 : Theme.CARD));
                c.setForeground(sel ? Theme.WHITE : Theme.LIGHT);
                if (!sel && colorizer != null) {
                    Color o = colorizer.fg(this, row, col);
                    if (o != null) c.setForeground(o);
                }
                return c;
            }
        };
        Runnable apply = () -> {
            t.setBackground(Theme.CARD2);
            t.setForeground(Theme.LIGHT);
            t.setGridColor(Theme.alpha(Theme.isDark() ? Color.WHITE : Color.BLACK, 12));
            t.setSelectionBackground(Theme.alpha(Theme.GOLD, 50));
            JTableHeader th = t.getTableHeader();
            th.setBackground(Theme.HEADER);
            th.setForeground(Theme.MUTED);
        };
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setFont(FONT);
        t.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader th = t.getTableHeader();
        th.setFont(FONT_BOLD.deriveFont(10f));
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);
        themed(t, apply);
        return t;
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(FONT);
        c.setPreferredSize(new Dimension(0, TINGGI_FIELD));
        themed(c, () -> {
            c.setBackground(Theme.FIELD);
            c.setForeground(Theme.WHITE);
        });
    }

    // ===================== LABEL =====================

    /** Label section header kecil (mis. "INFORMASI BARANG"). */
    public static JLabel sectionLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        themed(l, () -> l.setForeground(Theme.alpha(Theme.GOLD, 220)));
        return l;
    }

    /** Label kecil di atas field (mis. "Nama Barang"). */
    public static JLabel fieldLabel(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        themed(l, () -> l.setForeground(Theme.MUTED));
        return l;
    }

    /**
     * Label yang berisi emoji/icon — otomatis pakai font emoji.
     * Gunakan ini untuk semua label yang mengandung 📦 🧾 👤 dll.
     */
    public static JLabel iconLabel(String teks, int fontSize) {
        JLabel l = new JLabel(teks);
        l.setFont(emojiFont(fontSize, Font.PLAIN));
        themed(l, () -> l.setForeground(Theme.WHITE));
        return l;
    }

    // ===================== HELPER =====================

    /**
     * Deteksi apakah string mengandung emoji/karakter Unicode di luar BMP.
     * Emoji modern (📦🧾👤) ada di code point > U+FFFF (surrogate pair).
     */
    public static boolean hasEmoji(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            int cp = s.codePointAt(i);
            // Emoji range: Miscellaneous Symbols, Dingbats, Supplemental Symbols, dll.
            if (cp > 0x2600) return true;
            if (Character.isHighSurrogate(s.charAt(i))) return true;
        }
        return false;
    }
}