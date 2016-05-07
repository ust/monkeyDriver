package com.yrrlsv.fin;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Template {
    private EventType type;
    private Pattern pattern;
    private List<Placeholder> placeholders;

    public static Template flatTemplate(EventType type, String regex, List<Field> fields) {
        return new Template(type, regex, fields.stream().map(field -> new Placeholder(Collections.singletonList(field)))
                .collect(Collectors.toList()));
    }

    public static Template newTemplate(EventType type, String regex, List<Placeholder> placeholders) {
        return new Template(type, regex, placeholders);
    }

    public Template(EventType type, String regex, List<Placeholder> placeholders) {
        this.type = type;
        this.pattern = Pattern.compile(regex);
        this.placeholders = placeholders;
    }

    public Pattern pattern() {
        return pattern;
    }

    public EventType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (type != template.type) return false;
        if (!pattern.equals(template.pattern)) return false;
        return placeholders.equals(template.placeholders);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + pattern.hashCode();
        result = 31 * result + placeholders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Template{" +
                "type=" + type +
                ", pattern=" + pattern +
                ", placeholders=" + placeholders +
                '}';
    }

    public List<Placeholder> placeholders() {
        return placeholders;
    }
}
