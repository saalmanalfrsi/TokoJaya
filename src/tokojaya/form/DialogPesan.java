package tokojaya.form;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class DialogPesan extends JDialog {

    public enum Tipe { INFO, WARN, ERROR, SUCCESS }

    private final Color BG     = new Color(15, 19, 27);
    private final Color CARD   = new Color(22, 28, 40);
    private final Color CARD2  = new Color(28, 36, 52);
    private final Color GOLD   = new Color(212, 175, 55);
    private final Color GOLD2  = new Color(255, 215, 80);
    private final Color WHITE  = new Color(240, 244, 255);
    private final Color MUTED  = new Color(100, 120, 155);
    private final Color BORDER = new Color(40, 52, 75);
    private final Color SUCCESS= new Color(52, 211, 153);
    private final Color DANGER = new Color(251, 113, 133);
    private final Color WARN   = new Color(251, 191, 36);
    private final Color BLUE   = new Color(99, 179, 237);

    public DialogPesan(Frame parent, String judul, String pesan, Tipe tipe) {
        super(parent, true);
        setUndecorated(true);
        setSize(380, 220);
        setLocationRelativeTo(parent);

        Color accent = tipe==Tipe.ERROR ? DANGER :
                       tipe==Tipe.WARN  ? WARN   :
                       tipe==Tipe.SUCCESS? SUCCESS : BLUE;

        String icoStr = tipe==Tipe.ERROR  ? "✕" :
                        tipe==Tipe.WARN   ? "⚠" :
                        tipe==Tipe.SUCCESS ? "✓" : "ℹ";

        JPanel root = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
            }
        };
        root.setLayout(new BorderLayout());
        root.setOpaque(false);

        // Garis atas berwarna
        JPanel topLine = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                GradientPaint gp=new GradientPaint(0,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),200),
                    getWidth(),0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40));
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
            }
        };
        topLine.setOpaque(false);
        topLine.setPreferredSize(new Dimension(0,4));

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20,24,20,24));

        // Icon + Judul
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        titleRow.setOpaque(false); titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));

        JPanel icoCircle = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));
                g2.fillOval(0,0,28,28);
                g2.setColor(accent); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0,0,27,27);
                g2.setFont(new Font("SansSerif",Font.BOLD,13)); g2.setColor(accent);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(icoStr,(28-fm.stringWidth(icoStr))/2,(28+fm.getAscent()-fm.getDescent())/2);
            }
        };
        icoCircle.setOpaque(false); icoCircle.setPreferredSize(new Dimension(28,28));

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("SansSerif",Font.BOLD,14)); lJudul.setForeground(WHITE);

        titleRow.add(icoCircle); titleRow.add(lJudul);
        body.add(titleRow); body.add(Box.createVerticalStrut(12));

        // Pesan
        JLabel lPesan = new JLabel("<html><div style='width:300px;'>"+
            pesan.replace("\n","<br>")+"</div></html>");
        lPesan.setFont(new Font("SansSerif",Font.PLAIN,12));
        lPesan.setForeground(new Color(180,195,220));
        lPesan.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lPesan); body.add(Box.createVerticalStrut(20));

        // Tombol OK
        JButton btnOk = new JButton("  OK  ") {
            private boolean hov=false;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),60):
                    new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),150));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                super.paintComponent(g);
            }
        };
        btnOk.setFont(new Font("SansSerif",Font.BOLD,12));
        btnOk.setForeground(accent); btnOk.setContentAreaFilled(false);
        btnOk.setFocusPainted(false); btnOk.setBorder(new EmptyBorder(8,24,8,24));
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnOk.addActionListener(e -> dispose());

        body.add(btnOk);

        root.add(topLine, BorderLayout.NORTH);
        root.add(body,    BorderLayout.CENTER);
        setContentPane(root);
    }

    public void tampilkan() { setVisible(true); }
}