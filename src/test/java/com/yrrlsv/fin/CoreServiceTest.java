package com.yrrlsv.fin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yrrlsv.fin.Field.account;
import static com.yrrlsv.fin.Field.amount;
import static com.yrrlsv.fin.Field.balance;
import static com.yrrlsv.fin.Field.currency;
import static com.yrrlsv.fin.Field.date;
import static com.yrrlsv.fin.Field.none;
import static com.yrrlsv.fin.Field.shop;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CoreServiceTest {

    private static final String SMS_1 = "08/01 18:34\n" +
            "Splata za tovar/poslugu.\n" + // 36
            "Kartka *5768. Suma:\n" +
            "196.21 UAH. Misce:\n" +
            "SHOP KUMUSHKA CHEKISTI.\n" + // 98
            "Zalyshok: 2833.51"; // 115
    private static final String SMS_2 = "11/01 14:06\n" +
            "Splata za tovar/poslugu.\n" +
            "Kartka *5768. Suma:\n" +
            "112.87 UAH. Misce:\n" +
            "KAFE-PIZZERIYA MARIOS. \n" +
            "Zalyshok: 2518.73 UAH";

    private static final List<FieldLocator> BORDERS =
            Arrays.asList(new FieldLocator(date, 0, 10), new FieldLocator(account, 44, 48),
                    new FieldLocator(amount, 57, 62), new FieldLocator(shop, 76, 97),
                    new FieldLocator(balance, 110, 116));

    private static final String REGEX_1 = "(.+)\nSplata za tovar/poslugu.\n" +
            "Kartka (.+). " +
            "Suma:\n(.+) UAH. " +
            "Misce:\n(.+).\n" +
            "Zalyshok: (.+)";
    private static final String REGEX_2 = "(.+)\nSplata za tovar/poslugu.\n" +
            "Kartka (.+). " +
            "Suma:\n(.+) UAH. " +
            "Misce:\n(.+).\n" +
            "Zalyshok: (.+) UAH";


    private static final Template TEMPLATE_1 = Template.flatTemplate(EventType.charge, REGEX_1,
            Arrays.asList(date, account, amount, shop, balance));
    private static final Template TEMPLATE_2 = Template.flatTemplate(EventType.charge, REGEX_2,
            Arrays.asList(date, account, amount, shop, balance));

    private static final Map<Field, String> DATA_1 = new ImmutableMap.Builder<Field, String>()
            .put(Field.date, "08/01 18:34")
            .put(Field.account, "*5768")
            .put(Field.amount, "196.21")
            .put(Field.shop, "SHOP KUMUSHKA CHEKISTI")
            .put(Field.balance, "2833.51").build();
    private static final Map<Field, String> DATA_2 = new ImmutableMap.Builder<Field, String>()
            .put(Field.date, "11/01 14:06")
            .put(Field.account, "*5768")
            .put(Field.amount, "112.87")
            .put(Field.shop, "KAFE-PIZZERIYA MARIOS.")
            .put(Field.balance, "2518.73 UAH").build();

    private CoreService core;

    @Before
    public void cleanUp() {
        core = new CoreService(new HashSet<>());
    }

    @Ignore
    @Test
    public void createRegex() {
        Assert.assertEquals(REGEX_1, core.newRegex(SMS_1,
                BORDERS.stream().map(l -> Pair.of(l.start(), l.end())).collect(Collectors.toList())));
    }

    @Test
    @Ignore
    public void createTemplate() {
        Assert.assertEquals(TEMPLATE_1, core.newTemplate(SMS_1, EventType.charge, BORDERS));
    }

    @Ignore
    @Test
    public void createEvent() {
        Assert.assertEquals(new Event(EventType.charge, DATA_1), core.newEvent(TEMPLATE_1, SMS_1).get());
    }

    @Ignore
    @Test
    public void createSubsequentEvent() {
        Assert.assertEquals(new Event(EventType.charge, DATA_2), core.newEvent(TEMPLATE_1, SMS_2).get());
    }

    @Ignore
    @Test
    public void seekTemplate() {
        core.addTemplate(TEMPLATE_1);
        core.addTemplate(TEMPLATE_2);
        assertThat(new HashSet<>(core.parse(SMS_2)), is(new ImmutableSet.Builder<>()
                .add(TEMPLATE_1, TEMPLATE_2).build()));
    }

    @Test
    public void createPlaceholders() {
        String text = "----------AAAAA-----1005.00UAH-----NNNNN"; // 10 + 5 + 5 + 5+5
        String expected = "----------(.+)-----(.+)-----NNNNN";
        Template template = core.newTemplate(text, EventType.failed,
                FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 10, 15},
                        {Field.amount.ordinal(), 20, 27}, {Field.currency.ordinal(), 27, 30}}));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));

    }

    @Test
    public void createPlaceholdersBegining() {
        String text = "AAAAA-----1005.00UAH-----NNNNN"; // 10 + 5 + 5 + 5+5
        String expected = "(.+)-----(.+)-----NNNNN";
        Template template = core.newTemplate(text, EventType.failed,
                FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 0, 5},
                        {Field.amount.ordinal(), 10, 17}, {Field.currency.ordinal(), 17, 20}}));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));

    }

    @Test
    public void createPlaceholdersEnding() {
        String text = "----------AAAAA-----1005.00UAH"; // 10 + 5 + 5 + 5+5
        String expected = "----------(.+)-----(.+)";
        Template template = core.newTemplate(text, EventType.failed,
                FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 10, 15},
                        {Field.amount.ordinal(), 20, 27}, {Field.currency.ordinal(), 27, 30}}));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));
    }

    @Test
    public void recognizeFcknMessage() {
        /*
        * TEST for regex101.com
        *
        * UAH 0,33UAH 2,90UAH 1000,10UAH 20.000,10UAH 2.000.500,00000UAH
            USD 0.25USD 2USD 400USD 12,122USD 34,435,500.09USD
            GBP 1.33GBP 2GBP 20GBP 2010GBP 20'000GBP 1.01GBP 2'000'000.00GBP
            GBP 2GBP 20GBP 2010GBP 2'000GBP 1,01GBP 2'000'000,00GBP

            comas 10,560UAH
            dot 1.00USD 0.11USD 0,00012USD 10.345USD
            103,40UAH
            ----------------CONFUSING:

            wrong formats:   10'99ERR 15,2ERR 15,ERR 4,555'000.444.00ERR
                        100.100.330'999,00
            zeros:        00ERR 001.10ERR  000,90ERR 00,31ERR 00.000.00,00ERR
        *
        * */

        String message = "OTPdirekt:VIDMINA: 04.12.14 23:04: " +
                "Povernennya tovaru. Kartka *8310. " +
                "Suma: 2.500,00UAH (2.500,00UAH). " +
                "Misce: DELTA PAY2YOU 2 KIEV. " +
                "Zalyshok: 103,40UAH.";
        String regex = "OTPdirekt:VIDMINA: (.+): " +
                "Povernennya tovaru. Kartka (.+). " +
                "Suma: (.+) \\((.+)\\). " +
                "Misce: (.+). " +
                "Zalyshok: (.+).";
        List<Placeholder> placeholders = Arrays.<Placeholder>asList(
                Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
        );
        Template template = new Template(EventType.replenishment, regex, placeholders);

        CoreService service = new CoreService(Collections.singleton(template));
        List<Event> events = service.parse(message);
        Event expected = new Event.Builder().type(EventType.replenishment)
                .date(LocalDateTime.parse("2014-12-04T23:04"))
                .payer("*8310").amount(new BigDecimal(2500)).currency(Currency.getInstance("UAH"))
                .recipient("DELTA PAY2YOU 2 KIEV").balance(new BigDecimal("103.40")).build();

        assertThat(events.get(0), is(expected));
    }

    @Test
    public void smthWentWrong() {
        String text = "OTPdirekt:29.09.2014 16:04: " +
                "Popovnennya rahunku: 26208455083205. " +
                "Suma: 9500,00 UAH Zalyshok: 9500,00 UAH " +
                "Platnyk: Patenko Yaroslav Sviatoslavovych";
        String tmplt = "OTPdirekt:(.+): Popovnennya rahunku: (.+). Suma: (.+) Zalyshok: (.+) UAH Platnyk: (.+)";

        Template template = Template.newTemplate(EventType.replenishment, tmplt,
                Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                        Placeholder.of(balance), Placeholder.of(shop)));
        List<Event> results = new CoreService(Collections.singleton(template)).parse(text);
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(new Event.Builder().type(EventType.replenishment)
                .date(LocalDateTime.parse("2014-09-29T16:04")).currency(Currency.getInstance("UAH"))
                .payer("26208455083205").amount(new BigDecimal("9500")).balance(new BigDecimal("9500"))
                .recipient("Patenko Yaroslav Sviatoslavovych").build()));
    }

    @Test
    public void extended2ndTemplate() {
        String ok = "OTPdirekt:22.05.2015 15:19: Popovnennya rahunku: 26253455156239. " +
                "Suma: 2000,00 UAH Zalyshok: -6637,41 UAH Platnyk: Yaroslav Patenko";
        String msg = "OTPdirekt:25.05.2015 16:42: Popovnennya rahunku: 26208455083205. " +
                "Suma: 19,18 UAH Zalyshok: --- UAH Platnyk: VS PIF \"Arhentum\" TOV \"Drahon Eset Men";

        Template otp_replenish = new Template(EventType.replenishment,
                "OTPdirekt:(.+): " +
                        "Popovnennya rahunku: (.+). " +
                        "Suma: (.+) " +
                        "Zalyshok: (.+) " +
                        "Platnyk: (.+)",
                Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                        Placeholder.of(balance, none), Placeholder.of(shop)));
        Template otp_replenish_wo_balance = new Template(EventType.replenishment,
                "OTPdirekt:(.+): " +
                        "Popovnennya rahunku: (.+). " +
                        "Suma: (.+) " +
                        "Zalyshok: --- UAH " +
                        "Platnyk: (.+)",
                Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                        Placeholder.of(shop)));
        CoreService service = new CoreService(Sets.newHashSet(otp_replenish, otp_replenish_wo_balance));
        List<Event> events = service.parse(msg);
        assertThat(events.size(), is(1));
        System.out.println(events.get(0));
    }

    @Test
    public void wtf() {
        String msg = "OTPdirekt:VIDMINA: 10.12.14 22:24: Splata za tovar/poslugu. " +
                "Kartka *8310. " +
                "Suma: -226,00UAH (-226,00UAH). " +
                "Misce: CAFE PODSHOFFE KYIV. " +
                "Zalyshok: 5.604,40UAH.";
        String rub = "OTPdirekt:05.01.15 09:28: Splata za tovar/poslugu. " +
                "Kartka *8310. " +
                "Suma: -334,00RUB (-88,62UAH). " +
                "Misce: CAFE KROSHKA KARTOSHKA HIMKI. " +
                "Zalyshok: 1.546,91UAH.";

        Template otp_charge = new Template(EventType.charge,
                "(.+)\nSplata za tovar/poslugu.\n" +
                        "Kartka (.+). " +
                        "Suma:\n(.+). " +
                        "Misce:\n(.+).\n" +
                        "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
        ));
        Template otp_charge1 = new Template(EventType.charge,
                "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                        "Kartka (.+). " +
                        "Suma: (.+). " +
                        "Misce: (.+). " +
                        "Zalyshok: (.+).",
                Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                        Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
                ));
        String reg = "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                "Kartka *8310. " +
                "Suma: -334,00RUB (-88,62UAH). " +
                "Misce: CAFE KROSHKA KARTOSHKA HIMKI. " +
                "Zalyshok: 1.546,91UAH.";
        Template otp_charge2 = new Template(EventType.charge,
                "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                        "Kartka (.+). " +
                        "Suma: (.+) \\((.+)\\). " +
                        "Misce: (.+). " +
                        "Zalyshok: (.+).",
                Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                        Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
                ));
        Template otp_cancel = new Template(EventType.replenishment,
                "OTPdirekt:VIDMINA: (.+): Splata za tovar/poslugu. " +
                        "Kartka (.+). " +
                        "Suma: (.+) \\((.+)\\). " +
                        "Misce: (.+). " +
                        "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
        ));
        CoreService service = new CoreService(new LinkedHashSet<>(Arrays.asList(
                /*otp_cancel, */otp_charge2/*, otp_charge1, otp_charge*/)));
        List<Event> events = service.parse(rub);
        assertThat(events.size(), is(1));
        System.out.println(events.get(0));
    }



}
