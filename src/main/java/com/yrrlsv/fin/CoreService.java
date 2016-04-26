package com.yrrlsv.fin;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CoreService {

    public static final String BROAD_PLACEHOLDER = "(.+)";

    private Set<Template> templates;
    private EnumMap<Field, Parser> parsers;

    public CoreService(Set<Template> templates, Set<Parser> parsers) {
        this.parsers = new EnumMap<>(Field.class);
        parsers.forEach(p -> this.parsers.put(p.type(), p));
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
            Event.Builder builder = new Event.Builder();
            int i = 1;
            for (Placeholder placeholder : template.placeholders()) {
                List<Event.Builder> cases = new CombinatorialTask(placeholder.fields(), matcher.group(i++)).search();
                if (cases.size() == 1) {
                    builder.merge(cases.get(0));
                } else {
                    throw new NotImplementedException("ambiguous variants" + cases);
                }
            }
            return Optional.of(builder.setType(template.type()).build());
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
            String fieldData = text.substring(locator.start(), locator.end());
            List<Parser.Result> results = parsers.get(locator.field()).parse(fieldData);
            if (!results.isEmpty()) {
                data.merge(results.get(0).data());
            }

            String gap = text.substring(cursor, locator.start());
            selectedFields.add(locator.field());

            if (selectedText.indexOf(gap) == -1 || cursor == 0) {
                regex.append(gap).append(BROAD_PLACEHOLDER);
                placeholders.add(new Placeholder(selectedFields));
                selectedFields.clear();
                selectedText.setLength(0);
            } else {
                selectedText.append(gap).append(fieldData);
            }

            cursor = locator.end();
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


    private class CombinatorialTask {
        private final Comparator<Event.Builder> full;
        private final Comparator<CombineStep> priority;
        private final TreeSet<CombineStep> queue;

        private CombinatorialTask(List<Field> fields, String data) {
            full = (o1, o2) -> {
                int count1 = 0;
                int count2 = 0;
                for (Field f : fields) {
                    if (o1.isPresent(f)) count1++;
                    if (o2.isPresent(f)) count2++;
                }
                return Integer.compare(count2, count1);
            };
            priority = (o1, o2) -> {
                int fullness = full.compare(o1.data, o2.data);
                if (fullness != 0)
                    return fullness;
                return Parser.priority.compare(o1.field, o2.field);
            };
            queue = new TreeSet<>(priority);

            for (Field field : fields) {
                queue.add(new CombineStep(fields, field, data, new Event.Builder()));
            }
        }

        private List<Event.Builder> search() {
            ImmutableList.Builder<Event.Builder> result = new ImmutableList.Builder<>();
            TreeSet<Event.Builder> processed = new TreeSet<>(full);
            while (!queue.isEmpty()) {
                CombineStep step = queue.pollFirst();
                processed.add(step.data);
                queue.addAll(step.next());
            }
            return result.addAll(processed).build();
        }
    }

    private class CombineStep {
        private List<Field> fields;
        private Field field;
        private String rowData;
        private Event.Builder data;

        private CombineStep(List<Field> fields, Field field, String rowData, Event.Builder data) {
            this.fields = fields;
            this.field = field;
            this.rowData = rowData;
            this.data = data;
        }

        private List<CombineStep> next() {
            ImmutableList.Builder<CombineStep> steps = new ImmutableList.Builder<>();
            List<Parser.Result> cases = parsers.get(field).parse(rowData);
            int myIndex = fields.indexOf(field);
            int currentIndex = 0;
            for (Parser.Result aCase : cases) {
                Event.Builder merged = data.merge(aCase.data());
                for (Field f : fields) {
                    if (currentIndex < myIndex) {
                        steps.add(new CombineStep(fields, f, aCase.before(), merged));
                    } else if (currentIndex > myIndex) {
                        steps.add(new CombineStep(fields, f, aCase.after(), merged));
                    }
                }
            }
            return steps.build();
        }
    }

}

