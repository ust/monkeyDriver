package com.yrrlsv.fin;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CoreService {

    public static final String BROAD_PLACEHOLDER = "(.+)";

    private Set<Template> templates;
    private Map<Field, Parser> parsers;

    public CoreService(Set<Template> templates) {
        this.templates = templates;
        parsers = new EnumMap<>(Field.class);
        for (Field f : Field.fields) {
            parsers.put(f, Parser.create(f).get());
        }
    }

    public List<Event> parse(@NotNull String text) {
        Objects.requireNonNull(text);
        return templates.stream().map(template -> newEvent(template, text))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    protected Optional<Event> newEvent(@NotNull Template template, @NotNull String text) {
        Matcher matcher = Objects.requireNonNull(template).pattern().matcher(Objects.requireNonNull(text));
        if (matcher.find()) {
            Event.Builder builder = new Event.Builder();
            int i = 1;
            for (Placeholder placeholder : template.placeholders()) {
                List<Event.Builder> cases = CombinatorialTask.cases2(parsers, placeholder.fields(), matcher.group(i++));
                if (cases.size() == 1) {
                    builder.merge(cases.get(0));
                } else if (!cases.isEmpty()) {
                    throw new NotImplementedException("ambiguous variants while parsing :" + placeholder + " cases: " + cases);
                } else {
                    //throw new RuntimeException(placeholder.toString() + " cases: " + cases);
                }
                // else warn
            }
            return Optional.of(builder.type(template.type()).build());
        } else return Optional.empty();
    }

    public Template newTemplate(@NotNull String text, @NotNull EventType type, @NotNull List<FieldLocator> locators) {
        StringBuilder regex = new StringBuilder();
        List<Placeholder> placeholders = new ArrayList<>(locators.size());
        Event.Builder data = new Event.Builder();
        StringBuilder selectedText = new StringBuilder();
        List<Field> selectedFields = new ArrayList<>(locators.size());
        int cursor = 0;
        for (FieldLocator locator : locators) {
            String gap = text.substring(cursor, locator.start());
            String fieldData = text.substring(locator.start(), locator.end());
            List<Parser.Result> parsed = parsers.get(locator.field()).parse(fieldData);
            if (!parsed.isEmpty()) data.merge(parsed.get(0).data()); // get(0) ?

            if (selectedFields.size() == 0 || selectedText.indexOf(gap) == -1) {
                // escape special symbols
                regex.append(gap).append(BROAD_PLACEHOLDER);
                if (!selectedFields.isEmpty()) {
                    placeholders.add(new Placeholder(new ArrayList<>(selectedFields)));
                    selectedFields.clear();
                    selectedText.setLength(0);
                }
            } else {
                selectedText.append(gap).append(fieldData);
            }
            selectedFields.add(locator.field());
            cursor = locator.end();
        }
        if (!selectedFields.isEmpty()) {
            placeholders.add(new Placeholder(new ArrayList<>(selectedFields)));
            selectedFields.clear();
            selectedText.setLength(0);
        }
        regex.append(text.substring(cursor)); // last gap

        return new Template(type, regex.toString(), placeholders);
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

