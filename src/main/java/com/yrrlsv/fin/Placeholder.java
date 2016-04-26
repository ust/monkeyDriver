package com.yrrlsv.fin;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Placeholder {

    private List<Field> fields;

    public static Placeholder of(Field... fields) {
        return new Placeholder(Arrays.asList(fields));
    }

    Placeholder(@NotNull List<Field> fields) {
        if (fields.isEmpty())
            throw new IllegalArgumentException();
        this.fields = fields;
    }

    public List<Field> fields() {
        return fields;
    }

    @Override
    public String toString() {
        return "Placeholder{" +
                "fields=" + fields +
                '}';
    }
}