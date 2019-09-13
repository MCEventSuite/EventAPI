package dev.imabad.mceventsuite.api.controllers;

import dev.imabad.mceventsuite.api.api.APIStatus;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import spark.Request;
import spark.Response;

@Controller
public class HealthController {

    @Route(endpoint = "health", method = EndpointMethod.GET, json = false)
    public Object getHealth(Request request, Response response){
       return "Hello!";
    }

    @Route(endpoint = "status", method = EndpointMethod.GET)
    public Object getStatus(Request request, Response response){
        return new APIStatus();
    }
}
