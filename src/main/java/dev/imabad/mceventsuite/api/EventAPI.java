package dev.imabad.mceventsuite.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.controllers.*;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.IConfigProvider;
import dev.imabad.mceventsuite.core.api.objects.EventBooth;
import dev.imabad.mceventsuite.core.modules.eventpass.EventPassModule;
import dev.imabad.mceventsuite.core.modules.redis.RedisModule;
import dev.imabad.mceventsuite.core.modules.scavenger.ScavengerModule;
import dev.imabad.mceventsuite.core.util.GsonUtils;
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
    private WebDB webDB;
    private Algorithm algorithmHS;
    private JWTVerifier jwtVerifier;

    private EventAPI(){
        instance = this;
        new EventCore(new File(System.getProperty("user.dir")));
        EventCore.getInstance().getModuleRegistry().addAndEnableModule(new RedisModule());
        EventCore.getInstance().getModuleRegistry().addAndEnableModule(new EventPassModule());
        EventCore.getInstance().getModuleRegistry().addAndEnableModule(new ScavengerModule());
        apiConfig = EventCore.getInstance().getConfigLoader().getOrLoadConfig(this);
        algorithmHS = Algorithm.HMAC256(apiConfig.getTokenSecret());
        jwtVerifier = JWT.require(algorithmHS).build();
        endpointRegistry = new EndpointRegistry(apiConfig.getRootRoute());
        if(apiConfig.isDebug())
            Spark.exception(Exception.class, ((exception, request, response) -> {exception.printStackTrace(); response.body(exception.getStackTrace().toString());}));
        Spark.port(apiConfig.getPort());
        endpointRegistry.registerRoute(EndpointMethod.GET, "", (req, res) -> "Test test 123");
        endpointRegistry.registerController(new HealthController());
        endpointRegistry.registerController(new BoothController());
        endpointRegistry.registerController(new AuthController());
        endpointRegistry.registerController(new PlayerController());
        endpointRegistry.registerController(new TicketController());
        endpointRegistry.registerController(new EventPassController());
        endpointRegistry.enableRoutes();
        System.out.println("Starting web server on port: " + apiConfig.getPort());
        webDB = new WebDB();
        webDB.connect();
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

    public WebDB getWebDB() {
        return webDB;
    }

    public boolean isValidToken(String token){
        try {
            jwtVerifier.verify(token);
            return true;
        }catch(JWTVerificationException e){
            return false;
        }
    }

    public DecodedJWT getDecodedToken(String token){
        try {
            return jwtVerifier.verify(token);
        }catch(JWTVerificationException e){
            return null;
        }
    }

    public String generateToken(UUID uuid){
        long timeStamp = System.currentTimeMillis() + (apiConfig.getTokenDuration() * 1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return JWT.create().withClaim("uuid", uuid.toString()).withExpiresAt(calendar.getTime()).sign(algorithmHS);
    }
}
