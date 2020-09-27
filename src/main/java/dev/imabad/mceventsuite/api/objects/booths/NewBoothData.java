package dev.imabad.mceventsuite.api.objects.booths;

import java.util.HashMap;
import java.util.List;

public class NewBoothData {

    private String owner;
    private String name;

    public NewBoothData(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
}
