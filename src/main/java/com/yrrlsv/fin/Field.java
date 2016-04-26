package com.yrrlsv.fin;

public enum Field {


    account(true),
    shop(true),
    currency(true),

    date(false),
    amount(false),
    balance(false),

    source(false), // original message
    none(false), // transient field
    ;

    public static final Field[] fields = Field.values();


    private boolean unique;

    public static Field get(int ordinal) {
        return fields[ordinal];
    }

    Field(boolean unique) {
        this.unique = unique;
    }

    public boolean unique() {
        return unique;
    }
}
