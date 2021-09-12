package dev.imabad.mceventsuite.api.controllers;

import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.api.objects.BasicResponse;
import dev.imabad.mceventsuite.api.objects.EventPassPlayerResponse;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.eventpass.db.EventPassDAO;
import dev.imabad.mceventsuite.core.modules.eventpass.db.EventPassPlayer;
import dev.imabad.mceventsuite.core.modules.eventpass.db.EventPassReward;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import dev.imabad.mceventsuite.core.util.GsonUtils;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.stream.Collectors;

@Controller(prefix = "pass")
public class EventPassController {

    @Route(endpoint = "player/:name", method = EndpointMethod.GET)
    public Object player(Request request, Response response){
        String username = request.params("name");
        EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayer(username);
        if(eventPlayer == null){
            response.status(404);
            return BasicResponse.error("player does not exist");
        }
        EventPassPlayer eventPassPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).getOrCreateEventPass(eventPlayer);
        EventPassPlayerResponse eventPassPlayerResponse = new EventPassPlayerResponse(eventPassPlayer.getXp(), eventPassPlayer.levelFromXP(), eventPlayer);
        return eventPassPlayerResponse;
    }

    @Route(endpoint = "rewards/:year", method = EndpointMethod.GET)
    public Object rewards(Request request, Response response){
        String year = request.params("year");
        return EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(EventPassDAO.class).getRewards(Integer.parseInt(year));
    }
}
