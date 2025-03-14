package com.openfin.starter.java;
import com.openfin.desktop.snapshot.SnapshotSource;
import com.openfin.desktop.interop.ContextGroupInfo;
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
import java.util.concurrent.CompletionStage;

public class Main {
    static Interop interopConnection = new Interop();
    static int x = 0;
    static int y = 0;
    static int width = 400;
    static int height = 800;
    static int maxWidth = 400;
    static int maxHeight = 800;
    static int minWidth = 400;
    static int minHeight = 400;

    static List<JFrame> windows = new ArrayList<>();

    JComboBox<String> tickersCB;
    JComboBox<String> JoinChannelCB;
    JComboBox<String> appsCB;
    String platformUuid;
    String runtimeUuid;
    boolean registerIntentListener = false;
    JTextArea logArea;
    boolean registerIntents;

    public Main() {
        JFrame frame = new JFrame("Java Starter");
        frame.setBounds(x, y, width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMaximumSize(new Dimension(maxWidth, maxHeight));
        frame.setMinimumSize(new Dimension(minWidth, minHeight));
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getSize();
                int width = Math.min(size.width, maxWidth);
                int height = Math.min(size.height, maxHeight);
                frame.setSize(new Dimension(width, height));
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel btnLabelListen = new JLabel("Channel:");
        JLabel LabelTicker = new JLabel("Ticker:");
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

        String[] tickers = {"AAPL", "MSFT", "GOOGL", "TSLA"};
        tickersCB = new JComboBox<>(tickers);
        tickersCB.putClientProperty("ticker", true);
        tickersCB.setSelectedIndex(-1);

        // Initialize JoinChannelCB with an empty array
        JoinChannelCB = new JComboBox<>(new String[]{});
        JoinChannelCB.putClientProperty("join", true);
        JoinChannelCB.setSelectedIndex(-1);

        String[] appStrings = {"App 1", "App 2", "App 3"};
        appsCB = new JComboBox<>(appStrings);
        appsCB.putClientProperty("app", true);
        appsCB.setSelectedIndex(-1);

        JButton setContextButton = new JButton("Set Context");
        setContextButton.setEnabled(false); // Initially disabled
        setContextButton.addActionListener(e -> {
            try {
                interopConnection.clientSetContext(JoinChannelCB.getSelectedItem().toString(),
                        tickersCB.getSelectedItem().toString(), platformUuid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // Enable the setContextButton only when an item is selected in both tickersCB and JoinChannelCB
        ActionListener enableSetContextButtonListener = e -> setContextButton.setEnabled(
                tickersCB.getSelectedIndex() != -1 && JoinChannelCB.getSelectedIndex() != -1);
        tickersCB.addActionListener(enableSetContextButtonListener);
        JoinChannelCB.addActionListener(enableSetContextButtonListener);

        JButton fireIntent = new JButton("Fire Intent");
        fireIntent.setEnabled(false); // Initially disabled
        fireIntent.addActionListener(e -> {
            try {
                interopConnection.clientFireIntent("ViewChart", "fdc3.instrument", tickersCB.getSelectedItem().toString(), platformUuid);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // Enable the fireIntent button only when an item is selected in tickersCB and registerIntents is true
        tickersCB.addActionListener(e -> fireIntent.setEnabled(registerIntents && tickersCB.getSelectedIndex() != -1));

        // Create a button to open the app
        JButton openAppButton = new JButton("Open App");
        openAppButton.setEnabled(false); // Initially disabled
        openAppButton.addActionListener(e -> {
            JComboBox<?> cb = appsCB;
            createFrame(cb.getSelectedItem().toString(), x, y, width, height, maxWidth, maxHeight, minWidth, minHeight);
        });

        // Enable the openAppButton only when an item is selected in appsCB
        appsCB.addActionListener(e -> openAppButton.setEnabled(appsCB.getSelectedIndex() != -1));

        // Create a button to clear the log area
        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(e -> logArea.setText(""));

        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setLineWrap(true); // Enable line wrap
        logArea.setWrapStyleWord(true); // Wrap at word boundaries
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        panel.add(LabelTicker, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(tickersCB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        panel.add(btnLabelListen, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(JoinChannelCB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        panel.add(labelApps, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(appsCB, gbc);

        // Add the setContextButton and fireIntent button on the same row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        panel.add(setContextButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(fireIntent, gbc);

        // Add the openAppButton and clearLogButton on the same row
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.5;
        panel.add(openAppButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(clearLogButton, gbc);

        // Add the logArea component to take up the full width and both columns
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0; // Ensure it takes the full width
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void fetchChannelColors() {
        try {
            CompletionStage<ContextGroupInfo[]> getContextFuture = interopConnection.clientGetContextGroupInfo();
            getContextFuture.thenAccept(contextGroupInfos -> {
                List<String> ids = new ArrayList<>();
                for (ContextGroupInfo groupInfo : contextGroupInfos) {
                    ids.add(groupInfo.getId());
                }
                SwingUtilities.invokeLater(() -> {
                    JoinChannelCB.setModel(new DefaultComboBoxModel<>(ids.toArray(new String[0])));
					JoinChannelCB.setSelectedIndex(-1);
					JoinChannelCB.addActionListener(e -> {
						interopConnection.joinAllGroups(JoinChannelCB.getSelectedItem().toString(), this);
					});
                });
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableDropdowns() {
        tickersCB.setEnabled(true);
        JoinChannelCB.setEnabled(true);
        appsCB.setEnabled(true);
    }

    public void updateTicker(JSONObject id) {
        try {
            String tickerValue = id.getString("ticker");
            this.logMessage("Received Ticker: " + tickerValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateReceivedIntent(String id) {
        this.logMessage("Received Intent: " + id);
    }

    public void logMessage(String message) {
        logArea.append(message + "\n");
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
        mainApplication.registerIntents = registerIntents; // Set the registerIntents flag
        mainApplication.logMessage("Workspace UUID: " + workspaceUUID);
        mainApplication.logMessage("Native UUID: " + nativeUUID);
        mainApplication.logMessage("Register Intents: " + registerIntents);
        try {
            interopConnection.setup(mainApplication.platformUuid, mainApplication.runtimeUuid, () -> {
                mainApplication.enableDropdowns();
                mainApplication.fetchChannelColors();
				mainApplication.logMessage("Connected to Runtime.");
				SnapshotSource snapshotSource = new SnapshotSource(interopConnection.desktopConnection);
				snapshotSource.initSnapshotSourceProviderAsync(mainApplication.runtimeUuid, interopConnection);
				if (registerIntents) {
					try {
						interopConnection.addIntentListener(mainApplication.platformUuid, mainApplication).thenRun(() -> {
                            mainApplication.logMessage("Intent listener registered.");
                        }).exceptionally(ex -> {
							mainApplication.logMessage("Intent listener not registered.");
                            ex.printStackTrace();
                            return null;
                        });
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFrame(String name, int x, int y, int width, int height, int maxWidth, int maxHeight, int minWidth, int minHeight) {
        JFrame frame = new JFrame("Java Starter: Child Window");
        frame.setBounds(x, y, width, height);
        frame.setName(name);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMaximumSize(new Dimension(maxWidth, maxHeight));
        frame.setMinimumSize(new Dimension(minWidth, minHeight));
        FrameMonitor.registerFrame(frame, frame.getName(), x, y, width, height);
        windows.add(frame);
        frame.setVisible(true);
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
}
