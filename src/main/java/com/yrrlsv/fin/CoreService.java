package com.yrrlsv.fin;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreService {

    public static final String BROAD_PLACEHOLDER = "(.+)";

    private Set<Template> templates;
    private Map<Field, Parser> parsers;

    public CoreService(Set<Template> templates) {
        this.templates = validateTemplates(templates);
        this.parsers = Stream.of(Field.fields)
                .collect(Collectors.toMap(key -> key, field -> Parser.create(field).get()));
    }

    protected Set<Template> validateTemplates(Set<Template> templates) {
        return templates;
    }

    public List<Event> parse(Message message) {
        // warn: refine message i.g replace all \r\n  and cyrillic symbols
        return templates.stream()
                .map(t -> newEvent(t, message))
                .filter(Optional::isPresent)
                .map(e -> Collections.singletonList(e.get()))
                .findFirst().orElse(Collections.emptyList());
    }

    protected Optional<Event> newEvent(Template template, Message message) {
        Matcher matcher = template.pattern().matcher(message.text());
        if (matcher.find()) {
            Event.Builder builder = new Event.Builder();
            int i = 1;
            for (Placeholder placeholder : template.placeholders()) {
                List<Event.Builder> cases = new CombinatorialTask(template.content(), message)
                        .doThat(placeholder.fields(), matcher.group(i++));
                if (cases.size() == 1) {
                    builder.merge(cases.get(0));
                } else if (!cases.isEmpty()) {
                    //builder.merge(c_ases.get(0));
                    throw new NotImplementedException("ambiguous variants while parsing :" + placeholder + " variants: " + cases);
                }
                //throw new RuntimeException(placeholder.toString() + " c_ases: " + c_ases);
            }
            return Optional.of(builder.type(template.type()).build());
        } else return Optional.empty();
    }

    public Template newTemplate(Template.Content content) {
        String text = content.message().text();
        StringBuilder regex = new StringBuilder();
        List<Placeholder> placeholders = new ArrayList<>(content.locators().size());
        Event.Builder data = new Event.Builder();
        StringBuilder selectedText = new StringBuilder();
        List<Field> selectedFields = new ArrayList<>(content.locators().size());
        int cursor = 0;
        for (FieldLocator locator : content.locators()) {
            String gap = text.substring(cursor, locator.start());
            String fieldData = text.substring(locator.start(), locator.end());
            List<Parser.Result> parsed = parsers.get(locator.field()).parse(content, content.message(), fieldData);
            if (!parsed.isEmpty()) data.merge(parsed.get(0).data()); // get(0) ?

            if (selectedFields.size() == 0 || selectedText.indexOf(gap) == -1) {
                // warn: escape special symbols
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
        regex.append(text.substring(cursor)); // tail

        return new Template(content.type(), Pattern.compile(regex.toString()), placeholders,
                content.getDateTimePattern(), content.getDateTimeFormatter(), content.getCurrencyPattern(),
                content.moneyFormat(), content.getCurrencyPattern());
    }


    public boolean addTemplate(Template template) {
        return templates.add(template);
    }

    private class CombinatorialTask {
        private final Template.Content content;
        private final Message message;

        private CombinatorialTask(Template.Content content, Message message) {
            this.content = content;
            this.message = message;
        }


        private List<Event.Builder> doThat(List<Field> fields, String rowData) {
            if (fields.isEmpty()) return Collections.singletonList(new Event.Builder());
            else if (rowData.isEmpty()) return Collections.emptyList();

            ImmutableList.Builder<Event.Builder> merged = new ImmutableList.Builder<>();
            Field aim = Collections.min(fields, Parser.priority);
            for (Parser.Result aCase : parsers.get(aim).parse(content, message, rowData)) {
                int indexOf = fields.indexOf(aim);
                List<Event.Builder> left = doThat(fields.subList(0, indexOf), aCase.before());
                List<Event.Builder> right = doThat(fields.subList(indexOf + 1, fields.size()), aCase.after());
                for (Event.Builder l : left) {
                    for (Event.Builder r : right) {
                        merged.add(aCase.data().merge(l).merge(r));
                    }
                }
            }
            return merged.build();
        }
    }


}

