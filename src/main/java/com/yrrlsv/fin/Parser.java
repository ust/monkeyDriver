package com.yrrlsv.fin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        private final List<Field> order = Arrays.asList(date, amount, balance, currency, account, shop, none);

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

        @Override
        public String toString() {
            return "Result{" +
                    "before='" + before + '\'' +
                    ", data=" + data +
                    ", after='" + after + '\'' +
                    '}';
        }
    }

    static Optional<Parser> create(Field field) {
        switch (field) {
            case date:
                return Optional.of(new DateParser(date -> new Event.Builder().date(date)));
            case amount:
                return Optional.of(new MoneyParser(amount -> new Event.Builder().amount(amount)));
            case balance:
                return Optional.of(new MoneyParser(amount -> new Event.Builder().balance(amount)));
            case account:
                return Optional.of(new AccountParser(s -> new Event.Builder().payer(s)));
            case shop:
                return Optional.of(new AccountParser(s -> new Event.Builder().recipient(s)));
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
        return Collections.singletonList(new Result("", new Event.Builder(), ""));
    }
}

class RegexParser<T> implements Parser {
    protected final Function<T, Event.Builder> applier;
    protected final Map<Pattern, Function<String, T>> patterns;

    public RegexParser(Map<Pattern, Function<String, T>> patterns, Function<T, Event.Builder> builderFunction) {
        this.patterns = patterns;
        applier = builderFunction;
    }

    public List<Parser.Result> parse(String data) {
        ImmutableList.Builder<Parser.Result> results = new ImmutableList.Builder<>();
        for (Map.Entry<Pattern, Function<String, T>> entry : patterns.entrySet()) {
            Matcher matcher = entry.getKey().matcher(data);
            while (matcher.find()) {
                String group = matcher.group();
                results.add(new Parser.Result(
                        data.substring(0, matcher.start()),
                        applier.apply(entry.getValue().apply(group)),
                        data.substring(matcher.end())));
            }
        }
        return results.build();
    }
}

class DateParser extends RegexParser<LocalDateTime> {

    public static final ImmutableMap<Pattern, Function<String, LocalDateTime>> formatters =
            new ImmutableMap.Builder<Pattern, Function<String, LocalDateTime>>()
                    .put(Pattern.compile("\\d\\d.\\d\\d\\.\\d\\d\\s\\d\\d\\:\\d\\d"),
                            s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")))
                    .put(Pattern.compile("\\d\\d.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d\\:\\d\\d"),
                            s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .build();

    public DateParser(Function<LocalDateTime, Event.Builder> builderFunction) {
        super(formatters, builderFunction);
    }
}

class MoneyParser extends RegexParser<BigDecimal> implements Parser {
    private static final Pattern dotsComma =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\.\\d{3})+(?:\\,\\d{2,})?(?![\\'\\,\\.\\d])");
    private static final Pattern commasDots =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\,\\d{3})+(?:\\.\\d{2,})?(?![\\'\\,\\.\\d])");
    private static final Pattern quotes =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\'\\d{3})+(?:[\\.\\,]\\d{2,})?(?![\\'\\,\\.\\d])");
    private static final Pattern justFloating =
            Pattern.compile("(?<![\\'\\,\\.\\d])(?:0|[1-9]\\d*)(?:[\\,\\.]\\d{2,})?(?![\\'\\,\\.\\d])");

    public static final ImmutableMap<Pattern, Function<String, BigDecimal>> refiners =
            new ImmutableMap.Builder<Pattern, Function<String, BigDecimal>>()
                    .put(dotsComma, s -> format(s.replace(".", "").replace(',', '.')))
                    .put(commasDots, s -> format(s.replace(",", "")))
                    .put(quotes, s -> format(s.replace("\'", "").replace(',', '.')))
                    .put(justFloating, s -> format(s.replace(',', '.')))
                    .build();

    private static final DecimalFormat format;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        //symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        // String pattern = "#,##0.0#";
        format = new DecimalFormat("###.##", symbols);
        format.setParseBigDecimal(true);
    }

    private static BigDecimal format(String s) {
        try {
            return (BigDecimal) format.parse(s);
        } catch (ParseException ignored) {
        }
        return BigDecimal.ZERO;
    }

    MoneyParser(Function<BigDecimal, Event.Builder> builderFunction) {
        super(refiners, builderFunction);
    }
}

class AccountParser implements Parser {
    private Function<String, Event.Builder> applier;

    AccountParser(Function<String, Event.Builder> applier) {
        this.applier = applier;
    }

    @Override
    public List<Result> parse(String data) {
        return Collections.singletonList(new Result("", applier.apply(data), ""));
    }
}

class CurrencyParser implements Parser {
    @Override
    public List<Result> parse(String data) {
        ImmutableList.Builder<Result> results = new ImmutableList.Builder<>();
        String upperCaseData = data.toUpperCase();
        for (Currency currency : Currency.getAvailableCurrencies()) {
            int indexOf = upperCaseData.indexOf(currency.getCurrencyCode());
            if (indexOf != -1) {
                results.add(new Result(data.substring(0, indexOf),
                        new Event.Builder().currency(currency),
                        data.substring(indexOf + currency.getCurrencyCode().length())));
            }
        }
        return results.build();
    }
}

