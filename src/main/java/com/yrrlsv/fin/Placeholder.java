package com.yrrlsv.fin;

class Placeholder {

    private Field field;
    private Placeholder placeholder;

    protected Placeholder(Field field, Placeholder placeholder) {
        this.field = field;
        this.placeholder = placeholder;
    }

    public Field field() {
        return field;
    }
}