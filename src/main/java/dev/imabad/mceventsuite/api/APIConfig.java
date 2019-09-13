package dev.imabad.mceventsuite.api;

import dev.imabad.mceventsuite.core.api.BaseConfig;

public class APIConfig extends BaseConfig {

    private int port = 8080;
    private boolean debug = false;
    private String rootRoute = "";

    @Override
    public String getName() {
        return "api";
    }

    public int getPort() {
        return port;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getRootRoute() {
        return rootRoute;
    }
}
