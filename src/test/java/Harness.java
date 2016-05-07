import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.ParseLocation;
import com.joestelmach.natty.Parser;
import com.yrrlsv.fin.CoreService;
import com.yrrlsv.fin.Event;
import com.yrrlsv.fin.EventType;
import com.yrrlsv.fin.Field;
import com.yrrlsv.fin.FieldLocator;
import com.yrrlsv.fin.Message;
import com.yrrlsv.fin.Messages;
import com.yrrlsv.fin.Placeholder;
import com.yrrlsv.fin.Template;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yrrlsv.fin.Field.account;
import static com.yrrlsv.fin.Field.amount;
import static com.yrrlsv.fin.Field.balance;
import static com.yrrlsv.fin.Field.currency;
import static com.yrrlsv.fin.Field.date;
import static com.yrrlsv.fin.Field.none;
import static com.yrrlsv.fin.Field.shop;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Harness {

    @Test
    public void loadFile() {
        System.out.println(getClass().getResource("otp_charge_1.xml"));
    }

    @Test
    public void unmarshalOneXml() throws JAXBException {
        String backupXml = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"sms.xsl\"?>\n" +
                "<!--File Created By SMS Backup & Restore v7.46 on 11/01/2016 23:18:05-->\n" +
                "<smses count=\"1\">\n" +
                "<sms protocol=\"0\" " +
                "address=\"OTP Bank\" " +
                "date=\"1451847415771\" " +
                "type=\"1\" " +
                "subject=\"null\" " +
                "body=\"03/01 20:56&#10;" +
                /**/"Splata za tovar/poslugu.&#10;" +
                /**/"Kartka *5768. Suma:&#10;" +
                /**/"80.65 UAH. Misce:&#10;" +
                /**/"PR500. &#10;" +
                /**/"Zalyshok: 4082.55 UAH\" " +
                "toa=\"null\" " +
                "sc_toa=\"null\" " +
                "service_center=\"+380443736994\" " +
                "read=\"1\" status=\"-1\" " +
                "locked=\"0\" " +
                "date_sent=\"1451847413000\" " +
                "readable_date=\"3 Jan 2016 20:56:55\" " +
                "contact_name=\"(Unknown)\" />" +
                "</smses>";

        Unmarshaller unmarshaller = JAXBContext.newInstance(Messages.class, Message.class).createUnmarshaller();
        Messages messages = (Messages) unmarshaller.unmarshal(new StringReader(backupXml));
        System.out.println(messages);
    }

    private void printPlaceholders(String template, String sms) {
        Matcher matcher = Pattern.compile(template).matcher(sms);
        System.out.println("------------------------count: " + matcher.groupCount());
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                System.out.println(group);
            }
        }
    }

    @Test
    public void pattern() {
        String sms = "08/01 18:34\nSplata za tovar poslugu.\n" + // 36
                "Kartka *5768. Suma\n196.21 UAH. " + // 67
                "Misce:\nSHOP KUMUSHKA CHEKISTI.\n" + // 98
                "Zalyshok: 2833.51";

        String template = "(.+) Splata za tovar poslugu. " +
                "Kartka (.+). " +
                "Suma (.+) UAH. " +
                "Misce: (.+). " +
                "Zalyshok: (.+)";

        printPlaceholders(template, sms);
    }

    @Test
    public void explained() {
        String text = "Thanks, this is your value : 100. And this is your account number : 219AD098";
        String regex = "Thanks, this is your value : (.+). And this is your account number : (.+)";
        printPlaceholders(regex, text);
    }

    @Test
    public void placeholdersColision() {
        String sms = "08/01 18:34\nSplata za tovar poslugu.\n" + // 36
                "Kartka *5768. Suma\n196.21 UAH. " + // 67
                "Misce:\nSHOP KUMUSHKA CHEKISTI.\n" + // 98
                "Zalyshok: 2833.51";

        String template = "(.+)\n" +
                "Splata za tovar poslugu.\n" +
                "Kartka (.+). Suma\n" +
                "(.+) UAH. Misce:\n(.+).\n" +
                "Zalyshok: (.+)";
        String template2 = "(.+)\n" +
                "Splata za tovar poslugu.\n" +
                "Kartka (.+). Suma\n" +
                "(.+) (.+). Misce:\n(.+).\n" +
                "Zalyshok: (.+)";
        String template3 = "(.+)\n" +
                "Splata za tovar poslugu.\n" +
                "Kartka (.+). Suma\n" +
                "(.+)(.+). Misce:\n(.+).\n" +
                "Zalyshok: (.+)";

//        printPlaceholders(template, sms);
//        printPlaceholders(template2, sms);
        printPlaceholders(template3, sms);
    }

    @Test
    public void mergedPlaceholders() {
        int account = Field.account.ordinal();
        int amount = Field.amount.ordinal();
        int currency = Field.currency.ordinal();
        CoreService core = new CoreService(new HashSet<>());

        String text = "----------AAAAA-----1005.00UAH-----NNNNN"; // 10 + 5 + 5 + 5+5
        String expected = "----------(.+)-----(.+)-----NNNNN";
        Template template = core.newTemplate(text, EventType.failed,
                FieldLocator.listOf(new int[][]{{account, 10, 15}, {amount, 20, 27}, {currency, 27, 30}}));
        assertThat(template.pattern().pattern(), is(expected));
        assertThat(template.placeholders(),
                is(Arrays.asList(Placeholder.of(Field.account), Placeholder.of(Field.amount, Field.currency))));

        core.addTemplate(template);
        List<Event> actual = core.parse(text);
        Event expectedEvent = new Event.Builder().type(EventType.failed)
                .payer("AAAAA").amount(new BigDecimal(1005.00))
                .currency(Currency.getInstance("UAH"))
                .build();
        assertThat(actual.get(0), is(expectedEvent));
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
                .date(LocalDateTime.parse("2016-05-06T04:12:14"))
                .payer("*8310").amount(new BigDecimal(2500)).currency(Currency.getInstance("UAH"))
                .recipient("DELTA PAY2YOU 2 KIEV").balance(new BigDecimal("103.40")).build();

        System.out.println(events.get(0).equals(expected));
        assertThat(events.get(0), is(expected));
    }

    @Test
    public void hollyDateParser() {
        //analyzeHollyParser("\n\tthe day before next thursday");
        analyzeHollyParser("22.12.14 13:46");
//        analyzeHollyParser("OTPdirekt:22.12.14 13:46: " +
//                "Splata za tovar/poslugu. " +
//                "Kartka *8310. " +
//                "Suma: -138,00UAH . " +
//                "Misce: PIZZERIA MARIOS KYIV. " +
//                "Zalyshok: 7.732,95UAH.");
    }

    private void analyzeHollyParser(String value) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(value);
        System.out.println("---groups:" + groups);
        for (DateGroup group : groups) {
            System.out.println("---group:" + group);
            List dates = group.getDates();
            System.out.println("dates:" + dates);
            int line = group.getLine();
            int column = group.getPosition();
            System.out.println("line:" + line + "   column: " + column + "    absolute: " + group.getAbsolutePosition());
            System.out.println(group.getFullText());
            String matchingValue = group.getText();
            System.out.println("matchingValue:" + matchingValue);

            String syntaxTree = group.getSyntaxTree().toStringTree();
            System.out.println("syntaxTree:" + syntaxTree);
            Map<String, List<ParseLocation>> parseMap = group.getParseLocations();
            System.out.println("parseMap:" + parseMap);
            boolean isRecurreing = group.isRecurring();
            Date recursUntil = group.getRecursUntil();
            System.out.println("recursUntil:" + recursUntil);
        }
    }
}
