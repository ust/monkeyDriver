package com.yrrlsv.fin;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CoreService {

    public static final String BROAD_PLACEHOLDER = "(.+)";
    private Set<Template> templates;

    public CoreService(Set<Template> templates) {
        this.templates = templates;
    }

    public List<Event> parse(@NotNull String text) {
        Objects.requireNonNull(text);
        return templates.stream().map(template -> newEvent(template, text))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    protected Optional<Event> newEvent(@NotNull Template template, @NotNull String text) {
        Matcher matcher = Objects.requireNonNull(template).pattern().matcher(Objects.requireNonNull(text));
        if (matcher.find()) {
            int i = 1;
            Map<Field, String> data = new EnumMap<>(Field.class);
            for (Placeholder placeholder : template.placeholders())
                if (Field.none != placeholder.field())
                    data.put(placeholder.field(), matcher.group(i++));
            return Optional.of(new Event(template.type(), data));
        } else return Optional.empty();
    }

    public Template newTemplate(@NotNull String text, @NotNull EventType type, @NotNull List<FieldLocator> locators) {
        List<Field> fields = new ArrayList<>();
        List<Pair<Integer, Integer>> borders = new ArrayList<>();
        locators.forEach(l -> borders.add(new ImmutablePair<>(l.start(), l.end())));
        return new Template(type, newRegex(text, borders), fields);
    }

    public Template newTemplate2(@NotNull String text, @NotNull EventType type, @NotNull List<FieldLocator> locators) {
        StringBuilder regex = new StringBuilder();
        ArrayList<Placeholder> placeholders = new ArrayList<>();
        EnumMap<Field, String> data = new EnumMap<>(Field.class);
        int lastChar = 0;
        String lastData = "";
        Placeholder placeholder = null;
        for (FieldLocator  locator : locators) {
            String gap = text.substring(lastChar, locator.start());
            String currentData = text.substring(locator.start(), locator.end());
            data.put(locator.field(), currentData);
            if (lastData.contains(gap)) {
                placeholder = new Placeholder(locator.field(), placeholder);
                continue;
            }

            regex.append(gap);
            regex.append(BROAD_PLACEHOLDER);
            lastChar = locator.end() + 1;
            placeholder = new Placeholder(locator.field(), null);
            placeholders.add(placeholder);
        }
        if (lastChar < text.length())
            regex.append(text.substring(lastChar));

        return new Template(type, regex.toString(), null);

    }

    protected String newRegex(@NotNull String text, @NotNull List<Pair<Integer, Integer>> input) {
        StringBuilder regex = new StringBuilder();
        int lastChar = 0;
        String lastData = "";
        for (Pair<Integer, Integer> field : input) {
            String gap = text.substring(lastChar, field.getLeft());
            if (lastData.contains(gap))
                continue;

            regex.append(gap);
            regex.append(BROAD_PLACEHOLDER);
            lastChar = field.getRight() + 1;
        }
        if (lastChar < text.length())
            regex.append(text.substring(lastChar));

        return regex.toString();
    }

    public boolean addTemplate(@NotNull Template template) {
        return templates.add(template);
    }
}

