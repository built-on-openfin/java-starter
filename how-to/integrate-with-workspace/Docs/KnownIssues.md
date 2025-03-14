# Known Issues

This sample application when using v20.1 of the workspace starter cannot support multi instance mode. That is because workspace platform starter creates an id for the app as appId/instanceId as a way of distinguishing the appId. The Java Adapter as of 2025/03/14 will take this id and try to create a json file for it with throws an exception because of the / in the runtimeUUID. For now it only supports instanceMode: single.
