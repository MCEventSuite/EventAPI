package dev.imabad.mceventsuite.api;

import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.controllers.HealthController;
import java.io.File;

import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.IConfigProvider;
import spark.Spark;

public class EventAPI implements IConfigProvider<APIConfig> {

    private static EventAPI instance;

    public static EventAPI getInstance(){
        return instance;
    }

    public static void main(String[] args){
        new EventAPI();
    }

    private EndpointRegistry endpointRegistry;

    private APIConfig apiConfig;

    private EventAPI(){
        instance = this;
        new EventCore(new File(System.getProperty("user.dir")));
        endpointRegistry = new EndpointRegistry(apiConfig.getRootRoute());
        if(apiConfig.isDebug())
            Spark.exception(Exception.class, ((exception, request, response) -> {exception.printStackTrace(); response.body(exception.getStackTrace().toString());}));
        Spark.port(apiConfig.getPort());
        endpointRegistry.registerRoute(EndpointMethod.GET, "", (req, res) -> "Test test 123");
        endpointRegistry.registerController(new HealthController());
        endpointRegistry.enableRoutes();
        System.out.println("Starting web server on port: " + apiConfig.getPort());
    }

    @Override
    public Class<APIConfig> getConfigType() {
        return APIConfig.class;
    }

    @Override
    public APIConfig getConfig() {
        return apiConfig;
    }

    @Override
    public String getFileName() {
        return "api.json";
    }

    @Override
    public void loadConfig(APIConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public void saveConfig() {
        EventCore.getInstance().getConfigLoader().saveConfig(this);
    }

    @Override
    public boolean saveOnQuit() {
        return false;
    }

    public EndpointRegistry getEndpointRegistry() {
        return endpointRegistry;
    }

    public boolean isDebug(){
        return apiConfig.isDebug();
    }
}
