package com.openfin.starter.java;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

public class FrameMonitor {
    static Preferences pref;

    public static void init() {
        pref = Preferences.userRoot().node("FrameMonitor");
    }
  public static void registerFrame(JFrame frame, String frameUniqueId,
                                   int defaultX, int defaultY, int defaultW, int defaultH) {
      init();
      pref.node(frame.getName()).putInt("open", 1);
      CoalescedEventUpdater updater = new CoalescedEventUpdater(400,
              () -> updatePref(frame, pref));

      frame.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
              updater.update();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
              updater.update();
          }

          @Override
          public void componentHidden(ComponentEvent e) {
              FrameMonitor.pref.node(frame.getName()).putInt("open", 0);
          }

      });
  }

  private static void updatePref(JFrame frame, Preferences pref) {
      System.out.println("Updating preferences");
      Point location = frame.getLocation();
      pref.node(frame.getName()).putInt("x", location.x);
      pref.node(frame.getName()).putInt("y", location.y);
      Dimension size = frame.getSize();
      pref.node(frame.getName()).putInt("w", size.width);
      pref.node(frame.getName()).putInt("h", size.height);
  }
}