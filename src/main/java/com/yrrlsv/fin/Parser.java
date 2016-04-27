package com.yrrlsv.fin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.joestelmach.natty.DateGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yrrlsv.fin.Field.account;
import static com.yrrlsv.fin.Field.amount;
import static com.yrrlsv.fin.Field.balance;
import static com.yrrlsv.fin.Field.currency;
import static com.yrrlsv.fin.Field.date;
import static com.yrrlsv.fin.Field.none;
import static com.yrrlsv.fin.Field.shop;

public interface Parser {

    Comparator<Field> priority = new Comparator<Field>() {

        private final List<Field> order = Arrays.asList(date, account, currency, amount, balance, shop, none);

        @Override
        public int compare(Field o1, Field o2) {
            return Integer.compare(order.indexOf(o1), order.indexOf(o2));
        }
    };

    class Result {
        public static final List<Result> empty = Collections.emptyList();

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

    static Optional<Parser> create(Field field) {
        switch (field) {
            case date:
                return Optional.of(new NattyParser());
            case amount:
                return Optional.of(new MoneyParser(amount -> new Event.Builder().amount(amount)));
            case balance:
                return Optional.of(new MoneyParser(amount -> new Event.Builder().balance(amount)));
            case account:
            case shop:
                return Optional.of(new AccountParser());
            case currency:
                return Optional.of(new CurrencyParser());
            case source:
            case none:
            default:
                return Optional.of(new DummyParser());
        }
    }

    List<Result> parse(String data);
}

class DummyParser implements Parser {
    @Override
    public List<Result> parse(String data) {
        return Collections.emptyList();
    }
}


abstract class RegexParser<T> implements Parser {
    protected final Set<Pattern> patterns;
    protected final Function<T, Event.Builder> applier;

    protected RegexParser(Set<Pattern> patterns, Function<T, Event.Builder> applier) {
        this.patterns = patterns;
        this.applier = applier;
    }

    public List<Parser.Result> parse(String data) {
        ImmutableList.Builder<Parser.Result> results = new ImmutableList.Builder<>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                results.add(new Parser.Result(
                        data.substring(0, matcher.start()),
                        applier.apply(onMatch(pattern, data)),
                        data.substring(matcher.end())));
            }
        }
        return results.build();
    }

    protected abstract T onMatch(Pattern pattern, String data);
}

class DateTimeParser extends RegexParser<LocalDateTime> {
    private Map<Pattern, DateTimeFormatter> formatters;

    DateTimeParser(Map<Pattern, DateTimeFormatter> formatters) {
        super(formatters.keySet(), time -> new Event.Builder().date(time));
        this.formatters = formatters;
    }

    @Override
    protected LocalDateTime onMatch(Pattern pattern, String data) {
        return LocalDateTime.parse(data, formatters.get(pattern));
    }
}

class DateTimeCompositeParser implements Parser {
    private static final Map<Pattern, DateTimeFormatter> dateFormatters = new ImmutableMap.Builder<Pattern, DateTimeFormatter>()
            .put(null, null) // TODO
            .build();
    private static final Map<Pattern, DateTimeFormatter> timeFormatters = new ImmutableMap.Builder<Pattern, DateTimeFormatter>()
            .put(null, null) // TODO
            .build();
    private final DateTimeParser dateParser;
    private final DateTimeParser timeParser;

    DateTimeCompositeParser() {
        timeParser = new DateTimeParser(dateFormatters);
        dateParser = new DateTimeParser(timeFormatters);
    }

    @Override
    public List<Result> parse(String data) {
        List<Result> results = timeParser.parse(data);
        return null;
    }
}

class NattyParser implements Parser {
    private static final com.joestelmach.natty.Parser parser = new com.joestelmach.natty.Parser();

    @Override
    public List<Result> parse(String data) {
        ImmutableList.Builder<Result> results = new ImmutableList.Builder<>();
        for (DateGroup group : parser.parse(data)) {
            Date date = group.getDates().get(0); // mmmm get(0)???
            results.add(new Result(data.substring(0, group.getAbsolutePosition()),
                    new Event.Builder().date(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())),
                    data.substring(group.getAbsolutePosition() + group.getText().length())));
        }
        return results.build();
    }
}

class MoneyParser extends RegexParser<BigDecimal> {

    private static final String regex = "[0-9]+((\\.|,)[0-9][0-9])?";
    private static final Pattern pattern = Pattern.compile(regex);

    MoneyParser(Function<BigDecimal, Event.Builder> builderFunction) {
        super(Collections.singleton(pattern), builderFunction);
    }

    @Override
    protected BigDecimal onMatch(Pattern pattern, String data) {
        return BigDecimal.valueOf(Double.valueOf(data.replace(',', '.')));
    }
}

class AccountParser implements Parser {
    Set<String> accounts = new HashSet<>();

    @Override
    public List<Result> parse(String data) {
        return Collections.singletonList(new Result("", new Event.Builder().payer(data), ""));
    }
}

class CurrencyParser implements Parser {
    @Override
    public List<Result> parse(String data) {
        return Collections.singletonList(new Result("", new Event.Builder().setCurrency(Currency.getInstance(data)), ""));
    }
}

