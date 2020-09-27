package dev.imabad.mceventsuite.api.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.imabad.mceventsuite.api.EventAPI;
import dev.imabad.mceventsuite.api.api.Controller;
import dev.imabad.mceventsuite.api.api.EndpointMethod;
import dev.imabad.mceventsuite.api.api.Route;
import dev.imabad.mceventsuite.api.api.UnauthorizedResponse;
import dev.imabad.mceventsuite.api.objects.booths.NewBoothData;
import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventBooth;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.api.objects.EventRank;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.mysql.dao.BoothDAO;
import dev.imabad.mceventsuite.core.modules.mysql.dao.PlayerDAO;
import dev.imabad.mceventsuite.core.modules.mysql.dao.RankDAO;
import dev.imabad.mceventsuite.core.modules.redis.RedisChannel;
import dev.imabad.mceventsuite.core.modules.redis.RedisModule;
import dev.imabad.mceventsuite.core.modules.redis.messages.NewBoothMessage;
import dev.imabad.mceventsuite.core.modules.redis.messages.SendDiscordMessage;
import dev.imabad.mceventsuite.core.util.GsonUtils;
import dev.imabad.mceventsuite.core.util.UUIDUtils;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller(prefix = "booth")
public class BoothController {

    private List<String> processingBooths = new ArrayList<>();

    @Route(endpoint = "tebex", method = EndpointMethod.POST)
    public Object tebexWebhook(Request request, Response response){
        String header = request.headers("X-BC-Sig");
        String body = request.body();
        JsonObject webhook = GsonUtils.getGson().fromJson(body, JsonObject.class);
        String originalString = EventAPI.getInstance().getConfig().getSecret() + webhook.getAsJsonObject("payment").get("txn_id").getAsString() + webhook.getAsJsonObject("payment").get("status").getAsString() + webhook.getAsJsonObject("customer").get("email").getAsString();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                originalString.getBytes(StandardCharsets.UTF_8));
        String token = bytesToHex(encodedhash);
        /*if(!header.equals(token)){
            response.status(401);
            return new UnauthorizedResponse();
        }*/
        String userUUID = webhook.getAsJsonObject("customer").get("uuid").getAsString();
        NewBoothData newBoothData = EventAPI.getInstance().getWebDB().getBoothApplication(userUUID);
        if(newBoothData == null){
            response.status(401);
            return new UnauthorizedResponse();
        }
        if(processingBooths.contains(newBoothData.getName())){
            System.out.println("This booth has already been saved!");
            return true;
        }
        processingBooths.add(newBoothData.getName());
        JsonObject customer = webhook.getAsJsonObject("customer");
        JsonObject boughtPackage = webhook.getAsJsonArray("packages").get(0).getAsJsonObject();
        EventPlayer eventPlayer = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getOrCreatePlayer(UUID.fromString(UUIDUtils.insertDashUUID(newBoothData.getOwner())), customer.get("ign").getAsString());
        List<EventBooth> booths = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(BoothDAO.class).getPlayerBooths(eventPlayer);
        AtomicBoolean shouldContinue = new AtomicBoolean(false);
        if(booths.stream().anyMatch(eventBooth -> eventBooth.getName().equalsIgnoreCase(newBoothData.getName()))){
            booths.stream().filter(eventBooth -> eventBooth.getName().equalsIgnoreCase(newBoothData.getName())).findFirst().ifPresent(eventBooth -> {
                String type = boughtPackage.get("name").getAsString();
                type = type.substring(0, type.indexOf(" ")).toLowerCase();
                if(!eventBooth.getBoothType().equalsIgnoreCase(type) && eventBooth.getStatus().equalsIgnoreCase("un-assigned")){
                    eventBooth.setBoothType(type);
                    EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(BoothDAO.class).saveBooth(eventBooth);
                } else {
                    shouldContinue.set(true);
                }
            });
        } else {
            shouldContinue.set(true);
        }
        if(!shouldContinue.get()){
            processingBooths.remove(newBoothData.getName());
            return true;
        }
        EventRank boothOwner = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(RankDAO.class).getRankByName("Booth Owner").orElse(new EventRank(20, "Booth Owner", "", "", Collections.emptyList(), true));

        if(eventPlayer.getRank().getPower() < boothOwner.getPower()){
            eventPlayer.setRank(boothOwner);
            EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).saveOrUpdatePlayer(eventPlayer);
        }
        List<EventPlayer> members = new ArrayList<>();
        JsonArray variables = boughtPackage.getAsJsonArray("variables");
        for(JsonElement element : variables){
            if(element.getAsJsonObject().get("identifier").getAsString().contains("MemberIGN")){
                String username = element.getAsJsonObject().get("option").getAsString();
                if(username.length() > 0 && !username.equalsIgnoreCase("none")){
                    UUID uuid = UUIDUtils.getFromUsername(username);
                    if(uuid != null){
                        EventPlayer member = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).getOrCreatePlayer(uuid, username);
//                        if(member.getRank().getPower() < boothMember.getPower()){
//                            member.setRank(boothMember);
//                            EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(PlayerDAO.class).savePlayer(eventPlayer);
//                        }
                        members.add(member);
                    }
                }
            }
        }
        String type = boughtPackage.get("name").getAsString();
        type = type.substring(0, type.indexOf(" ")).toLowerCase();
        EventBooth eventBooth = new EventBooth(newBoothData.getName(), type, eventPlayer, members);
        EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(BoothDAO.class).saveBooth(eventBooth);
        EventCore.getInstance().getModuleRegistry().getModule(RedisModule.class).publishMessage(RedisChannel.GLOBAL, new NewBoothMessage(eventBooth));
        int boothCount = EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(BoothDAO.class).getBooths().size();
        EventCore.getInstance().getModuleRegistry().getModule(RedisModule.class).publishMessage(RedisChannel.GLOBAL, new SendDiscordMessage("110441858114531328", "New " + type + " booth purchased: " + eventBooth.getName() + " owned by " + eventBooth.getOwner().getLastUsername() + " with " + eventBooth.getMembers().size() + " members. Total booths: " + boothCount));
        processingBooths.remove(newBoothData.getName());
        return true;
    }

    @Route(endpoint = "list", method = EndpointMethod.GET, auth = true, permission = "eventsuite.booths.list")
    public List<EventBooth> booths(Request request, Response response){
        return EventCore.getInstance().getModuleRegistry().getModule(MySQLModule.class).getMySQLDatabase().getDAO(BoothDAO.class).getBooths();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}