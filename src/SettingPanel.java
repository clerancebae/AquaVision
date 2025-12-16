import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D; // Yuvarlak köşeler için gerekli

public class SettingPanel extends JPanel {

    public SettingPanel() {
        setBackground(new Color(189, 237, 255));
        setLayout(null);
        setSize(400, 300);

        // ---- Close Button ----
        ImageIcon icon = new ImageIcon("close.png");
        Image scaled = (icon.getImage() != null) ?
                icon.getImage().getScaledInstance(90, 60, Image.SCALE_SMOOTH) : null;
        ImageIcon scaledIcon = (scaled != null) ? new ImageIcon(scaled) : null;

        JButton closeBtn = new JButton(scaledIcon);
        if (scaledIcon == null) closeBtn.setText("X");

        closeBtn.setBounds(290, 10, 60, 60);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false); // Odaklanma çizgisini kaldırır
        closeBtn.setOpaque(false);

        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });
        add(closeBtn);

        // ---- Voice Button (Senin istediğin gibi duruyor) ----
        // Buraya "voice.png" veya "sound.png" gibi bir ikon ekleyeceksin sanırım
        ImageIcon iconClose = new ImageIcon(".png");
        Image scaledClose = (iconClose.getImage() != null) ?
                iconClose.getImage().getScaledInstance(90, 60, Image.SCALE_SMOOTH) : null;
        ImageIcon scaledIconClose = (scaledClose != null) ? new ImageIcon(scaledClose) : null;

        JButton closeVoice = new JButton(scaledIconClose);
        if (scaledIconClose == null) closeVoice.setText("-");

        closeVoice.setBounds(10, 10, 60, 60);
        closeVoice.setBorderPainted(false);
        closeVoice.setContentAreaFilled(false);
        closeVoice.setFocusPainted(false);
        closeVoice.setOpaque(false); // Düzeltme: closeBtn yerine closeVoice yapıldı

        closeVoice.addActionListener(e -> {
            // Buraya ses açma kapama kodlarını ekleyeceksin
        });
        add(closeVoice);

        // ---- SLIDER KISMI ----
        JSlider slider = getJSlider();
        add(slider);
        JLabel title = new JLabel("Game Settings");
        title.setBounds(50, 0, 200, 60);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        title.setForeground(new Color(0, 60, 120));
        add(title);

        JLabel music = new JLabel("Music");
        music.setBounds(35, 60, 200, 60);
        music.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,15));
        music.setForeground(new Color(0, 60, 120));
        add(music);
        slider.addChangeListener(e -> {
            int volume = slider.getValue();
            System.out.println(volume);
            //SoundManager.setVolume(volume / 100f);
        });
    }

    private static JSlider getJSlider() {
        JSlider slider = new JSlider();
        // Slider boyutunu biraz artırdım ki altın çerçeve sığsın
        slider.setBounds(10, 100, 275, 60);

        // Tasarım gereği standart çizgileri kapatıyoruz (Daha şık durması için)
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(false);
        slider.setPaintLabels(true);
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setValue(50); // Başlangıç değeri

        // 1. Slider topu için İSTİRİDYE resmini yükle
        // Projende "shell_icon.png" adında bir resim olduğundan emin ol
        ImageIcon sliderIcon = new ImageIcon("level_active.png");
        Image sliderImg = (sliderIcon.getImage() != null) ? sliderIcon.getImage() : null;

        // 2. Yeni Deniz Temalı Tasarımı Uygula
        slider.setUI(new SeaSliderUI(slider, sliderImg));

        // 3. Arka planı şeffaf yap
        slider.setOpaque(false);

        slider.setBackground(new Color(189, 237, 255));

        return slider;
    }
}

// --- YENİ TASARIM SINIFI (Altın Çerçeve & Mavi Dolum) ---
class SeaSliderUI extends BasicSliderUI {

    private Image thumbImage;
    private static final int TRACK_HEIGHT = 24; // Barın kalınlığı
    private static final int TRACK_ARC = 24;    // Köşelerin yuvarlaklığı

    public SeaSliderUI(JSlider b, Image image) {
        super(b);
        this.thumbImage = image;
    }

    // Noktalı odak çizgisini iptal et
    @Override
    public void paintFocus(Graphics g) {
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Barın dikey konumu (Ortalamak için)
        int trackY = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
        int w = trackRect.width;
        int h = TRACK_HEIGHT;

        // --- A) ALTIN ÇERÇEVE (GOLD FRAME) ---
        GradientPaint goldGradient = new GradientPaint(
                0, trackY, new Color(255, 235, 120), // Açık Altın
                0, trackY + h, new Color(210, 140, 30) // Koyu Altın
        );
        g2d.setPaint(goldGradient);
        // Dış çerçeveyi çiz
        g2d.fillRoundRect(trackRect.x, trackY - 2, w, h + 4, TRACK_ARC + 4, TRACK_ARC + 4);

        // --- B) İÇ KISIM (BOŞ TRACK - KOYU MAVİ) ---
        g2d.setColor(new Color(0, 60, 120)); // Derin okyanus mavisi
        // Biraz içeriden başlatıyoruz
        g2d.fillRoundRect(trackRect.x + 4, trackY + 2, w - 8, h - 4, TRACK_ARC, TRACK_ARC);

        // --- C) DOLU KISIM (SOL TARAF - CAM GÖBEĞİ) ---
        int fillWidth = thumbRect.x - trackRect.x + (thumbRect.width / 2);

        // Sınır koruması
        if (fillWidth < TRACK_ARC) fillWidth = TRACK_ARC;
        if (fillWidth > w) fillWidth = w;

        // Parlak turkuaz gradient
        GradientPaint cyanGradient = new GradientPaint(
                0, trackY, new Color(100, 240, 255), // Çok açık mavi (üst)
                0, trackY + h, new Color(0, 150, 220) // Normal mavi (alt)
        );

        // Clip kullanarak sadece belirli alanı boya (Taşmayı önler)
        Shape oldClip = g2d.getClip();
        RoundRectangle2D innerTrack = new RoundRectangle2D.Float(trackRect.x + 4, trackY + 2, w - 8, h - 4, TRACK_ARC, TRACK_ARC);
        g2d.setClip(innerTrack);

        g2d.setPaint(cyanGradient);
        g2d.fillRect(trackRect.x, trackY, fillWidth, h);

        g2d.setClip(oldClip);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (thumbImage != null) {
            // Resmi çiz (Boyutu 45x40 ayarladım, istiridye için ideal)
            g2d.drawImage(thumbImage, thumbRect.x, thumbRect.y - 8, 45, 40, null);
        } else {
            // Resim yoksa Altın Daire çiz
            g2d.setColor(new Color(255, 200, 50));
            g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
        }
    }

    @Override
    protected Dimension getThumbSize() {
        // İstiridye biraz büyük olduğu için alanı genişlettik
        return new Dimension(45, 40);
    }
}