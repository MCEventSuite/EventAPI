package dev.imabad.mceventsuite.api.objects;

import dev.imabad.mceventsuite.core.api.objects.EventYear;

public class EventPlayerYearSlim {

    private EventYear year;
    private String rank;

    public EventPlayerYearSlim(EventYear year, String rank) {
        this.year = year;
        this.rank = rank;
    }
}
