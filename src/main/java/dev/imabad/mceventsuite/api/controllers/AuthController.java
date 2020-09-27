package dev.imabad.mceventsuite.api.controllers;

import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.api.EventAPI;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import dev.imabad.mceventsuite.core.modules.redis.RedisModule;
import dev.imabad.mceventsuite.core.util.GsonUtils;
import spark.Request;
import spark.Response;

import java.util.UUID;

@Controller(prefix = "auth")
public class AuthController {

    @Route(method = EndpointMethod.POST, endpoint = "token")
    public Object authorize(Request request, Response response){
        JsonObject bodyData = GsonUtils.getGson().fromJson(request.body(), JsonObject.class);
        if(!bodyData.has("accessCode")){
            response.status(401);
            return false;
        }
        String accessCode = bodyData.get("accessCode").getAsString();
        String uuid = EventCore.getInstance().getModuleRegistry().getModule(RedisModule.class).getData("access:" + accessCode);
        if(uuid == null){
            response.status(401);
            return false;
        }
        EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayer(UUID.fromString(uuid));
        if(eventPlayer == null){
            response.status(401);
            return false;
        }
        if(!eventPlayer.hasPermission("eventsuite.staff")){
            response.status(401);
            return false;
        }
        EventCore.getInstance().getModuleRegistry().getModule(RedisModule.class).removeData("access:" + accessCode);
        return EventAPI.getInstance().generateToken(eventPlayer.getUUID());
    }

}
