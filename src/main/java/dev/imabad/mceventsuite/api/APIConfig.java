package dev.imabad.mceventsuite.api;

import dev.imabad.mceventsuite.core.api.BaseConfig;

public class APIConfig extends BaseConfig {

    private int port = 8080;
    private boolean debug = false;
    private String rootRoute = "";
    private String secret = "";
    private String tokenSecret = "";
    private String ticketSecret = "";
    private int tokenDuration = 3600;

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

    public String getSecret() {
        return secret;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public int getTokenDuration() {
        return tokenDuration;
    }

    public String getTicketSecret() {
        return ticketSecret;
    }
}
