package me.afoolslove.metalmaxre.desktop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * 鼠标滚轮上滑下滑 切换到上一个值或下一个值
 * *必须获得焦点，滑动才能有效
 */
public class ValueMouseWheelListener implements MouseWheelListener {
    private static final ValueMouseWheelListener INSTANCE = new ValueMouseWheelListener();

    public static ValueMouseWheelListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Object source = e.getSource();
        if (source instanceof JSpinner spinner) {
            if (!spinner.isEnabled() || !containsFocus(spinner)) {
                return;
            }

            int wheelRotation = e.getWheelRotation();
            if (wheelRotation == 1) {
                spinner.setValue(spinner.getNextValue());
            } else if (wheelRotation == -1) {
                spinner.setValue(spinner.getPreviousValue());
            }
        }
    }


    private boolean containsFocus(JSpinner spinner) {
        final Component focusOwner = KeyboardFocusManager.
                getCurrentKeyboardFocusManager().getFocusOwner();
        return isParentOf(spinner, focusOwner);
    }

    private boolean isParentOf(JSpinner spinner, Component comp) {
        synchronized (spinner.getTreeLock()) {
            while (comp != null && comp != spinner && !(comp instanceof Window)) {
                comp = comp.getParent();
            }
            return (comp == spinner);
        }
    }
}
