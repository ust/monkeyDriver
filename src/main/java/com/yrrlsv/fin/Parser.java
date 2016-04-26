package com.yrrlsv.fin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

interface Parser {

    Comparator<Field> priority = new Comparator<Field>() {

        private final List<Field> order =
                Arrays.asList(Field.date, Field.amount, Field.account, Field.currency, Field.shop, Field.none);

        @Override
        public int compare(Field o1, Field o2) {
            return Integer.compare(order.indexOf(o1), order.indexOf(o2));
        }
    };

    class Result {
        private String before;
        private Event.Builder data;
        private String after;

        public Result(String before, Event.Builder data, String after) {
            this.before = before;
            this.data = data;
            this.after = after;
        }

        public String before() {
            return before;
        }

        public Event.Builder data() {
            return data;
        }

        public String after() {
            return after;
        }
    }

    Field type();

    List<Result> parse(String data);
}
