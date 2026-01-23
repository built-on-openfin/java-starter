package com.here.starter.java;

import java.awt.Frame;
import java.lang.System;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import com.openfin.desktop.*;
import com.openfin.desktop.snapshot.SnapshotSourceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openfin.desktop.interop.Context;
import com.openfin.desktop.interop.ContextGroupInfo;
import com.openfin.desktop.interop.Intent;
import com.openfin.desktop.interop.InteropClient;
import com.openfin.desktop.channel.*;

import javax.swing.JFrame;

public class Interop implements SnapshotSourceProvider {
    private static Logger logger = LoggerFactory.getLogger(Interop.class.getName());
    public static DesktopConnection desktopConnection;
    private Apps apps = new Apps();
    InteropClient client;

    public void setup(String platformId, String connectionUuid, Runnable onReadyCallback) throws Exception {
        logger.debug("starting");
        desktopConnection = new DesktopConnection(connectionUuid);
        RuntimeConfiguration cfg = new RuntimeConfiguration();
        cfg.setRuntimeVersion("stable");
        cfg.setAdditionalRuntimeArguments(" --v=1 ");
        desktopConnection.connect(cfg, new DesktopStateListener() {

            @Override
            public void onReady() {
                logger.info("Desktop Connection Ready");
                desktopConnection.getInterop().connect(platformId).thenAccept(interopClient -> {
                    client = interopClient;
                    if (onReadyCallback != null) {
                        onReadyCallback.run();
                    }
                });
            }

            @Override
            public void onClose(String error) {
                logger.info("Closing: " + error);
            }

            @Override
            public void onError(String reason) {
                logger.error("Desktop Connection Error: " + reason);
            }

            @Override
            public void onMessage(String message) {
            }

            @Override
            public void onOutgoingMessage(String message) {

            }
        }, 60);
    }

    @Override
    public JSONObject getSnapshot() {
        JSONArray appsArray = new JSONArray();
        try {
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame instanceof JFrame) {
                    JFrame jFrame = (JFrame) frame;
                    var name = jFrame.getName();
                    if (name != null && !name.isEmpty() && !name.equals("frame0")) {
                        JSONObject appObject = new JSONObject();
                        appObject.put("appId", jFrame.getName());
                        appObject.put("title", jFrame.getTitle());
                        appObject.put("x", jFrame.getX());
                        appObject.put("y", jFrame.getY());
                        appObject.put("w", jFrame.getWidth());
                        appObject.put("h", jFrame.getHeight());
                        appsArray.put(appObject);
                    }
                }
            }
            return new JSONObject().put("snapshot", appsArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void applySnapshot(JSONObject snapshot) {
        try {
            Main.CloseAllWindows();
            JSONArray childWindows = (JSONArray) snapshot.get("snapshot");
            for (int i = 0; i < childWindows.length(); i++) {
                JSONObject app = (JSONObject) childWindows.get(i);
                Main.createApp(app.getString("appId"), app.getInt("x"), app.getInt("y"), app.getInt("w"),
                        app.getInt("h"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void createChannelClient(String platformId) throws JSONException {

        desktopConnection.getChannel(platformId.toLowerCase() + "-workspace-connection").connectAsync()
                .thenAccept(client -> {
                    client.addChannelListener(new ChannelListener() {
                        @Override
                        public void onChannelConnect(ConnectionEvent connectionEvent) {
                            logger.info("channel connected {}", connectionEvent.getChannelId());
                        }

                        @Override
                        public void onChannelDisconnect(ConnectionEvent connectionEvent) {
                            logger.info("channel disconnected {}", connectionEvent.getChannelId());
                        }
                    });

                    client.register("getApps", (action, payload, senderIdentity) -> {
                        try {
                            return apps.getAppDirectory();
                        } catch (Error e) {
                            throw new RuntimeException(e);
                        }
                    });

                    client.register("launchApp", (action, payload, senderIdentity) -> {
                        try {
                            var appId = ((JSONObject) payload).get("appId").toString();
                            Main.createApp(appId);
                            return null;
                        } catch (Error e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
    }

    public CompletionStage<ContextGroupInfo[]> clientGetContextGroupInfo() {
        // CSE-1141: Update Java Starter so it filters out context groups that do not
        // have meta data
        return client.getContextGroups().thenApply(groups -> {
            return java.util.Arrays.stream(groups)
                    .filter(group -> {
                        // Filter out groups that do not have a color set in their display metadata
                        return group.getDisplayMetadata().getColor() != null
                                && !group.getDisplayMetadata().getColor().isEmpty();
                    })
                    .toArray(ContextGroupInfo[]::new);
        });
    }

    public void clientSetContext(String group, String ticker, String platformName) throws Exception {
        Context context = new Context();
        JSONObject contextId = new JSONObject();
        contextId.put("ticker", ticker);
        context.setId(contextId);
        var name = "Unknown";
        if (ticker.equals("AAPL")) {
            name = "Apple Inc.";
        } else if (ticker.equals("MSFT")) {
            name = "Microsoft Corporation";
        } else if (ticker.equals("GOOGL")) {
            name = "Alphabet Inc.";
        } else if (ticker.equals("TSLA")) {
            name = "Tesla Inc.";
        }
        context.setName(name);
        context.setType("fdc3.instrument");
        CompletionStage<Void> setContextFuture = client.setContext(context);

        setContextFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    public void joinContextGroup(String groupId) {
        client.joinContextGroup(groupId).thenAccept(contextGroupInfo -> {
            logger.info("Joined context group");
        });
    }

    public void clientFireIntent(String intent, String type, String typeValue, String platformName) throws Exception {
        Context context = new Context();
        JSONObject contextId = new JSONObject();
        contextId.put("ticker", typeValue);
        context.setId(contextId);
        var name = "Unknown";
        if (typeValue.equals("AAPL")) {
            name = "Apple Inc.";
        } else if (typeValue.equals("MSFT")) {
            name = "Microsoft Corporation";
        } else if (typeValue.equals("GOOGL")) {
            name = "Alphabet Inc.";
        } else if (typeValue.equals("TSLA")) {
            name = "Tesla Inc.";
        }
        context.setName(name);
        context.setType("fdc3.instrument");

        Intent intentToRaise = new Intent();
        intentToRaise.setContext(context);
        intentToRaise.setName(intent);
        CompletionStage<Void> fireIntentFuture = client.fireIntent(intentToRaise);

        fireIntentFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    public CompletionStage<Void> addContextListener(Main JT) {
        return client.addContextListener(context -> {
            System.out.println("Received context: " + context.getId());
            if (context.getId().has("ticker")) {
                JT.updateTicker(context.getId());
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public CompletionStage<Void> addIntentListener(String platformName, Main JT) {
        return client.registerIntentListener("ViewInstrument", intent -> {
            Context context = intent.getContext();
            System.out.println("Received intent: " + intent.getName());
            System.out.println("Context: " + context.getId());
            JT.updateTicker(context.getId());
            JT.updateReceivedIntent(intent.getName());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
