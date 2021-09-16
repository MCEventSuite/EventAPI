package dev.imabad.mceventsuite.api.objects;

import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.eventpass.db.EventPassReward;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLModule;
import dev.imabad.mceventsuite.core.modules.scavenger.db.ScavengerDAO;
import dev.imabad.mceventsuite.core.modules.scavenger.db.ScavengerLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventPassPlayerResponse {

    private int xp;
    private int level;
    private EventPlayer player;
    private List<EventPlayerYearSlim> years;

    private Set<ScavengerLocation> foundLocations;

    public EventPassPlayerResponse(int xp, int level, EventPlayer player, Set<ScavengerLocation> foundLocations) {
        this.xp = xp;
        this.level = level;
        this.player = player;
        this.years = player.getAttendance().stream().map(eventPlayerYear -> new EventPlayerYearSlim(eventPlayerYear.getYear(), eventPlayerYear.getRank())).collect(Collectors.toList());
        this.foundLocations = foundLocations;
    }
}
