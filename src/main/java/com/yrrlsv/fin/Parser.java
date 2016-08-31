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
        // private String regex; ??

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

    List<Result> parse(Template.Content content, Message message, String data);

}

class DummyParser implements Parser {
    @Override
    public List<Result> parse(Template.Content content, Message message, String data) {
        return Collections.singletonList(new Result("", new Event.Builder(), ""));
    }
}

abstract class RegexParser<T> implements Parser {
    protected final Function<T, Event.Builder> applier;

    public RegexParser(Function<T, Event.Builder> applier) {
        this.applier = applier;
    }

    public List<Parser.Result> parse(Template.Content content, Message message, String data) {
        ImmutableList.Builder<Parser.Result> results = new ImmutableList.Builder<>();
        Matcher matcher = pattern(content).matcher(data);
        while (matcher.find()) {
            String group = matcher.group();
            results.add(new Parser.Result(
                    data.substring(0, matcher.start()),
                    applier.apply(format(content, group)),
                    data.substring(matcher.end())));
        }
        return results.build();
    }

    protected abstract Pattern pattern(Template.Content content);

    protected abstract T format(Template.Content content, String match);
}

class DateParser extends RegexParser<LocalDateTime> {

    // T0D0: cache DateTimeFormatter instead of compiling it each time
    public static final ImmutableMap<Pattern, Function<String, LocalDateTime>> formatters =
            new ImmutableMap.Builder<Pattern, Function<String, LocalDateTime>>()
                    .put(Pattern.compile("\\d\\d.\\d\\d\\.\\d\\d\\s\\d\\d\\:\\d\\d"),
                            s -> localDateTime(s, "dd.MM.yy HH:mm"))
                    .put(Pattern.compile("\\d\\d.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d\\:\\d\\d"),
                            s -> localDateTime(s, "dd.MM.yyyy HH:mm"))
                    .put(Pattern.compile("\\d{2}\\:\\d{2}\\:\\d{2} \\d{2}\\/\\d{2}\\/\\d{4}"),
                            s -> localDateTime(s, "HH:mm:ss dd/MM/yyyy"))
                    .put(Pattern.compile("\\d{14}"),
                            s -> localDateTime(s, "yyyyMMddHHmmss"))
                    .put(Pattern.compile("\\d{2}\\/\\d{2} \\d{2}:\\d{2}"),
                            s -> noYearDateTime(s, "dd/MM HH:mm"))
                    .put(Pattern.compile("\\d{2}\\.\\d{2} \\d{2}:\\d{2}"),
                            s -> noYearDateTime(s, "MM.dd HH:mm"))
                    .build();

    private static LocalDateTime localDateTime(String s, String pattern) {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern));
    }

    private static LocalDateTime noYearDateTime(String s, String pattern) {
        return localDateTime("2016_" + s, "yyyy_" + pattern); //  ¯\_(ツ)_/¯
    }

    public DateParser(Function<LocalDateTime, Event.Builder> builderFunction) {
        super(builderFunction);
    }

    @Override
    protected Pattern pattern(Template.Content content) {
        return content.getDateTimePattern();
    }

    @Override
    protected LocalDateTime format(Template.Content content, String match) {
        return LocalDateTime.parse(match, content.getDateTimeFormatter());
    }
}

class MoneyParser extends RegexParser<BigDecimal> implements Parser {
    private static final Pattern dotsComma =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\.\\d{3})+(?:\\,\\d+)?(?![\\'\\,\\.\\d])");
    private static final Pattern commasDots =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\,\\d{3})+(?:\\.\\d+)?(?![\\'\\,\\.\\d])");
    private static final Pattern quotes =
            Pattern.compile("(?<![\\'\\,\\.\\d])[1-9]\\d{0,2}(?:\\'\\d{3})+(?:[\\.\\,]\\d+)?(?![\\'\\,\\.\\d])");
    private static final Pattern justFloating =
            Pattern.compile("(?<![\\'\\,\\.\\d])(?:0|[1-9]\\d*)(?:[\\,\\.]\\d+)?(?![\\'\\,\\.\\d])");

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
        super(builderFunction);
    }

    @Override
    protected Pattern pattern(Template.Content content) {
        return content.moneyPattern();
    }

    @Override
    protected BigDecimal format(Template.Content content, String match) {
        try {
            return (BigDecimal) content.moneyFormat().parse(match);
        } catch (ParseException ignored) {
        }
        return BigDecimal.ZERO;

    }
}

class AccountParser implements Parser {
    private Function<String, Event.Builder> applier;

    AccountParser(Function<String, Event.Builder> applier) {
        this.applier = applier;
    }

    @Override
    public List<Result> parse(Template.Content content, Message message, String data) {
        return Collections.singletonList(new Result("", applier.apply(data), ""));
    }
}

class CurrencyParser implements Parser {
    @Override
    public List<Result> parse(Template.Content content, Message message, String data) {
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

