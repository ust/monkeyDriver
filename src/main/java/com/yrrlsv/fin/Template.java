package com.yrrlsv.fin;

import java.util.Set;
import java.util.regex.Pattern;

public class Template {
    private EventType type;
    private Pattern pattern;

    public Template(EventType type, String regex) {
        this.type = type;
        this.pattern = Pattern.compile(regex);
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
}
