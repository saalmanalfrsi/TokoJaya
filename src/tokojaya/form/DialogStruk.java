package tokojaya.form;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogStruk extends JDialog implements Printable {

    private final Color BG     = new Color(15, 19, 27);
    private final Color CARD   = new Color(22, 28, 40);
    private final Color CARD2  = new Color(28, 36, 52);
    private final Color GOLD   = new Color(212, 175, 55);
    private final Color GOLD2  = new Color(255, 215, 80);
    private final Color WHITE  = new Color(240, 244, 255);
    private final Color LIGHT  = new Color(180, 195, 220);
    private final Color MUTED  = new Color(100, 120, 155);
    private final Color BORDER = new Color(40, 52, 75);
    private final Color SUCCESS= new Color(52, 211, 153);
    private final Color DANGER = new Color(251, 113, 133);

    private java.util.List<PanelTransaksi.ItemKeranjang> keranjang;
    private String namaCustomer;
    private double grandTotal;
    private String noTransaksi;

    public DialogStruk(Frame parent,
                       java.util.List<PanelTransaksi.ItemKeranjang> keranjang,
                       String namaCustomer, double grandTotal) {
        super(parent, true);
        this.keranjang    = keranjang;
        this.namaCustomer = namaCustomer;
        this.grandTotal   = grandTotal;
        this.noTransaksi  = "TRX-" + new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());

        setUndecorated(true);
        setSize(500, 660);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),60));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
            }
        };
        root.setOpaque(false);

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,BORDER),
                new EmptyBorder(12,18,12,18)));
        JLabel hTitle = new JLabel("🖨  Struk Belanja");
        hTitle.setFont(new Font("SansSerif",Font.BOLD,14)); hTitle.setForeground(WHITE);
        JButton btnX = buatBtnTutup();
        btnX.addActionListener(e -> dispose());
        header.add(hTitle, BorderLayout.WEST);
        header.add(btnX,   BorderLayout.EAST);

        // ===== STRUK PANEL =====
        JPanel struk = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Background kertas putih kekuningan
                g2.setColor(new Color(252,250,240));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                // Shadow
                g2.setColor(new Color(0,0,0,30));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                super.paintComponent(g);
            }
        };
        struk.setLayout(new BoxLayout(struk, BoxLayout.Y_AXIS));
        struk.setOpaque(false);
        struk.setBorder(new EmptyBorder(20,24,20,24));

        // Logo toko
        JLabel logoToko = new JLabel("BERKAH JAYA", SwingConstants.CENTER);
        logoToko.setFont(new Font("SansSerif",Font.BOLD,20));
        logoToko.setForeground(new Color(30,20,10));
        logoToko.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subToko = new JLabel("Point of Sale System", SwingConstants.CENTER);
        subToko.setFont(new Font("SansSerif",Font.PLAIN,11));
        subToko.setForeground(new Color(120,100,60));
        subToko.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel alamat = new JLabel("Jl. Contoh No.1, Kota", SwingConstants.CENTER);
        alamat.setFont(new Font("SansSerif",Font.PLAIN,10));
        alamat.setForeground(new Color(140,120,80));
        alamat.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Garis pemisah
        struk.add(logoToko);
        struk.add(Box.createVerticalStrut(2));
        struk.add(subToko);
        struk.add(Box.createVerticalStrut(2));
        struk.add(alamat);
        struk.add(Box.createVerticalStrut(10));
        struk.add(buatGarisStruk());
        struk.add(Box.createVerticalStrut(8));

        // Info transaksi
        struk.add(buatRowStruk("No. Transaksi", noTransaksi, false));
        struk.add(buatRowStruk("Tanggal",
            new SimpleDateFormat("dd/MM/yyyy").format(new Date()), false));
        struk.add(buatRowStruk("Jam",
            new SimpleDateFormat("HH:mm:ss").format(new Date()), false));
        struk.add(buatRowStruk("Customer", namaCustomer, false));
        struk.add(Box.createVerticalStrut(8));
        struk.add(buatGarisStruk());
        struk.add(Box.createVerticalStrut(8));

        // Header kolom item
        JPanel colHeader = new JPanel(new BorderLayout());
        colHeader.setOpaque(false);
        JLabel cNama = new JLabel("ITEM");
        cNama.setFont(new Font("SansSerif",Font.BOLD,10));
        cNama.setForeground(new Color(80,60,20));
        JLabel cJml = new JLabel("QTY  ×  HARGA        TOTAL");
        cJml.setFont(new Font("SansSerif",Font.BOLD,10));
        cJml.setForeground(new Color(80,60,20));
        cJml.setHorizontalAlignment(JLabel.RIGHT);
        colHeader.add(cNama, BorderLayout.WEST);
        colHeader.add(cJml, BorderLayout.EAST);
        struk.add(colHeader);
        struk.add(Box.createVerticalStrut(6));

        // Item-item belanja
        for (PanelTransaksi.ItemKeranjang item : keranjang) {
            JPanel itemPanel = new JPanel(new BorderLayout(4,2));
            itemPanel.setOpaque(false);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));

            JLabel lNama = new JLabel(item.namaBarang);
            lNama.setFont(new Font("SansSerif",Font.BOLD,11));
            lNama.setForeground(new Color(30,20,10));

            JLabel lDetail = new JLabel(String.format("%d × Rp %,.0f", item.jumlah, item.harga));
            lDetail.setFont(new Font("Monospaced",Font.PLAIN,10));
            lDetail.setForeground(new Color(120,100,60));

            JLabel lSubtotal = new JLabel(String.format("Rp %,.0f", item.subtotal()));
            lSubtotal.setFont(new Font("Monospaced",Font.BOLD,11));
            lSubtotal.setForeground(new Color(30,20,10));
            lSubtotal.setHorizontalAlignment(JLabel.RIGHT);

            JPanel leftItem = new JPanel(new GridLayout(2,1,0,1));
            leftItem.setOpaque(false);
            leftItem.add(lNama); leftItem.add(lDetail);

            itemPanel.add(leftItem,   BorderLayout.CENTER);
            itemPanel.add(lSubtotal,  BorderLayout.EAST);
            struk.add(itemPanel);
            struk.add(Box.createVerticalStrut(4));
        }

        struk.add(Box.createVerticalStrut(6));
        struk.add(buatGarisStruk());
        struk.add(Box.createVerticalStrut(8));

        // Subtotal, total
        int totalUnit = keranjang.stream().mapToInt(i->i.jumlah).sum();
        struk.add(buatRowStruk("Total Item", totalUnit+" unit", false));
        struk.add(Box.createVerticalStrut(4));

        // Total bayar highlight
        JPanel totalRow = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200,170,80,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
            }
        };
        totalRow.setOpaque(false);
        totalRow.setBorder(new EmptyBorder(6,8,6,8));
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        JLabel tLabel = new JLabel("TOTAL BAYAR");
        tLabel.setFont(new Font("SansSerif",Font.BOLD,13));
        tLabel.setForeground(new Color(80,50,0));
        JLabel tValue = new JLabel(String.format("Rp %,.0f", grandTotal));
        tValue.setFont(new Font("Monospaced",Font.BOLD,16));
        tValue.setForeground(new Color(150,100,0));
        tValue.setHorizontalAlignment(JLabel.RIGHT);
        totalRow.add(tLabel, BorderLayout.WEST);
        totalRow.add(tValue, BorderLayout.EAST);
        struk.add(totalRow);
        struk.add(Box.createVerticalStrut(8));
        struk.add(buatGarisStruk());
        struk.add(Box.createVerticalStrut(12));

        // Ucapan terima kasih
        JLabel trims = new JLabel("— Terima kasih telah berbelanja —", SwingConstants.CENTER);
        trims.setFont(new Font("SansSerif",Font.ITALIC,11));
        trims.setForeground(new Color(140,120,80));
        trims.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel powered = new JLabel("POS System  ·  Berkah Jaya", SwingConstants.CENTER);
        powered.setFont(new Font("SansSerif",Font.PLAIN,9));
        powered.setForeground(new Color(180,160,120));
        powered.setAlignmentX(Component.CENTER_ALIGNMENT);

        struk.add(trims);
        struk.add(Box.createVerticalStrut(4));
        struk.add(powered);

        JScrollPane spStruk = new JScrollPane(struk);
        spStruk.setBorder(new EmptyBorder(0,0,0,0));
        spStruk.getViewport().setBackground(new Color(252,250,240));
        spStruk.setBackground(new Color(252,250,240));

        // ===== WRAPPER STRUK dengan padding =====
        JPanel strukWrap = new JPanel(new BorderLayout());
        strukWrap.setBackground(BG);
        strukWrap.setBorder(new EmptyBorder(16,24,16,24));
        strukWrap.add(spStruk, BorderLayout.CENTER);

        // ===== TOMBOL BAWAH =====
        JPanel btnRow = new JPanel(new GridLayout(1,2,10,0));
        btnRow.setBackground(CARD);
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1,0,0,0,BORDER),
                new EmptyBorder(14,18,14,18)));

        JButton btnTutup = buatBtnFull("✕  Tutup",  CARD2, MUTED,  false);
        JButton btnCetak = buatBtnFull("🖨  Print",  GOLD,  new Color(15,20,30), true);

        btnTutup.addActionListener(e -> dispose());
        btnCetak.addActionListener(e -> cetakKePrinter());

        btnRow.add(btnTutup);
        btnRow.add(btnCetak);

        root.add(header,    BorderLayout.NORTH);
        root.add(strukWrap, BorderLayout.CENTER);
        root.add(btnRow,    BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buatGarisStruk() {
        JPanel garis = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(180,160,120,80));
                // Garis putus-putus
                Graphics2D g2=(Graphics2D)g;
                g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,
                    10,new float[]{6,4},0));
                g2.drawLine(0,0,getWidth(),0);
            }
        };
        garis.setOpaque(false);
        garis.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        garis.setPreferredSize(new Dimension(0,1));
        return garis;
    }

    private JPanel buatRowStruk(String label, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout(8,0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif",Font.PLAIN,10));
        lbl.setForeground(new Color(120,100,60));
        JLabel val = new JLabel(value);
        val.setFont(bold ? new Font("Monospaced",Font.BOLD,11) : new Font("SansSerif",Font.PLAIN,11));
        val.setForeground(new Color(30,20,10));
        val.setHorizontalAlignment(JLabel.RIGHT);
        row.add(lbl,BorderLayout.WEST);
        row.add(val,BorderLayout.EAST);
        return row;
    }

    private void cetakKePrinter() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk - "+noTransaksi);
        job.setPrintable(this);
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,"Gagal mencetak: "+ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(pf.getImageableX(), pf.getImageableY());
        double scaleX = pf.getImageableWidth()  / 380.0;
        double scaleY = pf.getImageableHeight() / 550.0;
        double scale  = Math.min(scaleX, scaleY);
        g2.scale(scale, scale);

        // Render struk ke printer
        int x=10, y=20, lh=18;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Monospaced",Font.BOLD,14));
        drawCenter(g2,"TOKO BERKAH JAYA",x,y,360); y+=lh;
        g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        drawCenter(g2,"Point of Sale System",x,y,360); y+=lh;
        drawCenter(g2,"Jl. Contoh No.1, Kota",x,y,360); y+=lh;
        drawLine(g2,x,y,360); y+=6;

        g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        g2.drawString("No : "+noTransaksi, x, y+=lh);
        g2.drawString("Tgl: "+new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), x, y+=lh);
        g2.drawString("Cst: "+namaCustomer, x, y+=lh);
        drawLine(g2,x,y+=4,360); y+=8;

        g2.setFont(new Font("Monospaced",Font.BOLD,10));
        g2.drawString("ITEM",x,y+=lh);
        g2.drawString("SUBTOTAL",260,y);
        drawLine(g2,x,y+=4,360); y+=6;

        g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        for (PanelTransaksi.ItemKeranjang item : keranjang) {
            String nama = item.namaBarang.length()>24?item.namaBarang.substring(0,24):item.namaBarang;
            g2.drawString(nama,x,y+=lh);
            String detail=String.format("%dx Rp%,.0f",item.jumlah,item.harga);
            String sub=String.format("Rp%,.0f",item.subtotal());
            g2.drawString(detail,x+10,y+=12);
            g2.drawString(sub,360-g2.getFontMetrics().stringWidth(sub),y);
            y+=4;
        }

        drawLine(g2,x,y+=6,360); y+=6;
        g2.setFont(new Font("Monospaced",Font.BOLD,12));
        String tot=String.format("TOTAL  Rp %,.0f",grandTotal);
        g2.drawString(tot,x,y+=lh);
        drawLine(g2,x,y+=4,360); y+=10;

        g2.setFont(new Font("Monospaced",Font.PLAIN,9));
        drawCenter(g2,"Terima kasih telah berbelanja",x,y+=lh,360);
        return PAGE_EXISTS;
    }

    private void drawCenter(Graphics2D g, String s, int x, int y, int w) {
        int sw = g.getFontMetrics().stringWidth(s);
        g.drawString(s, x+(w-sw)/2, y);
    }
    private void drawLine(Graphics2D g, int x, int y, int w) {
        g.setColor(Color.LIGHT_GRAY);
        Stroke old=g.getStroke();
        g.setStroke(new BasicStroke(0.5f));
        g.drawLine(x,y,x+w,y);
        g.setStroke(old); g.setColor(Color.BLACK);
    }

    public void tampilkan() { setVisible(true); }

    private JButton buatBtnTutup() {
        JButton b = new JButton("✕") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?DANGER:CARD2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif",Font.BOLD,11)); b.setForeground(MUTED);
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(5,9,5,9)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton buatBtnFull(String t, Color bg, Color fg, boolean primary) {
        JButton b = new JButton(t) {
            private boolean hov=false;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(primary){
                    GradientPaint gp=hov?new GradientPaint(0,0,GOLD2,getWidth(),0,bg):
                        new GradientPaint(0,0,bg,getWidth(),0,new Color(160,125,30));
                    g2.setPaint(gp);
                }else{
                    g2.setColor(hov?new Color(38,48,68):bg);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(BORDER); g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                }
                if(primary) g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif",Font.BOLD,13)); b.setForeground(fg);
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0,42));
        b.setBorder(new EmptyBorder(0,10,0,10));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
}