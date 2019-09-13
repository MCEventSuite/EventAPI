package dev.imabad.mceventsuite.api;

import com.google.gson.Gson;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.EventRoute;
import dev.imabad.mceventsuite.core.api.IRegistry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import spark.Route;
import spark.Spark;

public class EndpointRegistry implements IRegistry {

    private List<EventRoute> eventRoutes;
    private boolean loadedDefault = false;
    private Gson gson;
    private String rootRoute;

    public EndpointRegistry(String rootRoute) {
        this.eventRoutes = new ArrayList<>();
        gson = new Gson();
        this.rootRoute = rootRoute;
    }

    public void registerRoute(EndpointMethod endpointMethod, String endpoint, Route route){
        EventRoute eventRoute = new EventRoute(endpointMethod, endpoint, route);
        this.eventRoutes.add(eventRoute);
        if(loadedDefault){
            enableRoute(eventRoute);
        }
    }

    private void enableRoute(EventRoute eventRoute){
        String endpoint = rootRoute + "/" + eventRoute.getEndpoint();
        switch(eventRoute.getEndpointMethod()){
            case GET:
                Spark.get(endpoint, eventRoute.getRoute());
                break;
            case PUT:
                Spark.put(endpoint, eventRoute.getRoute());
                break;
            case POST:
                Spark.post(endpoint, eventRoute.getRoute());
                break;
            case PATCH:
                Spark.patch(endpoint, eventRoute.getRoute());
                break;
            case DELETE:
                Spark.delete(endpoint, eventRoute.getRoute());
                break;
        }
        if(EventAPI.getInstance().isDebug()){
            System.out.println(String.format("Registering endpoint %s %s", eventRoute.getEndpointMethod().name(), endpoint));
        }
    }

    public void registerController(Object object){
        String prefix = "";
        if(object.getClass().isAnnotationPresent(Controller.class)){
            Controller controller = object.getClass().getAnnotation(Controller.class);
            prefix = controller.prefix();
        }
        Method[] methods = object.getClass().getDeclaredMethods();
        for(Method method : methods){
            if(method.isAnnotationPresent(dev.imabad.mceventsuite.api.api.Route.class)){
                dev.imabad.mceventsuite.api.api.Route route = method.getAnnotation(dev.imabad.mceventsuite.api.api.Route.class);
                registerRoute(route.method(), (prefix.length() > 0 ? prefix + "/" : "") + route.endpoint(), (req, res) -> {
                    Object response = method.invoke(object, req, res);
                    if(route.json()){
                        res.type("application/json");
                        return gson.toJson(response);
                    }
                    return response;
                });
            }
        }
    }

    public void enableRoutes(){
        eventRoutes.forEach(this::enableRoute);
        loadedDefault = true;
    }

    @Override
    public String getName() {
        return "endpoint";
    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
