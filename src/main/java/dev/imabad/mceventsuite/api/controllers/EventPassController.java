package dev.imabad.mceventsuite.api.controllers;

import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.api.MyMCUUID;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.api.objects.BasicResponse;
import dev.imabad.mceventsuite.api.objects.EventPassPlayerResponse;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.eventpass.db.*;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import dev.imabad.mceventsuite.core.modules.scavenger.db.ScavengerDAO;
import dev.imabad.mceventsuite.core.modules.scavenger.db.ScavengerHuntPlayer;
import dev.imabad.mceventsuite.core.modules.scavenger.db.ScavengerLocation;
import dev.imabad.mceventsuite.core.util.GsonUtils;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller(prefix = "pass")
public class EventPassController {

    @Route(endpoint = "player/:name", method = EndpointMethod.GET)
    public Object player(Request request, Response response){
        String username = request.params("name");
        EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayer(username);
        if(eventPlayer == null){
            UUID uuid = MyMCUUID.getUUID(username);
            if(uuid == null) {
                response.status(404);
                return BasicResponse.error("player does not exist");
            } else {
                eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getOrCreatePlayer(uuid, username);
            }
        }
        EventPassPlayer eventPassPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).getOrCreateEventPass(eventPlayer);
        Set<ScavengerLocation> locations = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(ScavengerDAO.class).getPlayerFoundLocations(eventPlayer);
//        System.out.println("Locations: " + scavengerHuntPlayer.getFoundLocations().size());
        return new EventPassPlayerResponse(eventPassPlayer.getXp(), eventPassPlayer.levelFromXP(), eventPlayer, locations);
    }

    @Route(endpoint = "rewards/:year", method = EndpointMethod.GET)
    public Object rewards(Request request, Response response){
        String year = request.params("year");
        return EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).getRewards(Integer.parseInt(year));
    }

    public JsonObject body(Request request){
        return GsonUtils.getGson().fromJson(request.body(), JsonObject.class);
    }

    @Route(endpoint = "redeem", method = EndpointMethod.POST)
    public Object redeem(Request request, Response response) {
        JsonObject object = body(request);
        String code = object.get("code").getAsString();
        String uuid = object.get("uuid").getAsString();
        EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayer(UUID.fromString(uuid));
        if(eventPlayer == null) {
            return BasicResponse.UNSUCCESSFUL;
        } else {
            EventPassCode eventPassCode = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).getCode(code);
            if(eventPassCode == null) {
                return BasicResponse.UNSUCCESSFUL;
            }
            EventPassUnlockedReward unlockedReward = new EventPassUnlockedReward(eventPassCode.getReward(), eventPlayer);
            EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).saveUnlockedReward(unlockedReward);
            return BasicResponse.SUCCESS;
        }
    }
}
