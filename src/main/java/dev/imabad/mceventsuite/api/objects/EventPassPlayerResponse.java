package dev.imabad.mceventsuite.api.objects;

import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.eventpass.db.EventPassReward;

import java.util.List;

public class EventPassPlayerResponse {

    private int xp;
    private int level;
    private EventPlayer player;

    public EventPassPlayerResponse(int xp, int level, EventPlayer player) {
        this.xp = xp;
        this.level = level;
        this.player = player;
    }
}
