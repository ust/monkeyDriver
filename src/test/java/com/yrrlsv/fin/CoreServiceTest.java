package com.yrrlsv.fin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yrrlsv.fin.Field.*;
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

    @Test
    public void createEvent() {
        Assert.assertEquals(new Event(EventType.charge, DATA_1), core.newEvent(TEMPLATE_1, SMS_1).get());
    }

    @Test
    public void createSubsequentEvent() {
        Assert.assertEquals(new Event(EventType.charge, DATA_2), core.newEvent(TEMPLATE_1, SMS_2).get());
    }

    @Test
    public void seekTemplate() {
        core.addTemplate(TEMPLATE_1);
        core.addTemplate(TEMPLATE_2);
        assertThat(new HashSet<>(core.parse(SMS_2)), is(new ImmutableSet.Builder<>()
                .add(TEMPLATE_1, TEMPLATE_2).build()));
    }

}
