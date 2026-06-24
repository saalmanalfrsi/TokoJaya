package tokojaya.form;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class DialogKonfirmasi extends JDialog {

    public enum Hasil { YA, TIDAK }
    private Hasil hasil = Hasil.TIDAK;

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
    private final Color WARN   = new Color(251, 191, 36);

    public DialogKonfirmasi(Frame parent, String judulDialog,
                            java.util.List<PanelTransaksi.ItemKeranjang> keranjang,
                            String namaCustomer, double grandTotal) {
        super(parent, true);
        setUndecorated(true);
        setSize(520, 580);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        root.setOpaque(false);
        getRootPane().setOpaque(false);
        getContentPane().setBackground(new Color(0,0,0,0));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,BORDER),
                new EmptyBorder(14,20,14,20)));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        titleRow.setOpaque(false);
        JLabel ico = new JLabel("💳");
        ico.setFont(new Font("SansSerif",Font.PLAIN,20));
        JPanel titleText = new JPanel(new GridLayout(2,1,0,2));
        titleText.setOpaque(false);
        JLabel t1 = new JLabel("Konfirmasi Pembayaran");
        t1.setFont(new Font("SansSerif",Font.BOLD,15)); t1.setForeground(WHITE);
        JLabel t2 = new JLabel("Periksa detail sebelum melanjutkan");
        t2.setFont(new Font("SansSerif",Font.PLAIN,11)); t2.setForeground(MUTED);
        titleText.add(t1); titleText.add(t2);
        titleRow.add(ico); titleRow.add(titleText);
        header.add(titleRow, BorderLayout.WEST);

        JButton btnX = new JButton("✕") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?DANGER:CARD2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                super.paintComponent(g);
            }
        };
        btnX.setFont(new Font("SansSerif",Font.BOLD,11));
        btnX.setForeground(MUTED); btnX.setContentAreaFilled(false);
        btnX.setFocusPainted(false); btnX.setBorder(new EmptyBorder(5,9,5,9));
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> { hasil=Hasil.TIDAK; dispose(); });
        header.add(btnX, BorderLayout.EAST);

        // ===== INFO CUSTOMER =====
        JPanel custPanel = new JPanel(new BorderLayout());
        custPanel.setBackground(new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),12));
        custPanel.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),40)),
                new EmptyBorder(10,20,10,20)));
        JLabel custIco = new JLabel("👤  ");
        custIco.setFont(new Font("SansSerif",Font.PLAIN,13)); custIco.setForeground(GOLD);
        JLabel custName = new JLabel(namaCustomer);
        custName.setFont(new Font("SansSerif",Font.BOLD,13)); custName.setForeground(GOLD2);
        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        custRow.setOpaque(false); custRow.add(custIco); custRow.add(custName);
        custPanel.add(custRow, BorderLayout.WEST);
        JLabel custLabel = new JLabel("Customer");
        custLabel.setFont(new Font("SansSerif",Font.PLAIN,10)); custLabel.setForeground(MUTED);
        custPanel.add(custLabel, BorderLayout.EAST);

        // ===== TABEL ITEM =====
        String[] cols = {"Barang","Qty","Harga","Subtotal"};
        Object[][] data = new Object[keranjang.size()][4];
        for (int i=0;i<keranjang.size();i++) {
            PanelTransaksi.ItemKeranjang item = keranjang.get(i);
            data[i][0] = item.namaBarang;
            data[i][1] = item.jumlah;
            data[i][2] = String.format("Rp %,.0f", item.harga);
            data[i][3] = String.format("Rp %,.0f", item.subtotal());
        }
        JTable tbl = new JTable(data, cols) {
            public boolean isCellEditable(int r,int c){return false;}
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row%2==0?CARD2:CARD);
                c.setForeground(col==3?GOLD:LIGHT);
                if(col==1||col==2||col==3) ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                return c;
            }
        };
        tbl.setBackground(CARD2); tbl.setForeground(LIGHT);
        tbl.setGridColor(new Color(255,255,255,8)); tbl.setRowHeight(32);
        tbl.setShowVerticalLines(false); tbl.setFont(new Font("SansSerif",Font.PLAIN,12));
        tbl.setIntercellSpacing(new Dimension(0,0)); tbl.setEnabled(false);
        JTableHeader th=tbl.getTableHeader();
        th.setBackground(new Color(18,24,36)); th.setForeground(MUTED);
        th.setFont(new Font("SansSerif",Font.BOLD,10)); th.setPreferredSize(new Dimension(0,34));
        int[] ws={200,50,120,120};
        for(int i=0;i<ws.length;i++) tbl.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(null); sp.getViewport().setBackground(CARD2);
        sp.setPreferredSize(new Dimension(0, Math.min(180, keranjang.size()*32+34)));

        // ===== TOTAL BOX =====
        JPanel totalBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),20),
                    getWidth(),0,new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),8));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),60));
                g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            }
        };
        totalBox.setOpaque(false);
        totalBox.setLayout(new BorderLayout());
        totalBox.setBorder(new EmptyBorder(14,18,14,18));
        JLabel totalLbl = new JLabel("TOTAL PEMBAYARAN");
        totalLbl.setFont(new Font("SansSerif",Font.BOLD,10));
        totalLbl.setForeground(new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),180));
        JLabel totalVal = new JLabel(String.format("Rp %,.0f", grandTotal));
        totalVal.setFont(new Font("Monospaced",Font.BOLD,28)); totalVal.setForeground(GOLD2);
        JLabel itemInfo = new JLabel(keranjang.size()+" jenis barang  ·  "+
            keranjang.stream().mapToInt(i->i.jumlah).sum()+" total unit");
        itemInfo.setFont(new Font("SansSerif",Font.PLAIN,11)); itemInfo.setForeground(MUTED);
        totalBox.add(totalLbl, BorderLayout.NORTH);
        totalBox.add(totalVal, BorderLayout.CENTER);
        totalBox.add(itemInfo, BorderLayout.SOUTH);

        // ===== TOMBOL =====
        JPanel btnPanel = new JPanel(new GridLayout(1,2,10,0));
        btnPanel.setOpaque(false);

        JButton btnBatal = buatBtn("✕  Batalkan", CARD2, MUTED, false);
        JButton btnBayar = buatBtn("✓  Konfirmasi Bayar", GOLD, new Color(15,20,30), true);
        btnPanel.add(btnBatal); btnPanel.add(btnBayar);

        btnBatal.addActionListener(e -> { hasil=Hasil.TIDAK; dispose(); });
        btnBayar.addActionListener(e -> { hasil=Hasil.YA;    dispose(); });

        // ===== SUSUN LAYOUT =====
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14,18,14,18));
        body.add(sp);
        body.add(Box.createVerticalStrut(12));
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        body.add(totalBox);
        body.add(Box.createVerticalStrut(14));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        body.add(btnPanel);

        root.add(header,     BorderLayout.NORTH);
        root.add(custPanel,  BorderLayout.CENTER);
        root.add(body,       BorderLayout.SOUTH);

        // Pasang layout ulang dengan lebih proper
        root.removeAll();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        custPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        root.add(header); root.add(custPanel); root.add(body);

        setContentPane(root);
        getRootPane().setBackground(new Color(0,0,0,0));
    }

    public Hasil tampilkan() {
        setVisible(true);
        return hasil;
    }

    private JButton buatBtn(String t, Color bg, Color fg, boolean primary) {
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
        b.setPreferredSize(new Dimension(0,44));
        b.setBorder(new EmptyBorder(0,10,0,10));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}