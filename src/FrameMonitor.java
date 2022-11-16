import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class FrameMonitor {
    static List<Preferences> prefs =  new ArrayList<Preferences>();
    static Preferences pref;
  public static void registerFrame(JFrame frame, String frameUniqueId,
                                   int defaultX, int defaultY, int defaultW, int defaultH) {
      pref = Preferences.userRoot()
                                     .node(frameUniqueId);
      frame.setLocation(getFrameLocation(pref, defaultX, defaultY));
      frame.setSize(getFrameSize(pref, defaultW, defaultH));
      
      CoalescedEventUpdater updater = new CoalescedEventUpdater(400,
              () -> updatePref(frame, pref));

      prefs.add(pref);
      frame.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
              updater.update();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
              updater.update();
          }
      });
  }

  private static void updatePref(JFrame frame, Preferences pref) {
              System.out.println("Updating preferences");
      Point location = frame.getLocation();
      pref.putInt("x", location.x);
      pref.putInt("y", location.y);
      Dimension size = frame.getSize();
      pref.putInt("w", size.width);
      pref.putInt("h", size.height);
  }

  private static Dimension getFrameSize(Preferences pref, int defaultW, int defaultH) {
      int w = pref.getInt("w", defaultW);
      int h = pref.getInt("h", defaultH);
      return new Dimension(w, h);
  }

  private static Point getFrameLocation(Preferences pref, int defaultX, int defaultY) {
      int x = pref.getInt("x", defaultX);
      int y = pref.getInt("y", defaultY);
      return new Point(x, y);
  }
}