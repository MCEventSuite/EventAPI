package dev.imabad.mceventsuite.api.controllers;

import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.api.EventAPI;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.api.objects.BasicResponse;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.api.objects.EventRank;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import dev.imabad.mceventsuite.core.modules.mysql.dao.RankDAO;
import dev.imabad.mceventsuite.core.modules.redis.RedisChannel;
import dev.imabad.mceventsuite.core.modules.redis.RedisModule;
import dev.imabad.mceventsuite.core.modules.redis.messages.players.UpdatedPlayerMessage;
import dev.imabad.mceventsuite.core.util.GsonUtils;
import dev.imabad.mceventsuite.core.util.UUIDUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.UUID;

@Controller
public class TicketController {

    public JsonObject body(Request request){
        return GsonUtils.getGson().fromJson(request.body(), JsonObject.class);
    }

    @Route(endpoint = "ticket", method = EndpointMethod.POST, auth = false)
    public BasicResponse addTicket(Request request, Response response){
        String secret = request.headers("Cubed-Secret");
        if(secret == null || secret.isEmpty() || secret.equals(EventAPI.getInstance().getConfig().getTicketSecret())) {
            return BasicResponse.error("Not allowed!");
        }
        String uuid = body(request).get("uuid").getAsString();
        String username = body(request).get("username").getAsString();
        Optional<EventRank> rankOptional = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(RankDAO.class).getRankByName("Attendee");
        if(rankOptional.isPresent()){
            EventRank rank = rankOptional.get();
            EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getOrCreatePlayer(UUID.fromString(UUIDUtils.insertDashUUID(uuid)), username);
            if(eventPlayer.getRank().getPower() < rank.getPower()) {
                eventPlayer.setRank(rank);
                EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).saveOrUpdatePlayer(eventPlayer);
                EventCore.getInstance().getModuleRegistry().getModule(RedisModule.class).publishMessage(RedisChannel.GLOBAL, new UpdatedPlayerMessage(eventPlayer.getUUID()));
            }
            return BasicResponse.SUCCESS;
        } else {
            return BasicResponse.UNSUCCESSFUL;
        }
    }

}
