package com.yrrlsv.fin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;

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

    public String payer() {
        return payer;
    }

    public String getRecipient() {
        return recipient;
    }

    public Currency currency() {
        return currency;
    }

    public LocalDateTime date() {
        return date;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (type != event.type) return false;
        if (payer != null ? !payer.equals(event.payer) : event.payer != null) return false;
        if (recipient != null ? !recipient.equals(event.recipient) : event.recipient != null) return false;
        if (currency != null ? !currency.equals(event.currency) : event.currency != null) return false;
        if (date != null ? !date.equals(event.date) : event.date != null) return false;
        if (amount != null ? amount.compareTo(event.amount) != 0 : event.amount != null) return false;
        return balance != null ? balance.compareTo(event.balance) == 0 : event.balance == null;

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (payer != null ? payer.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", payer='" + payer + '\'' +
                ", recipient='" + recipient + '\'' +
                ", currency=" + currency +
                ", date=" + date +
                ", amount=" + amount +
                ", balance=" + balance +
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
            return new Event(Objects.requireNonNull(type), payer, recipient, currency, date, amount, balance);
        }

        public EventType getType() {
            return type;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder merge(Builder other) {
            // type?
//            Builder result = new Builder();
//            result.payer = this.payer != null ? this.payer : other.payer;
//            result.recipient = this.recipient != null ? this.recipient : other.recipient;
//            result.currency = this.currency != null ? this.currency : other.currency;
//            result.date = this.date != null ? this.date : other.date;
//            result.amount = this.amount != null ? this.amount : other.amount;
//            result.balance = this.balance != null ? this.balance : other.balance;
            if (payer == null && other.payer != null) payer = other.payer;
            if (recipient == null && other.recipient != null) recipient = other.recipient;
            if (currency == null && other.currency != null) currency = other.currency;
            if (date == null && other.date != null) date = other.date;
            if (amount == null && other.amount != null) amount = other.amount;
            if (balance == null && other.balance != null) balance = other.balance;
            return this;
        }

        public boolean isPresent(Field field) {
            switch (field) {
                case account:
                    return payer != null;
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

        public String payer() {
            return payer;
        }

        public Builder payer(String payer) {
            this.payer = payer;
            return this;
        }

        public String recipient() {
            return recipient;
        }

        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Currency getCurrency() {
            return currency;
        }

        public Builder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public LocalDateTime date() {
            return date;
        }

        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public BigDecimal amount() {
            return amount;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public BigDecimal balance() {
            return balance;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;

            if (type != builder.type) return false;
            if (payer != null ? !payer.equals(builder.payer) : builder.payer != null) return false;
            if (recipient != null ? !recipient.equals(builder.recipient) : builder.recipient != null) return false;
            if (currency != null ? !currency.equals(builder.currency) : builder.currency != null) return false;
            if (date != null ? !date.equals(builder.date) : builder.date != null) return false;
            if (amount != null ? amount.compareTo(builder.amount) != 0 : builder.amount != null) return false;
            return balance != null ? balance.compareTo(builder.balance) == 0 : builder.balance == null;

        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (payer != null ? payer.hashCode() : 0);
            result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
            result = 31 * result + (currency != null ? currency.hashCode() : 0);
            result = 31 * result + (date != null ? date.hashCode() : 0);
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            result = 31 * result + (balance != null ? balance.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "type=" + type +
                    ", payer='" + payer + '\'' +
                    ", recipient='" + recipient + '\'' +
                    ", currency=" + currency +
                    ", date=" + date +
                    ", amount=" + amount +
                    ", balance=" + balance +
                    '}';
        }
    }

}
