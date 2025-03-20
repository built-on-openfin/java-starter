package com.openfin.starter.java;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Apps {

    public static class App {
        private String appId;
        private String title;
        private String description;
        private String manifestType;
        private int height;
        private int width;
        private int maxHeight;
        private int maxWidth;
        private int minHeight;
        private int minWidth;

        public App(String appId, String title, String description, String manifestType, int height, int width, int maxHeight, int maxWidth, int minHeight, int minWidth) {
            this.appId = appId;
            this.title = title;
            this.description = description;
            this.manifestType = manifestType;
            this.height = height;
            this.width = width;
            this.maxHeight = maxHeight;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.maxWidth = maxWidth;
        }

        public String getAppId() {
            return appId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getManifestType() {
            return manifestType;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public int getMinHeight() {
            return minHeight;
        }

        public int getMinWidth() {
            return minWidth;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private List<App> apps;

    public Apps() {
        apps = new ArrayList<>();
        apps.add(new App("app1", "Java - Child App One", "An example of a Java Application providing a list of child windows which in turn would be launched by the Java app and not the platform.", "connection", 800, 400, 800, 400, 400, 400));
        apps.add(new App("app2", "Java - Child App Two", "An example of a Java Application providing a list of child windows which in turn would be launched by the Java app and not the platform.", "connection", 600, 400, 600, 400, 400, 400));
        apps.add(new App("app3", "Java - Child App Three", "An example of a Java Application providing a list of child windows which in turn would be launched by the Java app and not the platform.", "connection", 300, 400, 300, 400, 300, 400));    }

    public JSONArray getAppDirectory() {
        JSONArray jsonArray = new JSONArray();
        for (App app : apps) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appId", app.getAppId());
            jsonObject.put("title", app.getTitle());
            jsonObject.put("description", app.getDescription());
            jsonObject.put("manifestType", app.getManifestType());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public App getApp(String appId) {
        for (App app : apps) {
            if (app.getAppId().equals(appId)) {
                return app;
            }
        }
        return null;
    }

    public List<App> getAppList() {
        return new ArrayList<>(apps);
    }
}
