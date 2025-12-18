package jgl.ui;

import jgl.Keyboard;
import jgl.Mouse;
import jgl.event.events.KeyPressEvent;
import jgl.event.events.KeyReleaseEvent;
import jgl.event.events.MouseDragEvent;
import jgl.event.events.MouseMoveEvent;
import jgl.event.events.MousePressEvent;
import jgl.event.events.MouseReleaseEvent;
import jgl.event.events.MouseScrollEvent;
import jgl.event.listeners.KeyListener;
import jgl.event.listeners.MouseListener;
import jgl.event.listeners.MouseScrollListener;

public class UI extends ElementContainer {

    private Element focused;

    public UI() {
        Keyboard.addKeyListener(new UIKeyListener());
        Mouse.addMouseListener(new UIMouseListener());
        Mouse.addScrollListener(new UIScrollListener());
    }

    public void setFocusTo(Element focused) {
        if (focused.isFocusable())
            this.focused = focused;
    }

    public Element getFocused() {
        return focused;
    }

    @Override
    public Element getParent() {
        return this;
    }

    private class UIKeyListener implements KeyListener {

        @Override
        public void keyPressed(KeyPressEvent event) {
            short key = event.getKey();
            if (key == Keyboard.TAB) {
                if (focused instanceof ElementContainer container) {

                }
            }
        }

        @Override
        public void keyReleased(KeyReleaseEvent event) {

        }
    }

    private class UIMouseListener implements MouseListener {
        @Override
        public void mousePressed(MousePressEvent event) {

        }

        @Override
        public void mouseReleased(MouseReleaseEvent event) {

        }

        @Override
        public void mouseDragged(MouseDragEvent event) {

        }

        @Override
        public void mouseMoved(MouseMoveEvent event) {

        }
    }

    private class UIScrollListener implements MouseScrollListener {

        @Override
        public void mouseScrolled(MouseScrollEvent event) {

        }
    }
}
