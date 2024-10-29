# How to Interop with a Workspace Platform

This example connects three native applications to a workspace platform. The implementation of the workspace platform is on our workspace starter repo: <https://github.com/built-on-openfin/workspace-starter/tree/main/how-to/workspace-platform-starter>.

It shows how a native application and a workspace platform could work together so that the native applications could provide a list of apps to a platform and provide information so that the native application can be part of a saved workspace.

## Launch Steps

- Launch workspace-platform-starter from the workspace-starter repo. This will launch the workspace platform and the native applications.

- Start Java-Starter from this repo. This will launch the Java application.

- Enter 'workspace-platform-starter' into the Platform id prompt. This will connect the Java application to the workspace platform (The platform id can be found in the manifest of the starter project) or pass the workspace platform id using the command line.

![img.png](img.png)

The launched application will allow you to launch native windows and listen/transmit context(ticker symbols) to the workspace platform.

![img_1.png](img_1.png)

## Native App Integration from Home

- workspace-platform-starter queries java project for native apps. The java project responds with a list of launchable native apps.

![img_2.png](img_2.png)

### Workspace Platform Starter Query for Native Apps (connections.ts in workspace-platform-starter repo)

```java
    export async function getConnectedApps(): Promise<PlatformApp[]> {
```

### Workspace Platform Starter Launch Native Apps (connections.ts in workspace-platform-starter repo)

```java
    export async function launchConnectedApp(app: PlatformApp) {
```

### Java Project Responds with Native Apps (Interop.java)

```java
    client.register("getApps", (action, payload, senderIdentity) -> {
```

### Java Project Launches Native App (Interop.java)

```java
    client.register("launchApp", (action, payload, senderIdentity) -> {
```
