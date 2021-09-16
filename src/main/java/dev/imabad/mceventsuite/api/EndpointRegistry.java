package dev.imabad.mceventsuite.api;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.EventRoute;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.IRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.imabad.mceventsuite.core.api.objects.EventBooth;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import spark.Route;
import spark.Spark;

public class EndpointRegistry implements IRegistry {

    private List<EventRoute> eventRoutes;
    private boolean loadedDefault = true;
    private Gson gson;
    private String rootRoute;

    public EndpointRegistry(String rootRoute) {
        this.eventRoutes = new ArrayList<>();
        gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                if(f.getDeclaringClass().equals(EventPlayer.class) && f.getName().equals("permissions")){
                    return true;
                }
                if(f.getDeclaringClass().equals(EventPlayer.class) && f.getName().equals("attendance")){
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
        this.rootRoute = rootRoute;
    }

    public void registerRoute(EndpointMethod endpointMethod, String endpoint, Route route) {
        EventRoute eventRoute = new EventRoute(endpointMethod, endpoint, route);
        this.eventRoutes.add(eventRoute);
        if (loadedDefault) {
            enableRoute(eventRoute);
        }
    }

    private void enableRoute(EventRoute eventRoute) {
        String endpoint = rootRoute + "/" + eventRoute.getEndpoint();
        switch (eventRoute.getEndpointMethod()) {
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
        if (EventAPI.getInstance().isDebug()) {
            System.out.println(String.format("Registering endpoint %s %s", eventRoute.getEndpointMethod().name(), endpoint));
        }
    }

    public void registerController(Object object) {
        String prefix = "";
        if (object.getClass().isAnnotationPresent(Controller.class)) {
            Controller controller = object.getClass().getAnnotation(Controller.class);
            prefix = controller.prefix();
        }
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(dev.imabad.mceventsuite.api.api.Route.class)) {
                dev.imabad.mceventsuite.api.api.Route route = method.getAnnotation(dev.imabad.mceventsuite.api.api.Route.class);
                registerRoute(route.method(), (prefix.length() > 0 ? prefix + "/" : "") + route.endpoint(), (req, res) -> {
                    if (route.auth()) {
                        String authHeader = req.headers("Authorization");
                        if (authHeader == null || authHeader.length() < 1) {
                            res.status(401);
                            return false;
                        } else {
                            if (!EventAPI.getInstance().isValidToken(authHeader)) {
                                res.status(401);
                                return false;
                            } else {
                                DecodedJWT jwt = EventAPI.getInstance().getDecodedToken(authHeader);
                                Claim uuidClaim = jwt.getClaim("uuid");
                                if (uuidClaim.isNull()) {
                                    res.status(401);
                                    return false;
                                }
                                String uuid = uuidClaim.asString();
                                EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayer(UUID.fromString(uuid));
                                if (eventPlayer != null) {
                                    if (route.permission().length() > 0) {
                                        if (!eventPlayer.hasPermission(route.permission())) {
                                            res.status(401);
                                            return false;
                                        }
                                    }
                                    req.attribute("player", eventPlayer);
                                }
                            }
                        }
                    }
                    try {
                        Object response = method.invoke(object, req, res);
                        if (route.json()) {
                            res.type("application/json");
                            return gson.toJson(response);
                        }
                        return response;
                    } catch(Exception e) {
                        e.printStackTrace();
                        res.status(500);
                        return null;
                    }
                });
            }
        }
    }

    public void enableRoutes() {
        Spark.options("/*",
        (request, response) -> {

            String accessControlRequestHeaders = request
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers",
                        accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods",
                        accessControlRequestMethod);
            }

            return "OK";
        });
        Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
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
