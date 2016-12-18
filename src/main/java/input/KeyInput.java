package input;

import intfs.Callback;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by GS-0628 on 12/16/16.
 */
public class KeyInput implements KeyListener {
    private Callback<KeyEvent> keyPressedCallback;
    private Callback<KeyEvent> keyReleasedCallback;

    public void whenKeyPressed(Callback<KeyEvent> callback) {
        keyPressedCallback = callback;
    }

    public void whenKeyReleased(Callback<KeyEvent> callback) {
        keyReleasedCallback = callback;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressedCallback.call(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyReleasedCallback.call(e);
    }
}
