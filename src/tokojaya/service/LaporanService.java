package tokojaya.service;

import tokojaya.dao.PenjualanDAO;
import tokojaya.model.LaporanRow;
import java.util.List;

/**
 * Logika bisnis laporan: ambil data terfilter + hitung ringkasan agregat.
 */
public class LaporanService {

    private final PenjualanDAO dao = new PenjualanDAO();

    public List<LaporanRow> getLaporan(PeriodeLaporan periode) {
        return dao.getLaporan(periode);
    }

    /** Ringkasan: [jumlahTransaksi, totalPendapatan, rataRataPerTransaksi]. */
    public Ringkasan hitungRingkasan(List<LaporanRow> rows) {
        double total = 0;
        for (LaporanRow r : rows) total += r.getTotal();
        double rata = rows.isEmpty() ? 0 : total / rows.size();
        return new Ringkasan(rows.size(), total, rata);
    }

    public static class Ringkasan {
        public final int    jumlahTransaksi;
        public final double totalPendapatan;
        public final double rataRata;
        public Ringkasan(int jumlahTransaksi, double totalPendapatan, double rataRata) {
            this.jumlahTransaksi = jumlahTransaksi;
            this.totalPendapatan = totalPendapatan;
            this.rataRata        = rataRata;
        }
    }
}
