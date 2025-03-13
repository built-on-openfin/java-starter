package com.openfin.starter.java;
import java.io.*;
import java.lang.System;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import com.openfin.desktop.*;
import com.openfin.desktop.ClientIdentity;
import com.openfin.desktop.snapshot.SnapshotSourceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openfin.desktop.interop.Context;
import com.openfin.desktop.interop.ContextGroupInfo;
import com.openfin.desktop.interop.Intent;
import com.openfin.desktop.channel.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Interop implements SnapshotSourceProvider {
    private static Logger logger = LoggerFactory.getLogger(Interop.class.getName());
    public static DesktopConnection desktopConnection;
    private String platformId;

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
                if (onReadyCallback != null) {
                    onReadyCallback.run();
                }
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

        this.platformId = platformId;
        FrameMonitor.init();
        createChannelClient(platformId);
    }

    @Override
    public JSONObject getSnapshot() {
        JSONArray appsArray = new JSONArray();
        try {
            OutputStream os = new ByteArrayOutputStream();

            FrameMonitor.pref.exportSubtree(os);

            String payloadString = os.toString();
            payloadString = payloadString.replace("\\r\\n", "");
            payloadString = payloadString.replace("\\\"", "\"");
            payloadString = payloadString.replace("\\/", "/");
            payloadString = payloadString.replaceAll("<map/>", "");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            try {
                Document doc = builder.parse(new InputSource(new StringReader(payloadString)));
                NodeList apps = doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
                for (int i = 3; i < apps.getLength(); i += 2) {
                    JSONObject appObject = new JSONObject();
                    appObject.put("appId", apps.item(i).getAttributes().item(0).getNodeValue());
                    appObject.put("title", apps.item(i).getAttributes().item(0).getNodeValue());
                    appObject.put("x", apps.item(i).getChildNodes().item(1).getChildNodes().item(1).getAttributes()
                            .item(1).getNodeValue());
                    appObject.put("y", apps.item(i).getChildNodes().item(1).getChildNodes().item(3).getAttributes()
                            .item(1).getNodeValue());
                    appObject.put("w", apps.item(i).getChildNodes().item(1).getChildNodes().item(5).getAttributes()
                            .item(1).getNodeValue());
                    appObject.put("h", apps.item(i).getChildNodes().item(1).getChildNodes().item(7).getAttributes()
                            .item(1).getNodeValue());
                    appObject.put("open", apps.item(i).getChildNodes().item(1).getChildNodes().item(9).getAttributes()
                            .item(1).getNodeValue());
                    appObject.put("description", apps.item(i).getAttributes().item(0).getNodeValue());
                    appObject.put("manifestType", "connection");
                    appsArray.put(appObject);
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new JSONObject("{ snapshot: " + appsArray.toString() + " }");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void applySnapshot(JSONObject snapshot) {
        try {
            Main.CloseAllWindows();
            OutputStream os = new ByteArrayOutputStream();
            FrameMonitor.pref.exportSubtree(os);

            String payloadString = os.toString();
            payloadString = payloadString.replace("\\r\\n", "");
            payloadString = payloadString.replace("\\\"", "\"");
            payloadString = payloadString.replace("\\/", "/");
            payloadString = payloadString.replaceAll("<map/>", "");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            JSONArray ja = (JSONArray) snapshot.get("snapshot");
            try {
                Document doc = builder.parse(new InputSource(new StringReader(payloadString)));
                NodeList apps = doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
                JSONObject jo;
                for (int i = 3; i < apps.getLength(); i += 2) {
                    for (int j = 0; j < ja.length(); j++) {
                        jo = (JSONObject) ja.get(j);
                        if (jo.getString("appId").equals(apps.item(i).getAttributes().item(0).getNodeValue())) {
                            if (Integer.parseInt(jo.getString("open")) == 1)
                                Main.createFrame(apps.item(i).getAttributes().item(0).getNodeValue(),
                                        Integer.parseInt(jo.getString("x")), Integer.parseInt(jo.getString("y")),
                                        Integer.parseInt(jo.getString("w")), Integer.parseInt(jo.getString("h")),
                                        Integer.parseInt(jo.getString("w")), Integer.parseInt(jo.getString("h")),
                                        Integer.parseInt(jo.getString("w")), Integer.parseInt(jo.getString("h")));
                        }
                    }
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void createChannelClient(String platformId) throws JSONException {

        desktopConnection.getChannel(platformId.toLowerCase() + "-connection").connectAsync().thenAccept(client -> {
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
                JSONArray appsArray = new JSONArray();
                try {
                    OutputStream os = new ByteArrayOutputStream();
                    FrameMonitor.pref.exportSubtree(os);

                    String payloadString = os.toString();
                    payloadString = payloadString.replace("\\r\\n", "");
                    payloadString = payloadString.replace("\\\"", "\"");
                    payloadString = payloadString.replace("\\/", "/");
                    payloadString = payloadString.replaceAll("<map/>", "");
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = null;
                    try {
                        builder = factory.newDocumentBuilder();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }

                    try {
                        Document doc = builder.parse(new InputSource(new StringReader(payloadString)));
                        NodeList apps = doc.getElementsByTagName("root").item(0).getChildNodes().item(1)
                                .getChildNodes();
                        for (int i = 3; i < apps.getLength(); i += 2) {
                            JSONObject appObject = new JSONObject();
                            appObject.put("appId", apps.item(i).getAttributes().item(0).getNodeValue());
                            appObject.put("title", apps.item(i).getAttributes().item(0).getNodeValue());
                            appObject.put("description", apps.item(i).getAttributes().item(0).getNodeValue());
                            appObject.put("manifestType", "connection");
                            appsArray.put(appObject);
                        }
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (BackingStoreException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return appsArray;
            });

            client.register("launchApp", (action, payload, senderIdentity) -> {
                try {
                    OutputStream os = new ByteArrayOutputStream();
                    FrameMonitor.pref.exportSubtree(os);

                    String payloadString = os.toString();
                    payloadString = payloadString.replace("\\r\\n", "");
                    payloadString = payloadString.replace("\\\"", "\"");
                    payloadString = payloadString.replace("\\/", "/");
                    payloadString = payloadString.replaceAll("<map/>", "");
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = null;
                    try {
                        builder = factory.newDocumentBuilder();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }

                    try {
                        Document doc = builder.parse(new InputSource(new StringReader(payloadString)));
                        NodeList apps = doc.getElementsByTagName("root").item(0).getChildNodes().item(1)
                                .getChildNodes();
                        for (int i = 3; i < apps.getLength(); i += 2) {
                            if (((JSONObject) payload).get("appId")
                                    .equals(apps.item(i).getAttributes().item(0).getNodeValue())) {
                                String x = apps.item(i).getChildNodes().item(1).getChildNodes().item(1).getAttributes()
                                        .item(1).getNodeValue();
                                String y = apps.item(i).getChildNodes().item(1).getChildNodes().item(3).getAttributes()
                                        .item(1).getNodeValue();
                                String width = apps.item(i).getChildNodes().item(1).getChildNodes().item(5)
                                        .getAttributes().item(1).getNodeValue();
                                String height = apps.item(i).getChildNodes().item(1).getChildNodes().item(7)
                                        .getAttributes().item(1).getNodeValue();
                                Main.createFrame(apps.item(i).getAttributes().item(0).getNodeValue(),
                                        Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(width),
                                        Integer.parseInt(height), Integer.parseInt(width),
                                        Integer.parseInt(height),
                                        Integer.parseInt(width),
                                        Integer.parseInt(height));
                            }
                        }
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (BackingStoreException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        });
    }

    public CompletionStage<ContextGroupInfo[]> clientGetContextGroupInfo() {
        return desktopConnection.getInterop().connect(this.platformId)
                .thenCompose(client -> client.getContextGroups());
    }

    public void clientSetContext(String group, String ticker, String platformName) throws Exception {
        Context context = new Context();
        JSONObject contextId = new JSONObject();
        contextId.put("ticker", ticker);
        context.setId(contextId);
        var name = "Unknown";
        if(ticker.equals("AAPL")) {
            name = "Apple Inc.";
        } else if(ticker.equals("MSFT")) {
            name = "Microsoft Corporation";
        } else if(ticker.equals("GOOGL")) {
            name = "Alphabet Inc.";
        } else if(ticker.equals("TSLA")) {
            name = "Tesla Inc.";
        }
        context.setName(name);
        context.setType("fdc3.instrument");
        CompletionStage<Void> setContextFuture = desktopConnection.getInterop().connect(platformName)
                .thenCompose(client -> {
                    return client.joinContextGroup(group).thenCompose(v -> {
                        return client.setContext(context);
                    });
                });

        setContextFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    public void joinAllGroups(String color, Main JT) {
        CompletableFuture<Context> listenerInvokedFuture = new CompletableFuture<>();
        desktopConnection.getInterop().connect(platformId).thenCompose(client -> {
            return client.getContextGroups().thenCompose(groups -> {
                return client.joinContextGroup(color).thenCompose(v -> {
                    return client.addContextListener(ctx -> {
                        System.out.print(color + ctx.getId());
                        JT.updateTicker(ctx.getId());
                        listenerInvokedFuture.complete(ctx);
                    });
                });
            });
        });
    }

    public void clientFireIntent(String intent, String type, String typeValue, String platformName) throws Exception {
        Context context = new Context();
        JSONObject contextId = new JSONObject();
        contextId.put("ticker", typeValue);
        context.setId(contextId);
        var name = "Unknown";
        if(typeValue.equals("AAPL")) {
            name = "Apple Inc.";
        } else if(typeValue.equals("MSFT")) {
            name = "Microsoft Corporation";
        } else if(typeValue.equals("GOOGL")) {
            name = "Alphabet Inc.";
        } else if(typeValue.equals("TSLA")) {
            name = "Tesla Inc.";
        }
        context.setName(name);
        context.setType("fdc3.instrument");

        Intent intentToRaise = new Intent();
        intentToRaise.setContext(context);
        intentToRaise.setName(intent);
        CompletionStage<Void> fireIntentFuture = desktopConnection.getInterop().connect(platformName)
            .thenCompose(client -> {
                return client.fireIntent(intentToRaise);
            });

        fireIntentFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    public void addIntentListener(String platformName, Main JT) throws Exception {
        desktopConnection.getInterop().connect(platformName).thenCompose(client -> {
            return client.registerIntentListener("ViewInstrument", intent -> {
                Context context = intent.getContext();
                System.out.println("Received intent: " + intent.getName());
                System.out.println("Context: " + context.getId());
                JT.updateTicker(context.getId());
                JT.updateReceivedIntent(intent.getName());
            });
        }).toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
}
