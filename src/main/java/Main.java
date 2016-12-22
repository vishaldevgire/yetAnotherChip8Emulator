import chip8.Chip8;
import input.KeyInput;
import intfs.Callback;
import ui.UI;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Main {

    public static void main(String args[]) throws IOException, InterruptedException {
        if (args.length != 1 || !Paths.get(args[0]).toFile().isFile()) {
            System.out.print("\n\tUsage: <emulator.jar> <chip8_game_file>\n");
            System.exit(1);
        }

        // Initialize the chip8.Chip8 system and load the game into the memory
        Chip8 chip8 = Chip8.getInstance();
        byte[] bytes = Files.readAllBytes(Paths.get(new File(args[0]).getAbsolutePath()));
        chip8.loadGame(bytes);

        KeyInput keyInput = new KeyInput();
        keyInput.whenKeyPressed(keyPressedHandler(chip8));
        keyInput.whenKeyReleased(keyReleasedHandler(chip8));

        UI window = new UI(chip8.gfx);
        window.addKeyListener(keyInput);

        Runnable gameLoop = () -> {
            while (true) {
                chip8.emulateCycle();
                if (chip8.drawFlag) {
                    window.repaint();
                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread gameThread = new Thread(gameLoop);
        gameThread.start();
        gameThread.join();
    }

    private static Callback<KeyEvent> keyReleasedHandler(final Chip8 chip8) {
        return keyEvent -> {
            char key = keyEvent.getKeyChar();

            if (key == '1') chip8.key[0x1] = 0;
            else if (key == '2') chip8.key[0x2] = 0; //down
            else if (key == '3') chip8.key[0x3] = 0;
            else if (key == '4') chip8.key[0xC] = 0;

            else if (key == 'q') chip8.key[0x4] = 0; //left
            else if (key == 'w') chip8.key[0x5] = 0; // fire/action
            else if (key == 'e') chip8.key[0x6] = 0; //right
            else if (key == 'r') chip8.key[0xD] = 0;

            else if (key == 'a') chip8.key[0x7] = 0;
            else if (key == 's') chip8.key[0x8] = 0; //up
            else if (key == 'd') chip8.key[0x9] = 0;
            else if (key == 'f') chip8.key[0xE] = 0;

            else if (key == 'z') chip8.key[0xA] = 0;
            else if (key == 'x') chip8.key[0x0] = 0;
            else if (key == 'c') chip8.key[0xB] = 0;
            else if (key == 'v') chip8.key[0xF] = 0;
        };
    }

    private static Callback<KeyEvent> keyPressedHandler(final Chip8 chip8) {

        return keyEvent -> {
            char key = keyEvent.getKeyChar();
            //System.out.print("\n KeyPressed: " + key);
            if (key == 27) {   // esc
                System.exit(0);
            }

            if (key == '1') chip8.key[0x1] = 1;
            else if (key == '2') chip8.key[0x2] = 1;
            else if (key == '3') chip8.key[0x3] = 1;
            else if (key == '4') chip8.key[0xC] = 1;

            else if (key == 'q') chip8.key[0x4] = 1;
            else if (key == 'w') chip8.key[0x5] = 1;
            else if (key == 'e') chip8.key[0x6] = 1;
            else if (key == 'r') chip8.key[0xD] = 1;

            else if (key == 'a') chip8.key[0x7] = 1;
            else if (key == 's') chip8.key[0x8] = 1;
            else if (key == 'd') chip8.key[0x9] = 1;
            else if (key == 'f') chip8.key[0xE] = 1;

            else if (key == 'z') chip8.key[0xA] = 1;
            else if (key == 'x') chip8.key[0x0] = 1;
            else if (key == 'c') chip8.key[0xB] = 1;
            else if (key == 'v') chip8.key[0xF] = 1;
        };
    }
}