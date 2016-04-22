package com.yrrlsv.fin;

public class FieldLocator {
    private Field field;
    private int start;
    private int end;

    public FieldLocator(Field field, int start, int end) {
        this.field = field;
        this.start = start;
        this.end = end;
    }

    public Field field() {
        return field;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
