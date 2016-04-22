package com.yrrlsv.fin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;

public class Event {
    private EventType type;

    private String payer;
    private String recipient;
    private Currency currency;
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal balance;

    private Map<Field, String> data;

    public static Event failed(String message) {
        return new Event(EventType.failed, Collections.singletonMap(Field.source, message));
    }

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
