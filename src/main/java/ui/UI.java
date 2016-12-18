package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by GS-0628 on 12/16/16.
 */
public class UI extends JPanel {
    private JFrame window;
    private char[] videoBuffer;
    private String title = "Chip 8 emulator";

    public int width = 64, height = 32, scale = 10;

    public UI(char[] videoBuffer) {
        this.videoBuffer = videoBuffer;
        window = new JFrame(title);
        window.setSize(width * scale + 5, height * scale + 30);
        window.setResizable(false);
        window.setVisible(true);
        window.setLayout(new BorderLayout());
        window.add(this, BorderLayout.CENTER);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public synchronized void addKeyListener(KeyListener l) {
        window.addKeyListener(l);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for(int y = 0; y < 32; ++y) {
            for (int x = 0; x < 64; ++x) {
                g.setColor(videoBuffer[(y * 64) + x] == 0 ? Color.black : Color.orange);
                g.fillRect((x * scale), (y * scale), (x * scale) + scale, (y * scale) + scale);
            }
        }
    }
}
