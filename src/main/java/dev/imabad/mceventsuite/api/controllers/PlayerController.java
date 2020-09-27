package dev.imabad.mceventsuite.api.controllers;

import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import spark.Request;
import spark.Response;

import java.util.List;

@Controller(prefix = "player")
public class PlayerController {

    @Route(endpoint = "list", method = EndpointMethod.GET, auth = true, permission = "eventsuite.players.list")
    public List<EventPlayer> booths(Request request, Response response){
        return EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getPlayers();
    }

}
