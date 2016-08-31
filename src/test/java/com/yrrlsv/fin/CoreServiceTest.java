package com.yrrlsv.fin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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

    private CoreService core;

    @Before
    public void cleanUp() {
        core = new CoreService(new HashSet<>());
    }

    @Test
    public void createPlaceholders() {
        Message text = new Message("----------AAAAA-----1005.00UAH-----NNNNN"); // 10 + 5 + 5 + 5+5
        String expected = "----------(.+)-----(.+)-----NNNNN";
        Template template = core.newTemplate(new Template.Content().message(text).setType(EventType.failed)
                .setLocators(FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 10, 15},
                        {Field.amount.ordinal(), 20, 27}, {Field.currency.ordinal(), 27, 30}})));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));

    }

    @Test
    public void createPlaceholdersBegining() {
        Message text = new Message("AAAAA-----1005.00UAH-----NNNNN"); // 10 + 5 + 5 + 5+5
        String expected = "(.+)-----(.+)-----NNNNN";
        Template template = core.newTemplate(new Template.Content().message(text).setType(EventType.failed)
                .setLocators(FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 0, 5},
                        {Field.amount.ordinal(), 10, 17}, {Field.currency.ordinal(), 17, 20}})));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));

    }

    @Test
    public void createPlaceholdersEnding() {
        Message text = new Message("----------AAAAA-----1005.00UAH"); // 10 + 5 + 5 + 5+5
        String expected = "----------(.+)-----(.+)";
        Template template = core.newTemplate(new Template.Content().message(text).setType(EventType.failed)
                .setLocators(FieldLocator.listOf(new int[][]{{Field.account.ordinal(), 10, 15},
                        {Field.amount.ordinal(), 20, 27}, {Field.currency.ordinal(), 27, 30}})));
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

        Message message = new Message("OTPdirekt:VIDMINA: 04.12.14 23:04: " +
                "Povernennya tovaru. Kartka *8310. " +
                "Suma: 2.500,00UAH (2.500,00UAH). " +
                "Misce: DELTA PAY2YOU 2 KIEV. " +
                "Zalyshok: 103,40UAH.");
        String regex = "OTPdirekt:VIDMINA: (.+): " +
                "Povernennya tovaru. Kartka (.+). " +
                "Suma: (.+) \\((.+)\\). " +
                "Misce: (.+). " +
                "Zalyshok: (.+).";
        List<Placeholder> placeholders = Arrays.<Placeholder>asList(
                Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
        );
        String dateTimeRegex = null;
        String dateTimeFormatter = null;
        String moneyRegex = null;
        String moneyFormat = null;
        String currencyRegex = null;
        Template template = new Template(EventType.replenishment, regex, placeholders,
                dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);

        CoreService service = new CoreService(Collections.singleton(template));
        List<Event> events = service.parse(message);
        Event expected = new Event.Builder().type(EventType.replenishment)
                .date(LocalDateTime.parse("2014-12-04T23:04"))
                .payer("*8310").amount(new BigDecimal(2500)).currency(Currency.getInstance("UAH"))
                .recipient("DELTA PAY2YOU 2 KIEV").balance(new BigDecimal("103.40")).build();

        assertThat(events.get(0), is(expected));
    }

    @Test
    public void extended2ndTemplate() {
        String ok = "OTPdirekt:22.05.2015 15:19: Popovnennya rahunku: 26253455156239. " +
                "Suma: 2000,00 UAH Zalyshok: -6637,41 UAH Platnyk: Yaroslav Patenko";
        Message msg = new Message("OTPdirekt:25.05.2015 16:42: Popovnennya rahunku: 26208455083205. " +
                "Suma: 19,18 UAH Zalyshok: --- UAH Platnyk: VS PIF \"Arhentum\" TOV \"Drahon Eset Men");

        String dateTimeRegex = null;
        String dateTimeFormatter = null;
        String moneyRegex = null;
        String moneyFormat = null;
        String currencyRegex = null;
        Template otp_replenish = new Template(EventType.replenishment,
                "OTPdirekt:(.+): " +
                        "Popovnennya rahunku: (.+). " +
                        "Suma: (.+) " +
                        "Zalyshok: (.+) " +
                        "Platnyk: (.+)",
                Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                        Placeholder.of(balance, none), Placeholder.of(shop)),
                dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
        Template otp_replenish_wo_balance = new Template(EventType.replenishment,
                "OTPdirekt:(.+): " +
                        "Popovnennya rahunku: (.+). " +
                        "Suma: (.+) " +
                        "Zalyshok: --- UAH " +
                        "Platnyk: (.+)",
                Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                        Placeholder.of(shop)),
                dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
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
        Message rub = new Message("OTPdirekt:05.01.15 09:28: Splata za tovar/poslugu. " +
                "Kartka *8310. " +
                "Suma: -334,00RUB (-88,62UAH). " +
                "Misce: CAFE KROSHKA KARTOSHKA HIMKI. " +
                "Zalyshok: 1.546,91UAH.");

        String dateTimeRegex = null;
        String dateTimeFormatter = null;
        String moneyRegex = null;
        String moneyFormat = null;
        String currencyRegex = null;
        Template otp_charge = new Template(EventType.charge,
                "(.+)\nSplata za tovar/poslugu.\n" +
                        "Kartka (.+). " +
                        "Suma:\n(.+). " +
                        "Misce:\n(.+).\n" +
                        "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
        ), dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
        Template otp_charge1 = new Template(EventType.charge,
                "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                        "Kartka (.+). " +
                        "Suma: (.+). " +
                        "Misce: (.+). " +
                        "Zalyshok: (.+).",
                Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                        Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
                ), dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
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
                ), dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
        Template otp_cancel = new Template(EventType.replenishment,
                "OTPdirekt:VIDMINA: (.+): Splata za tovar/poslugu. " +
                        "Kartka (.+). " +
                        "Suma: (.+) \\((.+)\\). " +
                        "Misce: (.+). " +
                        "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
        ), dateTimeRegex, dateTimeFormatter, moneyRegex, moneyFormat, currencyRegex);
        CoreService service = new CoreService(new LinkedHashSet<>(Arrays.asList(
                /*otp_cancel, */otp_charge2/*, otp_charge1, otp_charge*/)));
        List<Event> events = service.parse(rub);
        assertThat(events.size(), is(1));
        System.out.println(events.get(0));
    }


}
