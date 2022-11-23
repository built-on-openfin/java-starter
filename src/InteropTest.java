import java.io.*;
import java.lang.System;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import com.openfin.desktop.*;
import com.openfin.desktop.ClientIdentity;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openfin.desktop.interop.Context;
import com.openfin.desktop.interop.ContextGroupInfo;
import com.openfin.desktop.channel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class InteropTest {
	private static Logger logger = LoggerFactory.getLogger(InteropTest.class.getName());

	private static final String DESKTOP_UUID = InteropTest.class.getName();
	private static DesktopConnection desktopConnection;
	private String platformId;
	private JavaTest javaTest;
	public void setup(String platformId) throws Exception {
		logger.debug("starting");
		desktopConnection = TestUtils.setupConnection(DESKTOP_UUID);
		this.platformId = platformId;
		createChannelClient();
	}

	public void createChannelClient() throws JSONException {

		desktopConnection.getChannel("platform-command").connectAsync().thenAccept(client -> {
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
			client.register("getApps", new ChannelAction() {
				@Override
				public Object invoke(String action, Object payload, JSONObject senderIdentity) {
					JSONObject payloadWindows = new JSONObject();

					try {
						OutputStream os = new ByteArrayOutputStream();
						FrameMonitor.pref.exportSubtree(os);
						payloadWindows.put("windows", os.toString());
						os.flush();
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (BackingStoreException e) {
						throw new RuntimeException(e);
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}
					client.dispatch("getApps", payloadWindows, new AckListener() {
						@Override
						public void onSuccess(Ack ack) {
							logger.info("success");
						}

						@Override
						public void onError(Ack ack) {
						}
					});
					return null;
				}
			});

			client.register("applySnapshot", new ChannelAction() {
				@Override
				public Object invoke(String action, Object payload, JSONObject senderIdentity) {
					//FrameMonitor.prefs.clear();
					String payloadString = payload.toString();
					payloadString = payloadString.substring(12, payloadString.length() - 2);
					payloadString = payloadString.replace("\\r\\n", "");
					payloadString = payloadString.replace("\\\"", "\"");
					payloadString = payloadString.replace("\\/", "/");
					// remove all occurance of <map/> tag
					payloadString = payloadString.replaceAll("<map/>", "");
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = null;
					try {
						builder = factory.newDocumentBuilder();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					}

					try {
					// convert payloadstring to xml document
					Document doc = builder.parse(new InputSource(new StringReader(payloadString)));
					// get all map tags
					NodeList apps =	doc.getElementsByTagName("root").item(0).getChildNodes().item(1).getChildNodes();
					for (int i = 3; i < apps.getLength(); i += 2) {
						String x = apps.item(i).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(1).getNodeValue();
						String y = apps.item(i).getChildNodes().item(1).getChildNodes().item(3).getAttributes().item(1).getNodeValue();
						String width = apps.item(i).getChildNodes().item(1).getChildNodes().item(5).getAttributes().item(1).getNodeValue();
						String height = apps.item(i).getChildNodes().item(1).getChildNodes().item(7).getAttributes().item(1).getNodeValue();
						javaTest.createFrame(apps.item(i).getAttributes().item(0).getNodeValue(), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(width), Integer.parseInt(height));
					}
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
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

	public void joinAllGroups(String color, JavaTest JT) throws Exception {
		CompletableFuture<Context> listenerInvokedFuture = new CompletableFuture<>();
		JSONObject retval = new JSONObject();
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


