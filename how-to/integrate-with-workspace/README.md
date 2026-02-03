# Java-Starter
A starter repo providing examples of how to use the HERE Java Adapter.
This repo contains a simple Java application that uses the HERE Java Adapter to launch a HERE application.

It uses maven as the build tool and the HERE Java Adapter is available on Maven Central. 
Please refer to the pom.xml file for the dependencies.

## Getting Started
1. Clone this repo
2. Install the HERE Java Adapter and its dependencies using `mvn install`
3. Start Java-Starter, the java starter contains the main method
4. To package into a jar you can use mvn package (or the tools provided by your IDE to execute the command). 

## IDEs

You can use your own preference to build this project but we are using Visual Studio Code with Java Extensions.

## Command Line Args

This example supports the following command line arguments:

- workspaceUUID (the workspace to connect to) - If not provided then a pop up appears asking for a workspace platform uuid to be specified.
- nativeUUID (the id this app should provide to the runtime when starting up - this will be the identity seen by the platform you connect to). Defaults to "interop-test-desktop".
- registerIntents (defaults to false and changes to true if passed). Not used yet.

## Starters

| Documentation                                                        | Description                                                                                                                                                                  |
|----------------------------------------------------------------------| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [How To Save and Restore Workspaces](Docs/SaveWorkspace.md) | This example shows you how you can ask a platform for a snapshot to save as part of your native application state (where the platform is a child view of your native app) |
| [How To Listen/Transmit context](Docs/ContextSharing.md)           | This gives an example of how a Native Apps can integrate with a workspace platform (e.g. provide snapshot data from a native app to a platform, provide search results or call actions against a platform).|
| [How To Register apps with workspace](Docs/RegisterApps.md)        | This gives an example of how a Native Apps can integrate with a workspace platform (e.g. provide snapshot data from a native app to a platform, provide search results or call actions against a platform).|

## Documentation

- https://resources.here.io/docs/core/adapters/java/
- https://search.maven.org/artifact/co.openfin/openfin-desktop-java-adapter