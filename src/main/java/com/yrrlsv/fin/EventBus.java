package com.yrrlsv.fin;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private ArrayList<Event> events = new ArrayList<>();

    public void fire(Event event) {
        events.add(event);
        switch (event.type()) {
            case charge:
                break;
            case replenishment:
                break;
        }
    }

    public List<Event> events() {
        return events;
    }
}
