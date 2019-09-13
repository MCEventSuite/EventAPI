package dev.imabad.mceventsuite.api.api;

import spark.Route;

public class EventRoute {

    private EndpointMethod endpointMethod;
    private String endpoint;
    private Route route;

    public EventRoute(EndpointMethod endpointMethod, String endpoint, Route route){
        this.endpointMethod = endpointMethod;
        this.endpoint = endpoint;
        this.route = route;
    }

    public EndpointMethod getEndpointMethod() {
        return endpointMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Route getRoute() {
        return route;
    }
}

