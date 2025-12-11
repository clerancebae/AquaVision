import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MissionPanel extends JPanel {
    private JPanel strtPanel;
    // --- AYARLAR ---
    private static final int BUTTON_SIZE = 60;
    private static final int FONT_SIZE = 20;

    private final int[][] levelPositions = {
            {170, 520}, // Level 1
            {370, 340}, // Level 2
            {170, 220}, // Level 3
            {350, 100}, // Level 4
            {260, 20}, // Level 5
    };

    private List<JButton> levelButtons = new ArrayList<>();
    private Image backgroundImage;
    private ImageIcon shellIcon;  // Açık (Renkli)
    private ImageIcon lockedIcon;
    private ImageIcon returnBtn; // Kapalı (Gri/Kilitli)

    public MissionPanel(JPanel strtPanel) {
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(null);
        this.strtPanel = strtPanel;

        loadImages();
        initButtons();
    }


    private void loadImages() {
        // Konsola bilgi yazdıralım (Hata ayıklamak için)
        System.out.println("--- RESİM YÜKLEME BAŞLADI ---");

        // 1. Arkaplan
        backgroundImage = safeLoadImage("MissionBackground.png");

        // 2. Aktif İkon (level_active.png)
        Image imgActive = safeLoadImage("level_active.png");
        if (imgActive != null) {
            Image scaled = imgActive.getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            shellIcon = new ImageIcon(scaled);
            System.out.println("BAŞARILI: level_active.png yüklendi.");
        } else {
            System.err.println("HATA: level_active.png BULUNAMADI! (Butonlar renkli daire olacak)");
            shellIcon = null; // Resim yoksa null kalsın, aşağıda renk ile çizeceğiz
        }

        // 3. Kilitli İkon (level_locked.png)
        Image imgLocked = safeLoadImage("level_locked.png");
        if (imgLocked != null) {
            Image scaled = imgLocked.getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            lockedIcon = new ImageIcon(scaled);
            System.out.println("BAŞARILI: level_locked.png yüklendi.");
        } else {
            // Kilit resmi yoksa ve aktif resim varsa, aktif resmi grileştirip kullanalım
            if (shellIcon != null) {
                System.out.println("BİLGİ: level_locked.png yok, aktif resim grileştiriliyor.");
                lockedIcon = createGrayIcon(shellIcon);
            } else {
                lockedIcon = null;
            }
        }
    }

    // Hem src klasörüne hem proje ana dizinine bakan güvenli yükleyici
    private Image safeLoadImage(String fileName) {
        try {
            // 1. Önce classpath (src/resources) içine bak
            URL url = getClass().getResource("/" + fileName);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            // 2. Bulamazsa direkt dosya yoluna bak
            return new ImageIcon(fileName).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    // Otomatik grileştirme metodu
    private ImageIcon createGrayIcon(ImageIcon icon) {
        Image img = icon.getImage();
        ImageFilter filter = new GrayFilter(true, 50);
        java.awt.image.ImageProducer producer = new java.awt.image.FilteredImageSource(img.getSource(), filter);
        Image grayImg = Toolkit.getDefaultToolkit().createImage(producer);
        return new ImageIcon(grayImg);
    }

    private void initButtons() {

        int maxUnlockedLevel = 1;
        JButton returnButton = new  JButton();
        returnButton.setBounds(10, 10, 50, 50);

        Image img = safeLoadImage("return.png");
        if (img != null) {
            Image scaled = img.getScaledInstance(100, 50, Image.SCALE_SMOOTH);
            returnButton.setIcon(new ImageIcon(scaled));
        }

        returnButton.setContentAreaFilled(false);
        returnButton.setBorderPainted(false);
        returnButton.setFocusPainted(false);

        returnButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.getContentPane().removeAll();

            frame.getContentPane().add(strtPanel);

            frame.revalidate();
            frame.repaint();

            strtPanel.requestFocusInWindow();
        });

        this.add(returnButton);

        for (int i = 0; i < levelPositions.length; i++) {
            JButton btn = new  JButton();
            int levelNum = i + 1;
            btn.setBounds(levelPositions[i][0], levelPositions[i][1], BUTTON_SIZE, BUTTON_SIZE);


            btn.setHorizontalTextPosition(JButton.CENTER);
            btn.setVerticalTextPosition(JButton.CENTER);
            btn.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
            btn.setForeground(Color.WHITE);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);


            if (levelNum <= maxUnlockedLevel) {


                if (shellIcon != null) {
                    btn.setIcon(shellIcon);
                } else {

                    btn.setBackground(Color.GREEN);
                    btn.setOpaque(true);
                }

                btn.setText(String.valueOf(levelNum));
                btn.setEnabled(true);

                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Level " + levelNum + " oynanıyor!");
                    }
                });

            } else {

                if (lockedIcon != null) {
                    btn.setIcon(lockedIcon);
                    btn.setDisabledIcon(lockedIcon); // Disable olunca bu resmi göster
                } else {
                    // Resim yoksa GRİ DAİRE yap
                    btn.setBackground(Color.GRAY);
                    btn.setOpaque(true);
                }

                btn.setText(""); // Yazı yok
                btn.setEnabled(false); // Tıklanamaz
            }

            this.add(btn);
            levelButtons.add(btn);
        }
    } // we can use iterator in this code.

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            // Arkaplan resmi yoksa MAVİ ekran
            g.setColor(new Color(100, 200, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}