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

    private boolean unique;

    Field(boolean unique) {
        this.unique = unique;
    }

    public boolean unique() {
        return unique;
    }
}
