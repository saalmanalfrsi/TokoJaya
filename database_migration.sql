-- =====================================================================
--  TOKO BERKAH JAYA — MIGRASI DATABASE (revisi v2.1)
--  Jalankan di MySQL (phpMyAdmin / CLI) pada database: db_toko_berkah
--  Aman dijalankan pada database yang sudah berisi data lama.
-- =====================================================================

USE db_toko_berkah;

-- ---------------------------------------------------------------------
-- 1. TABEL KATEGORI BARANG (BARU)
--    Sebelumnya kategori di-hardcode (1=Elektronik, 2=Makanan, 3=Minuman).
--    Sekarang dipindah ke tabel agar bisa ditambah & diedit dari aplikasi.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_kategori (
    id_kategori   INT          NOT NULL AUTO_INCREMENT,
    nama_kategori VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id_kategori),
    UNIQUE KEY uq_nama_kategori (nama_kategori)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed kategori awal (sesuai data lama). Abaikan jika sudah ada.
INSERT IGNORE INTO tb_kategori (id_kategori, nama_kategori) VALUES
    (1, 'Elektronik'),
    (2, 'Makanan'),
    (3, 'Minuman');

-- ---------------------------------------------------------------------
-- 2. KOLOM KATEGORI PADA CUSTOMER (BARU)
--    Requirement: customer punya kategori, nilai 'anomali' DILARANG.
-- ---------------------------------------------------------------------
ALTER TABLE tb_customer
    ADD COLUMN kategori VARCHAR(30) NOT NULL DEFAULT 'Umum' AFTER telepon;

-- ---------------------------------------------------------------------
-- 3. KOLOM TRANSAKSI: NO NOTA, UANG BAYAR, KEMBALIAN (BARU)
--    Disimpan per-baris item, namun bernilai sama untuk satu nota,
--    sehingga laporan dapat dikelompokkan per nota (GROUP BY no_nota).
-- ---------------------------------------------------------------------
ALTER TABLE tb_penjualan
    ADD COLUMN no_nota    VARCHAR(30) NULL          AFTER id_jual,
    ADD COLUMN uang_bayar DOUBLE      NOT NULL DEFAULT 0,
    ADD COLUMN kembalian  DOUBLE      NOT NULL DEFAULT 0;

-- Ubah tgl_transaksi menjadi DATETIME agar filter laporan (hari ini /
-- minggu ini / bulan ini) lebih presisi. Aman untuk data lama (jam 00:00).
ALTER TABLE tb_penjualan
    MODIFY COLUMN tgl_transaksi DATETIME NOT NULL;

-- Index untuk mempercepat filter & pengelompokan laporan.
CREATE INDEX idx_penjualan_nota ON tb_penjualan (no_nota);
CREATE INDEX idx_penjualan_tgl  ON tb_penjualan (tgl_transaksi);

-- Beri nomor nota untuk data lama (agar tidak NULL saat dikelompokkan).
UPDATE tb_penjualan
   SET no_nota = CONCAT('OLD-', LPAD(id_jual, 6, '0'))
 WHERE no_nota IS NULL OR no_nota = '';

-- ---------------------------------------------------------------------
-- 4. (OPSIONAL) Foreign key kategori barang -> tb_kategori
--    Jalankan hanya jika seluruh id_kategori pada tb_barang valid.
-- ---------------------------------------------------------------------
-- ALTER TABLE tb_barang
--     ADD CONSTRAINT fk_barang_kategori
--     FOREIGN KEY (id_kategori) REFERENCES tb_kategori (id_kategori);

-- Selesai.
