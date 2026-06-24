package tokojaya.form;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class DialogSukses extends JDialog {

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

    public DialogSukses(Frame parent, String namaCustomer,
                        java.util.List<PanelTransaksi.ItemKeranjang> keranjang,
                        double grandTotal) {
        super(parent, true);
        setUndecorated(true);
        setSize(440, 460);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(SUCCESS.getRed(),SUCCESS.getGreen(),SUCCESS.getBlue(),80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                // Glow atas
                g2.setColor(new Color(SUCCESS.getRed(),SUCCESS.getGreen(),SUCCESS.getBlue(),15));
                g2.fillOval(-60,-60,280,200);
            }
        };
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(0,0,0,0));

        // Icon sukses animasi
        JPanel icoPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2, cy=getHeight()/2, r=36;
                // Lingkaran hijau
                g2.setColor(new Color(SUCCESS.getRed(),SUCCESS.getGreen(),SUCCESS.getBlue(),30));
                g2.fillOval(cx-r-8,cy-r-8,2*(r+8),2*(r+8));
                g2.setColor(SUCCESS);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx-r,cy-r,2*r,2*r);
                // Centang
                g2.setColor(SUCCESS);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx-18,cy,cx-5,cy+14);
                g2.drawLine(cx-5,cy+14,cx+20,cy-14);
            }
        };
        icoPanel.setOpaque(false);
        icoPanel.setPreferredSize(new Dimension(0,100));
        icoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));

        // Teks sukses
        JLabel lblSukses = new JLabel("Pembayaran Berhasil!", SwingConstants.CENTER);
        lblSukses.setFont(new Font("SansSerif",Font.BOLD,20)); lblSukses.setForeground(WHITE);
        lblSukses.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSukses.setBorder(new EmptyBorder(0,0,4,0));

        JLabel lblSub = new JLabel("Terima kasih, "+namaCustomer, SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif",Font.PLAIN,12)); lblSub.setForeground(MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Card info ringkasan
        JPanel infoCard = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
            }
        };
        infoCard.setOpaque(false);
        infoCard.setLayout(new GridLayout(3,2,8,10));
        infoCard.setBorder(new EmptyBorder(16,20,16,20));
        infoCard.setMaximumSize(new Dimension(360,110));
        infoCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        addInfoRow(infoCard, "Total Bayar",
            String.format("Rp %,.0f", grandTotal), GOLD2, true);
        addInfoRow(infoCard, "Jumlah Item",
            keranjang.size()+" jenis  ·  "+
            keranjang.stream().mapToInt(i->i.jumlah).sum()+" unit", LIGHT, false);
        addInfoRow(infoCard, "Waktu",
            new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date()), LIGHT, false);

        // Tombol tutup
        JButton btnTutup = new JButton("Selesai") {
            private boolean hov=false;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=hov?new GradientPaint(0,0,new Color(70,220,160),getWidth(),0,SUCCESS):
                        new GradientPaint(0,0,SUCCESS,getWidth(),0,new Color(30,160,100));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                super.paintComponent(g);
            }
        };
        btnTutup.setFont(new Font("SansSerif",Font.BOLD,14));
        btnTutup.setForeground(new Color(10,30,20));
        btnTutup.setContentAreaFilled(false); btnTutup.setFocusPainted(false);
        btnTutup.setPreferredSize(new Dimension(160,44));
        btnTutup.setMaximumSize(new Dimension(160,44));
        btnTutup.setBorder(new EmptyBorder(0,0,0,0));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTutup.addActionListener(e -> dispose());

        root.add(icoPanel);
        root.add(lblSukses);
        root.add(lblSub);
        root.add(Box.createVerticalStrut(16));
        root.add(infoCard);
        root.add(Box.createVerticalStrut(20));
        root.add(btnTutup);
        root.add(Box.createVerticalStrut(24));

        setContentPane(root);
    }

    private void addInfoRow(JPanel panel, String label, String value, Color valueColor, boolean bold) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif",Font.PLAIN,11)); lbl.setForeground(MUTED);
        JLabel val = new JLabel(value);
        val.setFont(bold ? new Font("Monospaced",Font.BOLD,15) : new Font("SansSerif",Font.PLAIN,12));
        val.setForeground(valueColor);
        val.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(lbl); panel.add(val);
    }

    public void tampilkan() { setVisible(true); }
}