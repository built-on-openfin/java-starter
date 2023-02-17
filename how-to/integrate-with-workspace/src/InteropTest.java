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
import com.openfin.desktop.channel.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class InteropTest implements SnapshotSourceProvider {
	private static Logger logger = LoggerFactory.getLogger(InteropTest.class.getName());
	public static DesktopConnection desktopConnection;
	private String platformId;
	private JavaTest javaTest;

	public void setup(String platformId) throws Exception {
		logger.debug("starting");
		desktopConnection = TestUtils.setupConnection("interop-test-desktop");
		this.platformId = platformId;
		FrameMonitor.init();
		createChannelClient();
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
					appObject.put("x", apps.item(i).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(1).getNodeValue());
					appObject.put("y", apps.item(i).getChildNodes().item(1).getChildNodes().item(3).getAttributes().item(1).getNodeValue());
					appObject.put("w", apps.item(i).getChildNodes().item(1).getChildNodes().item(5).getAttributes().item(1).getNodeValue());
					appObject.put("h", apps.item(i).getChildNodes().item(1).getChildNodes().item(7).getAttributes().item(1).getNodeValue());
					appObject.put("open", apps.item(i).getChildNodes().item(1).getChildNodes().item(9).getAttributes().item(1).getNodeValue());
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
			javaTest.CloseAllWindows();
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
				NodeList apps =	doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
				JSONObject jo;
				for (int i = 3; i < apps.getLength(); i += 2) {
					for (int j = 0; j < ja.length(); j++) {
						jo = (JSONObject) ja.get(j);
						if (jo.getString("appId").equals(apps.item(i).getAttributes().item(0).getNodeValue())) {
							if(Integer.parseInt(jo.getString("open")) == 1)
								javaTest.createFrame(apps.item(i).getAttributes().item(0).getNodeValue(), Integer.parseInt(jo.getString("x")), Integer.parseInt(jo.getString("y")), Integer.parseInt(jo.getString("w")), Integer.parseInt(jo.getString("h")));
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

	public void createChannelClient() throws JSONException {

		desktopConnection.getChannel("customize-workspace-workspace-connection").connectAsync().thenAccept(client -> {
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
						NodeList apps =	doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
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
						NodeList apps =	doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
						for (int i = 3; i < apps.getLength(); i += 2) {
							if (((JSONObject) payload).get("appId").equals(apps.item(i).getAttributes().item(0).getNodeValue())) {
								String x = apps.item(i).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(1).getNodeValue();
								String y = apps.item(i).getChildNodes().item(1).getChildNodes().item(3).getAttributes().item(1).getNodeValue();
								String width = apps.item(i).getChildNodes().item(1).getChildNodes().item(5).getAttributes().item(1).getNodeValue();
								String height = apps.item(i).getChildNodes().item(1).getChildNodes().item(7).getAttributes().item(1).getNodeValue();
								javaTest.createFrame(apps.item(i).getAttributes().item(0).getNodeValue(), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(width), Integer.parseInt(height));
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
	public void clientGetContextGroupInfo() throws Exception {
		CompletionStage<ContextGroupInfo[]> getContextFuture = desktopConnection.getInterop().connect(this.platformId).thenCompose(client->{
			return client.getContextGroups();
		});

		ContextGroupInfo[] contextGroupInfo = getContextFuture.toCompletableFuture().get(100, TimeUnit.SECONDS);
		for(ContextGroupInfo c : contextGroupInfo) {
			//clientAddContextListener();
		}
	}

	public void clientGetInfoForContextGroup() throws Exception {
		CompletionStage<ContextGroupInfo> getContextFuture = desktopConnection.getInterop().connect(this.platformId).thenCompose(client->{
			return client.getInfoForContextGroup("red");
		});

		ContextGroupInfo contextGroupInfo = getContextFuture.toCompletableFuture().get(100, TimeUnit.SECONDS);
		logger.debug("Context Group Info" + contextGroupInfo.toString());
	}

	public void clientGetAllClientsInContextGroup() throws Exception {
		CompletionStage<ClientIdentity[]> getContextFuture = desktopConnection.getInterop().connect(this.platformId).thenCompose(client->{
			return client.joinContextGroup("red").thenCompose(v->{
				return client.getAllClientsInContextGroup("red");
			});
		});

		ClientIdentity[] clientIdentity = getContextFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
	}

	public void clientJoinThenRemoveFromContextGroup() throws Exception {
		AtomicInteger clientCntAfterJoin = new AtomicInteger(0);
		AtomicInteger clientCntAfterRemove = new AtomicInteger(0);
		CompletionStage<?> testFuture = desktopConnection.getInterop().connect(this.platformId).thenCompose(client->{
			return client.joinContextGroup("red").thenCompose(v->{
				return client.getAllClientsInContextGroup("red");
			}).thenAccept(clients->{
				clientCntAfterJoin.set(clients.length);
			}).thenCompose(v->{
				return client.removeFromContextGroup();
			}).thenCompose(v->{
				return client.getAllClientsInContextGroup("red");
			}).thenAccept(clients->{
				clientCntAfterRemove.set(clients.length);
			});
		});

		testFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
	}

	public void clientSetContext(String group, String ticker, String platformName) throws Exception {
		Context context = new Context();
		JSONObject contextId = new JSONObject();
		contextId.put("ticker", ticker);
		context.setId(contextId);
		context.setName("MyName");
		context.setType("instrument");
		CompletionStage<Void> setContextFuture = desktopConnection.getInterop().connect(platformName).thenCompose(client->{
			return client.getContextGroups().thenCompose(groups->{
				return client.joinContextGroup(group).thenCompose(v->{
					return client.setContext(context);
				});
			});
		});

		setContextFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
	}

	public void clientAddContextListener() throws Exception {
		Context context = new Context();
		JSONObject contextId = new JSONObject();
		contextId.put("ticker", "WMT");
		context.setId(contextId);
		context.setName("MyName");
		context.setType("instrument");

		CompletableFuture<Context> listenerInvokedFuture = new CompletableFuture<>();

		desktopConnection.getInterop().connect(this.platformId).thenCompose(client->{
			return client.addContextListener(ctx->{
				listenerInvokedFuture.complete(ctx);
			}).thenApply(v->{
				return client;
			});
		}).thenCompose(client->{
			return client.joinContextGroup("red").thenCompose(v->{
				return client.setContext(context);
			});
		});

		Context ctx = listenerInvokedFuture.toCompletableFuture().get(10, TimeUnit.SECONDS);
	}

	public void joinAllGroups(String color, JavaTest JT) {
		CompletableFuture<Context> listenerInvokedFuture = new CompletableFuture<>();
		javaTest = JT;
		desktopConnection.getInterop().connect(platformId).thenCompose(client->{
			return client.getContextGroups().thenCompose(groups->{
				return client.joinContextGroup(color).thenCompose(v->{
					return client.addContextListener(ctx->{
						System.out.print(color + ctx.getId());
						JT.updateTicker(ctx.getId());
						listenerInvokedFuture.complete(ctx);
					});
				});
			});
		});
	}

}


