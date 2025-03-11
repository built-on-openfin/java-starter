package com.openfin.starter.java;

import com.openfin.desktop.ActionEvent;
import com.openfin.desktop.snapshot.SnapshotSource;

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main implements ActionListener {
	static Interop interopConnection = new Interop();
	JLabel ticker = new JLabel("No Ticker Passed");
	JLabel receivedIntent = new JLabel("No Intent Received");
	static int x = 0;
	static int y = 0;
	static int width = 400;
	static int height = 800;
	static int maxWidth = 400;
	static int maxHeight = 800;
	static int minWidth = 400;
	static int minHeight = 400;

	static List<JFrame> windows = new ArrayList<JFrame>();

	JComboBox tickersCB;
	JComboBox JoinChannelCB;
	String platformUuid;
	String runtimeUuid;
	boolean registerIntentListener = false;
	JTextArea logArea;

	public Main() {
		JFrame frame = new JFrame("Java Starter");
		frame.setBounds(x, y, width, height);
		// Set the default close operation
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set the maximum size of the frame
        frame.setMaximumSize(new Dimension(maxWidth, maxHeight));
		frame.setMinimumSize(new Dimension(minWidth, minHeight));
		// Add a ComponentListener to enforce maximum size
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getSize();
                int width = Math.min(size.width, maxWidth);
                int height = Math.min(size.height, maxHeight);
                frame.setSize(new Dimension(width, height));
            }
        });
		JPanel panel = new JPanel();
		JLabel btnLabelListen = new JLabel("Select to listen to Channel");
		JLabel btnLabelSet = new JLabel("Select to set Channel:");
		JLabel LabelTicker = new JLabel("Select ticker symbol:");
		JLabel labelApps = new JLabel("Apps:");
		platformUuid = CommandLineOptions.getSpecifiedWorkspaceUUID();
		runtimeUuid = CommandLineOptions.getSpecifiedNativeUUID();
	
		if (runtimeUuid == null || runtimeUuid.isEmpty()) {
			runtimeUuid = "interop-test-desktop";
		}
		if (platformUuid == null || platformUuid.isEmpty()) {
			do {
				platformUuid = JOptionPane.showInputDialog("Enter Platform id:");
			} while (platformUuid == null || platformUuid.isEmpty());
		}

		String[] tickers = { "AAPL", "MSFT", "GOOGL", "TSLA" };
		tickersCB = new JComboBox(tickers);
		tickersCB.putClientProperty("ticker", true);
		tickersCB.setSelectedIndex(-1);
		tickersCB.addActionListener(this);

		String[] channelColors = { "red", "green", "pink", "orange", "purple", "yellow" };

		JoinChannelCB = new JComboBox(channelColors);
		JoinChannelCB.putClientProperty("join", true);
		JoinChannelCB.setSelectedIndex(-1);
		JoinChannelCB.addActionListener(this);

		JComboBox SetChannelCB = new JComboBox(channelColors);
		SetChannelCB.putClientProperty("join", false);
		SetChannelCB.setSelectedIndex(-1);
		SetChannelCB.addActionListener(this);

		String[] appStrings = { "App 1", "App 2", "App 3" };
		JComboBox appsCB = new JComboBox(appStrings);
		appsCB.putClientProperty("app", true);
		appsCB.setSelectedIndex(-1);
		appsCB.addActionListener(this);

		 // Create a button and add an action listener
        JButton fireIntent = new JButton("FireIntent");
        fireIntent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					interopConnection.clientFireIntent("ViewChart", "fdc3.instrument", "AAPL", platformUuid);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
        });

		// Create a text area for logging
		logArea = new JTextArea(40, 50);
		 
		logArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(logArea);
		scrollPane.setSize(300,400);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 70, 30, 70));
		panel.setLayout(new GridLayout(0, 1));
		panel.add(receivedIntent);
		panel.add(ticker);
		panel.add(LabelTicker);
		panel.add(tickersCB);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		panel.add(btnLabelListen);
		panel.add(JoinChannelCB);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		panel.add(btnLabelSet);
		panel.add(SetChannelCB);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		panel.add(labelApps);
		panel.add(appsCB);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		panel.add(fireIntent);
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		panel.add(scrollPane); // Add the scroll pane containing the text area
		panel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between components
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		CommandLineOptions.setCommandLineArgs(args);
		var workspaceUUID = CommandLineOptions.getSpecifiedWorkspaceUUID();
		var nativeUUID = CommandLineOptions.getSpecifiedNativeUUID();
		var registerIntents = CommandLineOptions.getRegisterIntents();
		System.out.println("Workspace UUID: " + workspaceUUID);
		System.out.println("Native UUID: " + nativeUUID);
		System.out.println("Register Intents: " + registerIntents);
		Main mainApplication = new Main();
		mainApplication.logMessage("Workspace UUID: " + workspaceUUID);
		mainApplication.logMessage("Native UUID: " + nativeUUID);
		mainApplication.logMessage("Register Intents: " + registerIntents);
		mainApplication.updateReceivedIntent(workspaceUUID);
		try {
			interopConnection.setup(mainApplication.platformUuid, mainApplication.runtimeUuid);
			SnapshotSource snapshotSource = new SnapshotSource(interopConnection.desktopConnection);
			snapshotSource.initSnapshotSourceProviderAsync(mainApplication.runtimeUuid, interopConnection);
			if(registerIntents) {
				interopConnection.addIntentListener(mainApplication.platformUuid, mainApplication);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logMessage(String message) {
        logArea.append(message + "\n");
    }

	public static void createFrame(String name, int x, int y, int width, int height, int maxWidth, int maxHeight, int minWidth, int minHeight) {
		JFrame frame = new JFrame("Java Starter: Child Window");
		// set the frame's location and size
		frame.setBounds(x, y, width, height);
		frame.setName(name);
		// Set the default close operation
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Set the maximum size of the frame
        frame.setMaximumSize(new Dimension(maxWidth, maxHeight));
		frame.setMinimumSize(new Dimension(minWidth, minHeight));
		FrameMonitor.registerFrame(frame, frame.getName(),
				x, y, width, height);
		windows.add(frame);
		frame.setVisible(true);
		// Add a WindowListener to remove the frame from the windows list when it is closed
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				windows.remove(frame);
			}
		});
		frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getSize();
                int width = Math.min(size.width, maxWidth);
                int height = Math.min(size.height, maxHeight);
                frame.setSize(new Dimension(width, height));
            }
        });
	}

	public static void CloseAllWindows() {
		for (JFrame w : windows) {
			w.setVisible(false);
			w.dispose();
		}
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		JComboBox cb = (JComboBox) e.getSource();
		try {
			if (cb.getClientProperty("join") != null && ((boolean) cb.getClientProperty("join"))) {
				interopConnection.joinAllGroups(JoinChannelCB.getSelectedItem().toString(), this);
			} else if (cb.getClientProperty("ticker") != null && (boolean) cb.getClientProperty("ticker")) {
				interopConnection.clientSetContext(JoinChannelCB.getSelectedItem().toString(),
						tickersCB.getSelectedItem().toString(), platformUuid);
			} else if (cb.getClientProperty("app") != null && (boolean) cb.getClientProperty("app")) {
				createFrame(cb.getSelectedItem().toString(), x, y, width, height, maxWidth, maxHeight, minWidth, minHeight);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void updateTicker(JSONObject id) {
		try {
			String tickerValue = id.getString("ticker");
			ticker.setText("Passed Ticker: " + tickerValue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void updateReceivedIntent(String id) {
		try {
			receivedIntent.setText("Received Intent: " + id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
