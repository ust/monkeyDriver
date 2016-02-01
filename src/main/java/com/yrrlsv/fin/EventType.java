package com.yrrlsv.fin;

import java.util.EnumSet;
import java.util.Set;

public enum EventType {
    charge, replenishment, deposit, failed, promo;

    public Set<Field> fields() {
        switch (this) {
            case charge:
                return EnumSet.of(Field.date, Field.account, Field.amount, Field.shop, Field.balance);
            case replenishment:
                return EnumSet.of(Field.date, Field.account, Field.amount, Field.shop, Field.balance);
            case deposit:
                return EnumSet.of(Field.date, Field.account, Field.amount, Field.balance);
            case failed:
            default:
                return EnumSet.noneOf(Field.class);
        }
    }
}
