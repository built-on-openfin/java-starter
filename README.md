# Java-Starter
A starter repo providing examples of how to use the OpenFin Java Adapter.
This repo contains a simple Java application that uses the OpenFin Java Adapter to launch an OpenFin application.

It uses maven as the build tool and the OpenFin Java Adapter is available on Maven Central. 
Please refer to the pom.xml file for the dependencies.

## Getting Started
1. Clone this repo
2. Install the OpenFin Java Adapter and its dependencies using `mvn install`
3. Start Java-Starter, JavaTest contains the main method

## Starters

| Documentation                                                           | Description                                                                                                                                                                  |
|-------------------------------------------------------------------------| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [How To Save and Restore Workspaces](/SaveWorkspace)                  | This example shows you how you can ask a platform for a snapshot to save as part of your native application state (where the platform is a child view of your native app) |
| [How To Listen/Transmit context](./how-to/integrate-with-workspace)     | This gives an example of how a Native Apps can integrate with a workspace platform (e.g. provide snapshot data from a native app to a platform, provide search results or call actions against a platform).|
| [How To Register apps with workspace](./how-to/integrate-with-workspace) | This gives an example of how a Native Apps can integrate with a workspace platform (e.g. provide snapshot data from a native app to a platform, provide search results or call actions against a platform).|
| [How To Use Notifications](./how-to/use-notifications)                  | This gives an example of how Native Apps can create and use Notifications with a workspace platform |

[Context Sharing](/.md)

## Architecture

Window location is tracked using FrameMonitor.java. This class is responsible for listening to the window's frame events and updating the window's location to a local [Preferences](https://docs.oracle.com/javase/7/docs/api/java/util/prefs/Preferences.html).


## Documentation
- https://developers.openfin.co/of-docs/docs/java-api
- https://search.maven.org/artifact/co.openfin/openfin-desktop-java-adapter

