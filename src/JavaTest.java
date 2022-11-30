import com.openfin.desktop.snapshot.SnapshotSource;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class JavaTest implements ActionListener{
	static InteropTest i = new InteropTest();
	JLabel ticker = new JLabel("Empty");
	JComboBox tickersCB;
	JComboBox JoinChannelCB;
	String platform;
	public JavaTest() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		JLabel btnLabelListen = new JLabel("Select to listen to Channel");
		JLabel btnLabelSet = new JLabel("Select to set Channel:");
		JLabel LabelTicker = new JLabel("Select ticker symbol:");
		JLabel labelApps = new JLabel("Apps:");
		JLabel labelWorkspace = new JLabel("Workspace:");
		platform = JOptionPane.showInputDialog("Enter Platform id:");

		String[] tickers = { "AAPL", "MSFT", "GOOG", "TSLA" };
		tickersCB = new JComboBox(tickers);
		tickersCB.putClientProperty("ticker", true);
		tickersCB.setSelectedIndex(0);
		tickersCB.addActionListener(this);

		String[] channelColors = { "red", "green", "pink", "orange", "purple", "yellow" };

		JoinChannelCB = new JComboBox(channelColors);
		JoinChannelCB.putClientProperty("join", true);
		JoinChannelCB.setSelectedIndex(1);
		JoinChannelCB.addActionListener(this);

		JComboBox SetChannelCB = new JComboBox(channelColors);
		SetChannelCB.putClientProperty("join", false);
		SetChannelCB.setSelectedIndex(1);
		SetChannelCB.addActionListener(this);

		String[] appStrings = { "App 1", "App 2", "App 3" };
		JComboBox appsCB = new JComboBox(appStrings);
		appsCB.putClientProperty("app", true);
		appsCB.setSelectedIndex(-1);
		appsCB.addActionListener(this);

		panel.setBorder(BorderFactory.createEmptyBorder(10,70,30,70));
		panel.setLayout(new GridLayout(0,1));

		panel.add(ticker);
		panel.add(LabelTicker);
		panel.add(tickersCB);
		panel.add(btnLabelListen);
		panel.add(JoinChannelCB);
		panel.add(btnLabelSet);
		panel.add(SetChannelCB);
		panel.add(labelApps);
		panel.add(appsCB);


		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}

	public static void createFrame(String name, int x, int y, int w, int h) {
		JFrame frame = new JFrame("Remembering Window Size and Location");
		// set the frame's location and size
		frame.setBounds(x, y, w, h);
		frame.setName(name);
		FrameMonitor.registerFrame(frame, frame.getName(),
				x, y, w, h);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		JavaTest jt = new JavaTest();
		try {
			i.setup(jt.platform);
			SnapshotSource snapshotSource = new SnapshotSource(i.desktopConnection);
			snapshotSource.initSnapshotSourceProviderAsync("customize-workspace", i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		try {
			if(cb.getClientProperty("join") != null && ((boolean) cb.getClientProperty("join"))) {
				i.joinAllGroups(JoinChannelCB.getSelectedItem().toString(),this);
			} else if(cb.getClientProperty("ticker") != null && (boolean) cb.getClientProperty("ticker")) {
				i.clientSetContext(JoinChannelCB.getSelectedItem().toString(), tickersCB.getSelectedItem().toString(), platform);
			}else if(cb.getClientProperty("app") != null && (boolean) cb.getClientProperty("app")) {
				createFrame(cb.getSelectedItem().toString(), 0, 0, 400, 400);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	public void updateTicker(JSONObject id) {
		ticker.setText(id.toString());
	}

}
