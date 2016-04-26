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

    public Event(EventType type,
                 String payer,
                 String recipient,
                 Currency currency,
                 LocalDateTime date,
                 BigDecimal amount,
                 BigDecimal balance) {
        this.type = type;
        this.payer = payer;
        this.recipient = recipient;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.balance = balance;
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

    public static class Builder {
        private EventType type;
        private String payer;
        private String recipient;
        private Currency currency;
        private LocalDateTime date;
        private BigDecimal amount;
        private BigDecimal balance;

        public Event build() {
            return new Event(type, payer, recipient, currency, date, amount, balance);
        }

        public EventType getType() {
            return type;
        }

        public Builder setType(EventType type) {
            this.type = type;
            return this;
        }

        public Builder merge(Builder other) {
            // type?
            if (payer != null && other.payer != null) payer = other.payer;
            if (recipient != null && other.recipient != null) recipient = other.recipient;
            if (currency != null && other.currency != null) currency = other.currency;
            if (date != null && other.date != null) date = other.date;
            if (amount != null && other.amount != null) amount = other.amount;
            if (balance != null && other.balance != null) balance = other.balance;
            return this;
        }

        public boolean isPresent(Field field) {
            switch (field) {
                case account:
                    return amount != null;
                case shop:
                    return recipient != null;
                case currency:
                    return currency != null;
                case date:
                    return date != null;
                case amount:
                    return amount != null;
                case balance:
                    return balance != null;

                case source:
                case none:
                default:
                    return false;
            }
        }

        public String getPayer() {
            return payer;
        }

        public void setPayer(String payer) {
            this.payer = payer;
        }

        public String getRecipient() {
            return recipient;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public Currency getCurrency() {
            return currency;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

    }
}
