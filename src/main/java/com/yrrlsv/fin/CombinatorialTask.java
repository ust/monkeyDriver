package com.yrrlsv.fin;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CombinatorialTask {

    public static List<Event.Builder> cases2(Map<Field, Parser> parsers, List<Field> fields, String rowData) {
        return new RecursionTask(parsers, fields, rowData).go();
    }

    private static class RecursionTask {
        private final Map<Field, Parser> parsers;
        private final List<Field> initFields;
        private final String initRowData;

        private RecursionTask(Map<Field, Parser> parsers, List<Field> initFields, String initRowData) {
            this.parsers = parsers;
            this.initFields = initFields;
            this.initRowData = initRowData;
        }


        private List<Event.Builder> go() {
            return goRecursive(initFields, initRowData);
        }

        private List<Event.Builder> goRecursive(List<Field> fields, String rowData) {
            if (fields.isEmpty()) return Collections.singletonList(new Event.Builder());
            else if (rowData.isEmpty()) return Collections.emptyList();

            ImmutableList.Builder<Event.Builder> merged = new ImmutableList.Builder<>();
            Field aim = Collections.min(fields, Parser.priority);
            for (Parser.Result aCase : parsers.get(aim).parse(rowData)) {
                int indexOf = fields.indexOf(aim);
                List<Event.Builder> left = goRecursive(fields.subList(0, indexOf), aCase.before());
                List<Event.Builder> right = goRecursive(fields.subList(indexOf + 1, fields.size()), aCase.after());
                for (Event.Builder l : left) {
                    for (Event.Builder r : right) {
                        merged.add(aCase.data().merge(l).merge(r));
                    }
                }
            }
            return merged.build();
        }
    }


    public static List<Event.Builder> cases(Map<Field, Parser> parsers, List<Field> fields, String rowData) {
        return (fields.size() > 1
                ? new CombinatorialFunction(parsers, fields, rowData)
                : new SimpleParseTask(parsers.get(fields.get(0)), rowData)
        ).get();
    }

    private static class SimpleParseTask implements Supplier<List<Event.Builder>> {

        private final Parser parser;
        private final String rowData;

        public SimpleParseTask(Parser parser, String rowData) {
            this.parser = parser;
            this.rowData = rowData;
        }

        @Override
        public List<Event.Builder> get() {
            return parser.parse(rowData).stream().map(Parser.Result::data).collect(Collectors.toList());
        }
    }

    private static class CombinatorialFunction implements Supplier<List<Event.Builder>> {
        private final Comparator<Event.Builder> fullness;
        private final Comparator<CombineStep> priority;
        private final TreeSet<CombineStep> queue;
        private final List<Field> fields;

        private CombinatorialFunction(Map<Field, Parser> parsers, List<Field> fields, String data) {
            this.fields = fields;
            fullness = (o1, o2) -> {
                int count1 = 0;
                int count2 = 0;
                for (Field f : fields) {
                    if (o1.isPresent(f)) count1++;
                    if (o2.isPresent(f)) count2++;
                }
                return Integer.compare(count2, count1);
            };
            priority = (o1, o2) -> {
                int fullness = this.fullness.compare(o1.data(), o2.data());
                if (fullness != 0)
                    return fullness;
                return Parser.priority.compare(o1.field(), o2.field());
            };
            queue = new TreeSet<>(priority);

            for (Field field : fields) {
                queue.add(new CombineStep(parsers, fields, field, data, new Event.Builder()));
            }
        }

        public List<Event.Builder> get() {
            TreeSet<Event.Builder> processed = new TreeSet<>(fullness);
            while (!queue.isEmpty()) {
                CombineStep step = queue.pollFirst();
                processed.add(step.data());
                queue.addAll(step.next());
            }

            ImmutableList.Builder<Event.Builder> result = new ImmutableList.Builder<>();
            boolean full = false;
            outer:
            for (Event.Builder b : processed) {
                for (Field f : fields) {
                    if (!b.isPresent(f)) {
                        break outer;
                    }
                }
                result.add(b);
                full = true;
            }
            if (!full) {
                result.addAll(processed);
            }

            return result.build();
        }
    }

    private static class CombineStep {
        private List<Field> fields;
        private Field target;
        private String rowData;
        private Event.Builder data;
        private Map<Field, Parser> parsers;

        CombineStep(Map<Field, Parser> parsers, List<Field> fields, Field target, String rowData, Event.Builder data) {
            this.parsers = parsers;
            this.fields = fields;
            this.target = target;
            this.rowData = rowData;
            this.data = data;
        }

        public List<CombineStep> next() {
            ImmutableList.Builder<CombineStep> steps = new ImmutableList.Builder<>();
            List<Parser.Result> cases = parsers.get(target).parse(rowData);
            for (Parser.Result aCase : cases) {
                Event.Builder merged = aCase.data().merge(data);
                // if there are fields and data is empty, meant the whole brunch is not correct
                if (!aCase.before().isEmpty()) {
                    for (Field f : fields) {
                        if (!merged.isPresent(f)) {
                            steps.add(new CombineStep(parsers, fields, f, aCase.before(), merged));
                        }
                    }
                }
                if (!aCase.after().isEmpty()) {
                    for (Field f : fields) {
                        if (!merged.isPresent(f)) {
                            steps.add(new CombineStep(parsers, fields, f, aCase.after(), merged));
                        }
                    }
                }
            }
            return steps.build();
        }

        public Field field() {
            return target;
        }

        public Event.Builder data() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CombineStep that = (CombineStep) o;

            if (!fields.equals(that.fields)) return false;
            if (target != that.target) return false;
            if (!rowData.equals(that.rowData)) return false;
            return data.equals(that.data);

        }

        @Override
        public int hashCode() {
            int result = fields.hashCode();
            result = 31 * result + target.hashCode();
            result = 31 * result + rowData.hashCode();
            result = 31 * result + data.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "CombineStep{" +
                    "fields=" + fields +
                    ", field=" + target +
                    ", rowData='" + rowData + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

}
