package com.yrrlsv.fin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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


    public static List<FieldLocator> listOf(int[][] borders) {
        return Arrays.stream(borders).map(b -> new FieldLocator(Field.get(b[0]), b[1], b[2])).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldLocator that = (FieldLocator) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        return field == that.field;

    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }

    @Override
    public String toString() {
        return "FieldLocator{" +
                "field=" + field +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
