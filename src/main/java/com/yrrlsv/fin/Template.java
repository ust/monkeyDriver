package com.yrrlsv.fin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class Template {
    public static final String default_decimal_format = "###.##";
    public static final String default_currency_rgx = "[A-Z]{3}";

    private final EventType type;
    private final Pattern pattern;
    private final List<Placeholder> placeholders;
    private final Pattern dateTimePattern;
    private final DateTimeFormatter dateTimeFormatter;
    private final Pattern moneyPattern;
    private final DecimalFormat moneyFormat;
    private final Pattern currencyPattern;


    private static DecimalFormat decimalFormat(String moneyFormat) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        //symbols.setGroupingSeparator(',');
        // String pattern = "#,##0.0#"; // w/o grouping splitter "###.##"
        DecimalFormat decimalFormat = new DecimalFormat(moneyFormat, symbols);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }

    public Template(EventType type,
                    String pattern,
                    List<Placeholder> placeholders,
                    String dateTimeRegex,
                    String dateTimeFormat,
                    String moneyRegex,
                    String moneyFormat,
                    String currencyRegex) {
        this(type,
                Pattern.compile(pattern),
                placeholders,
                Pattern.compile(dateTimeRegex),
                DateTimeFormatter.ofPattern(dateTimeFormat),
                Pattern.compile(moneyRegex),
                decimalFormat(moneyFormat),
                Pattern.compile(currencyRegex));
    }


    public Template(EventType type,
                    Pattern pattern,
                    List<Placeholder> placeholders,
                    Pattern dateTimePattern,
                    DateTimeFormatter dateTimeFormatter,
                    Pattern moneyPattern,
                    DecimalFormat moneyFormat,
                    Pattern currencyPattern) {
        this.type = type;
        this.pattern = pattern;
        this.placeholders = placeholders;
        this.dateTimePattern = dateTimePattern;
        this.dateTimeFormatter = dateTimeFormatter;
        this.moneyPattern = moneyPattern;
        this.moneyFormat = moneyFormat;
        this.currencyPattern = currencyPattern;
    }


    public Pattern pattern() {
        return pattern;
    }

    public EventType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (type != template.type) return false;
        if (!pattern.equals(template.pattern)) return false;
        return placeholders.equals(template.placeholders);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + pattern.hashCode();
        result = 31 * result + placeholders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Template{" +
                "type=" + type +
                ", pattern=" + pattern +
                ", placeholders=" + placeholders +
                '}';
    }

    public List<Placeholder> placeholders() {
        return placeholders;
    }

    public Content content() {
        return Content.unmodifiable(new Content().setType(type)
                .setDateTimeFormatter(null).setMoneyPattern(null).setCurrencyPattern(null));
    }

    public static class Content {
        private EventType type;
        private Message message;
        private List<FieldLocator> locators;
        private Pattern dateTimePattern;
        private DateTimeFormatter dateTimeFormatter;
        private Pattern moneyPattern;
        private DecimalFormat moneyFormat;
        private Pattern currencyPattern;

        public static Content unmodifiable(Content original) {
            return new Content(original) {
                @Override
                public Content setType(EventType type) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public Content message(Message message) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public Content setLocators(List<FieldLocator> locators) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public Content setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public void setDateTimePattern(Pattern dateTimeRegex) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public Content setMoneyPattern(Pattern moneyRegex) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public void setMoneyFormat(DecimalFormat moneyFormat) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }

                @Override
                public Content setCurrencyPattern(Pattern currencyRegex) {
                    throw new UnsupportedOperationException("Cannot modify content");
                }
            };
        }

        private Content(Content content) {
            this.type = content.type;
            this.message = content.message;
            this.locators = content.locators;
            this.dateTimeFormatter = content.dateTimeFormatter;
            this.moneyPattern = content.moneyPattern;
            this.currencyPattern = content.currencyPattern;
        }

        public Content() {
        }

        public EventType type() {
            return type;
        }

        public Content setType(EventType type) {
            this.type = type;
            return this;
        }

        public Message message() {
            return message;
        }

        public Content message(Message message) {
            this.message = message;
            return this;
        }

        public List<FieldLocator> locators() {
            return locators;
        }

        public Content setLocators(List<FieldLocator> locators) {
            this.locators = locators;
            return this;
        }

        public DateTimeFormatter getDateTimeFormatter() {
            return dateTimeFormatter;
        }

        public Content setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
            return this;
        }

        public Pattern getDateTimePattern() {
            return dateTimePattern;
        }

        public void setDateTimePattern(Pattern dateTimePattern) {
            this.dateTimePattern = dateTimePattern;
        }

        public Pattern moneyPattern() {
            return moneyPattern;
        }

        public DecimalFormat moneyFormat() {
            return moneyFormat;
        }

        public void setMoneyFormat(DecimalFormat moneyFormat) {
            this.moneyFormat = moneyFormat;
        }

        public Content setMoneyPattern(Pattern moneyPattern) {
            this.moneyPattern = moneyPattern;
            return this;
        }

        public Pattern getCurrencyPattern() {
            return currencyPattern;
        }

        public Content setCurrencyPattern(Pattern currencyPattern) {
            this.currencyPattern = currencyPattern;
            return this;
        }

    }
}
