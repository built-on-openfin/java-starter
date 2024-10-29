package com.openfin.starter.java;

public class CommandLineOptions {
    private static String[] commandLineArgs;

    public static void setCommandLineArgs(String[] args) {
        commandLineArgs = args;
    }

    private static String[] getCommandLineArgs() {
        return commandLineArgs;
    }

    public static String getSpecifiedWorkspaceUUID() {
        String workspaceUUID = null;
        String[] arguments = getCommandLineArgs();
        for (String arg : arguments) {
            if (arg.startsWith("workspaceUUID=")) {
                String[] workspaceUUIDArg = arg.split("=");
                workspaceUUID = workspaceUUIDArg[1];
                if (workspaceUUID.trim().isEmpty() || workspaceUUID.startsWith("{OF-")) {
                    // empty string or token has been passed and is not a suitable UUID
                    return null;
                }
                break;
            }
        }
        return workspaceUUID;
    }

    public static String getSpecifiedNativeUUID() {
        String nativeUUID = null;
        String[] arguments = getCommandLineArgs();
        for (String arg : arguments) {
            if (arg.startsWith("nativeUUID=")) {
                String[] nativeUUIDArg = arg.split("=");
                nativeUUID = nativeUUIDArg[1];
                if (nativeUUID.trim().isEmpty() || nativeUUID.startsWith("{OF-")) {
                    // empty string or token has been passed and is not a suitable UUID
                    return null;
                }
                break;
            }
        }
        return nativeUUID;
    }

    public static boolean getRegisterIntents() {
        String[] arguments = getCommandLineArgs();
        for (String arg : arguments) {
            if (arg.equals("registerIntents")) {
                return true;
            }
        }
        return false;
    }
}
