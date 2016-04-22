package com.yrrlsv.fin;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Template {
    private EventType type;
    private Pattern pattern;
    private List<Placeholder> placeholders;

    public Template(EventType type, String regex, List<Field> fields) {
        this.type = type;
        this.pattern = Pattern.compile(regex);
        this.placeholders = fields.stream().map(field -> new Placeholder(field, null))
                .collect(Collectors.toList());
    }

    public Template(EventType type, String regex, List<Placeholder> fields, Boolean flag) {
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

        return type == template.type && pattern.equals(template.pattern);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + pattern.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Template{" +
                "type=" + type +
                ", pattern=" + pattern.toString().replaceAll("\n", "\\n") +
                '}';
    }

    public List<Placeholder> placeholders() {
        return placeholders;
    }
}
