package com.yrrlsv.fin;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CoreService {

    public static final String BROAD_PLACEHOLDER = "(.+)";
    private Set<Template> templates = new HashSet<>();

    public List<Template> seekTemplate(String text) {
        return templates.parallelStream().filter(template -> matches(template, text)).collect(Collectors.toList());
    }

    public boolean matches(Template template, String text) {
        return template.pattern().matcher(text).find();
    }

    public Event createEvent(Template template, String text) {
        Matcher matcher = template.pattern().matcher(text);
        if (matcher.find()) {
            int i = 1;
            Map<Field, String> data = new EnumMap<>(Field.class);
            for (Field field : template.type().fields()) {
                data.put(field, matcher.group(i++));
            }
            return new Event(template.type(), data);
        } else return null;
    }

    public Template createTemplate(String text, EventType type, List<Pair<Integer, Integer>> borders) {
        return new Template(type, createRegex(text, borders));
    }

    public String createRegex(String text, List<Pair<Integer, Integer>> borders) {
        StringBuilder regex = new StringBuilder();
        int lastChar = 0;
        for (Pair<Integer, Integer> border : borders) {
            regex.append(text.substring(lastChar, border.getLeft()));
            regex.append(BROAD_PLACEHOLDER);
            lastChar = border.getRight() + 1;
        }
        if (lastChar < text.length())
            regex.append(text.substring(lastChar));

        return regex.toString();
    }

    public boolean addTemplate(Template template) {
        return templates.add(template);
    }
}

