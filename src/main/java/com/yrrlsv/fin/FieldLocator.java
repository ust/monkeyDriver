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
}
