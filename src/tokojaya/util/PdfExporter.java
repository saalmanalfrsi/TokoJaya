package tokojaya.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import tokojaya.model.LaporanRow;
import tokojaya.service.LaporanService;
import tokojaya.service.PeriodeLaporan;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Generator PDF laporan penjualan memakai OpenPDF (fork iText 2.x, lisensi LGPL/MPL).
 *
 * <p>OpenPDF dipilih karena: stabil & masih dipelihara, API sederhana, lisensi
 * ramah-komersial (berbeda dengan iText 5/7 yang AGPL), dan satu JAR tanpa
 * dependency tambahan — cocok untuk aplikasi Swing desktop.
 */
public final class PdfExporter {

    // Palet warna PDF (independen dari tema aplikasi agar hasil cetak konsisten).
    private static final Color BIRU_TUA = new Color(30, 41, 73);
    private static final Color EMAS     = new Color(190, 150, 40);
    private static final Color ABU      = new Color(238, 240, 245);
    private static final Color ABU_TEKS = new Color(110, 120, 135);

    private PdfExporter() {}

    /**
     * Tulis laporan ke file PDF.
     * @throws Exception bila gagal menulis (ditangani pemanggil untuk tampil dialog).
     */
    public static void exportLaporan(File file, List<LaporanRow> rows,
                                     PeriodeLaporan periode,
                                     LaporanService.Ringkasan ringkasan) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 40, 44);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        tulisHeader(doc, periode);
        tulisTabel(doc, rows);
        tulisRingkasan(doc, ringkasan);
        tulisFooter(doc);

        doc.close();
    }

    private static void tulisHeader(Document doc, PeriodeLaporan periode) throws DocumentException {
        Font fJudul = new Font(Font.HELVETICA, 18, Font.BOLD, BIRU_TUA);
        Font fToko  = new Font(Font.HELVETICA, 11, Font.BOLD, EMAS);
        Font fInfo  = new Font(Font.HELVETICA, 9,  Font.NORMAL, ABU_TEKS);

        Paragraph toko = new Paragraph("TOKO BERKAH JAYA", fToko);
        doc.add(toko);

        Paragraph judul = new Paragraph("Laporan Penjualan", fJudul);
        judul.setSpacingAfter(2);
        doc.add(judul);

        String tgl = new SimpleDateFormat("dd MMMM yyyy, HH:mm").format(new Date());
        Paragraph info = new Paragraph(
            "Periode: " + (periode == null ? "Semua Waktu" : periode.getLabel())
            + "     |     Dicetak: " + tgl, fInfo);
        info.setSpacingAfter(10);
        doc.add(info);

        // Garis pemisah
        PdfPTable garis = new PdfPTable(1);
        garis.setWidthPercentage(100);
        PdfPCell g = new PdfPCell();
        g.setFixedHeight(2.5f);
        g.setBackgroundColor(EMAS);
        g.setBorder(Rectangle.NO_BORDER);
        garis.addCell(g);
        garis.setSpacingAfter(12);
        doc.add(garis);
    }

    private static void tulisTabel(Document doc, List<LaporanRow> rows) throws DocumentException {
        Font fHead = new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE);
        Font fSel  = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, new Color(40, 48, 66));

        PdfPTable tabel = new PdfPTable(new float[]{4f, 13f, 12f, 14f, 4.5f, 11f, 11f, 11f});
        tabel.setWidthPercentage(100);
        tabel.setHeaderRows(1);

        String[] judulKolom = {"No", "No. Nota", "Tanggal", "Customer", "Qty",
                               "Total", "Bayar", "Kembalian"};
        for (String j : judulKolom) {
            PdfPCell c = new PdfPCell(new Phrase(j, fHead));
            c.setBackgroundColor(BIRU_TUA);
            c.setPadding(6);
            c.setBorderColor(BIRU_TUA);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabel.addCell(c);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        int no = 1;
        for (LaporanRow r : rows) {
            boolean genap = (no % 2 == 0);
            Color bg = genap ? ABU : Color.WHITE;
            tambahSel(tabel, String.valueOf(no++), fSel, bg, Element.ALIGN_CENTER);
            tambahSel(tabel, r.getNoNota(), fSel, bg, Element.ALIGN_LEFT);
            tambahSel(tabel, r.getTanggal() == null ? "-" : sdf.format(r.getTanggal()),
                      fSel, bg, Element.ALIGN_CENTER);
            tambahSel(tabel, r.getCustomer(), fSel, bg, Element.ALIGN_LEFT);
            tambahSel(tabel, String.valueOf(r.getTotalQty()), fSel, bg, Element.ALIGN_CENTER);
            tambahSel(tabel, Rupiah.format(r.getTotal()), fSel, bg, Element.ALIGN_RIGHT);
            tambahSel(tabel, Rupiah.format(r.getUangBayar()), fSel, bg, Element.ALIGN_RIGHT);
            tambahSel(tabel, Rupiah.format(r.getKembalian()), fSel, bg, Element.ALIGN_RIGHT);
        }

        if (rows.isEmpty()) {
            PdfPCell kosong = new PdfPCell(new Phrase("Tidak ada data pada periode ini.", fSel));
            kosong.setColspan(8);
            kosong.setPadding(12);
            kosong.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabel.addCell(kosong);
        }

        tabel.setSpacingAfter(12);
        doc.add(tabel);
    }

    private static void tambahSel(PdfPTable tabel, String teks, Font font, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(teks == null ? "-" : teks, font));
        c.setBackgroundColor(bg);
        c.setPadding(5);
        c.setBorderColor(new Color(220, 224, 232));
        c.setHorizontalAlignment(align);
        tabel.addCell(c);
    }

    private static void tulisRingkasan(Document doc, LaporanService.Ringkasan r) throws DocumentException {
        Font fLbl = new Font(Font.HELVETICA, 9,  Font.NORMAL, ABU_TEKS);
        Font fVal = new Font(Font.HELVETICA, 11, Font.BOLD, BIRU_TUA);

        PdfPTable box = new PdfPTable(3);
        box.setWidthPercentage(100);
        box.addCell(kartuRingkasan("TOTAL TRANSAKSI", String.valueOf(r.jumlahTransaksi), fLbl, fVal));
        box.addCell(kartuRingkasan("TOTAL PENDAPATAN", Rupiah.format(r.totalPendapatan), fLbl, fVal));
        box.addCell(kartuRingkasan("RATA-RATA / TRX", Rupiah.format(r.rataRata), fLbl, fVal));
        doc.add(box);
    }

    private static PdfPCell kartuRingkasan(String label, String nilai, Font fLbl, Font fVal) {
        PdfPTable inner = new PdfPTable(1);
        PdfPCell l = new PdfPCell(new Phrase(label, fLbl));
        l.setBorder(Rectangle.NO_BORDER); l.setPaddingBottom(3);
        PdfPCell v = new PdfPCell(new Phrase(nilai, fVal));
        v.setBorder(Rectangle.NO_BORDER);
        inner.addCell(l); inner.addCell(v);

        PdfPCell wrap = new PdfPCell(inner);
        wrap.setPadding(10);
        wrap.setBackgroundColor(ABU);
        wrap.setBorderColor(new Color(220, 224, 232));
        return wrap;
    }

    private static void tulisFooter(Document doc) throws DocumentException {
        Font f = new Font(Font.HELVETICA, 8, Font.ITALIC, ABU_TEKS);
        Paragraph p = new Paragraph(
            "\nDokumen ini dibuat otomatis oleh aplikasi Toko Berkah Jaya POS.", f);
        p.setSpacingBefore(16);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }
}
