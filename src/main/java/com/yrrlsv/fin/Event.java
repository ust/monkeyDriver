package com.yrrlsv.fin;

import java.util.Map;

public class Event {
    private EventType type;
    private Map<Field, String> data;

    public Event(EventType type, Map<Field, String> data) {
        this.type = type;
        this.data = data;
    }

    public EventType type() {
        return type;
    }

    public Map<Field, String> data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return !(data != null ? !data.equals(event.data) : event.data != null);

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
