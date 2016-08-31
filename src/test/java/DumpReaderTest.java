import com.yrrlsv.fin.AndroidBackupDataProvider;
import com.yrrlsv.fin.DumpReader;
import com.yrrlsv.fin.Event;
import com.yrrlsv.fin.EventBus;
import com.yrrlsv.fin.EventType;
import com.yrrlsv.fin.Placeholder;
import com.yrrlsv.fin.Template;
import com.yrrlsv.fin.TemplateProvider;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yrrlsv.fin.Field.account;
import static com.yrrlsv.fin.Field.amount;
import static com.yrrlsv.fin.Field.balance;
import static com.yrrlsv.fin.Field.currency;
import static com.yrrlsv.fin.Field.date;
import static com.yrrlsv.fin.Field.none;
import static com.yrrlsv.fin.Field.shop;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DumpReaderTest {

    private static final String otpDateTimeRegex = null;
    private static final String otpDateTimeFormatter = null;
    private static final String otpMoneyRegex = null;
    private static final String otpMoneyFormat = null;
    private static final String otpCurrencyRegex = null;

    private static final Template otp_charge_currency = new Template(EventType.charge,
            "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_charge2 = new Template(EventType.charge,
            "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_charge = new Template(EventType.charge,
            "(.+)\nSplata za tovar/poslugu.\n" +
                    "Kartka (.+). " +
                    "Suma:\n(.+). " +
                    "Misce:\n(.+).\n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_charge3 = new Template(EventType.charge,
            "OTP Smart: (.+)\r\n(.+)\r\n" +
                    "Spysannya na sumu: (.+)\r\n" +
                    "Zalyshok: (.+)\r\n", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_charge4 = new Template(EventType.charge,
            "(.+)\nC2C perekaz koshtiv z kartky:\n(.+)\n" +
                    "Suma: (.+)\n" +
                    "Misce: (.+)\n" +
                    "Zalyshok: (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_withdrawal3 = new Template(EventType.charge,
            "OTPdirekt:(.+): Otrymannya gotivky. " +
                    "Kartka (.+). " +
                    "Suma: (.+) . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_withdrawal4 = new Template(EventType.charge,
            "OTPdirekt:VIDMINA: (.+): Otrymannya gotivky. Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_withdrawal2 = new Template(EventType.charge,
            "(.+)\nOtrymannya gotivky v ATM.\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+). Misce:\n" +
                    "(.+). \n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_withdrawal = new Template(EventType.charge,
            "(.+)\nOtrymannya gotivky.\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+). Misce:\n" +
                    "(.+). \n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_cashier_withdrawal = new Template(EventType.charge,
            "OTPdirekt:(.+): " +
                    "Spysannya z rahunku: (.+). " +
                    "Suma: (.+) " +
                    "Zalyshok: (.+) " +
                    "Otrymuvach: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none), Placeholder.of(shop)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_cancel_limit = new Template(EventType.replenishment,
            "Vidmova:Nedostatno koshtiv abo limit\n" +
                    "(.+)\n" +
                    "Splata za tovar/poslugu.\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+). Misce:\n" +
                    "(.+). \n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_cancel = new Template(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_cancel2 = new Template(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): " +
                    "Povernennya tovaru. Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_cancel_wo_braces = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Povernennya tovaru. " +
                    "Kartka (.+). " +
                    "Suma: (.+) . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_replenish = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) " +
                    "Zalyshok: (.+) " +
                    "Platnyk: (.+)",
            Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                    Placeholder.of(balance), Placeholder.of(shop)), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_replenish1 = new Template(EventType.replenishment,
            "(.+)\nPopovnennya rahunku. \n" +
                    "Kartkа (.+). \n" +
                    "Suma: (.+).\n" +
                    "Misce: (.+)\n" +
                    "Zalyshok: (.+)",
            Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                    Placeholder.of(shop), Placeholder.of(balance)), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    // TODO check and normalize cyrilic sybols to their utf-8 analoguos i.g "К"->"K" damn f**..
    private static final Template otp_replenish2 = new Template(EventType.replenishment,
            "(.+)\nPopovnennya kartkovogo rahunku. \n" +
                    ".artkа (.+). \n" +
                    "Suma: (.+).\n" +
                    "Misce: (.+)\n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_replenish4 = new Template(EventType.replenishment,
            "(.+)\nPopovnennya kartky:\n(.+). \n" +
                    "Suma: (.+).\n" +
                    "Zalyshok: (.+)\n(.+) kredyt. limit (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(balance, none),
                    Placeholder.of(none), Placeholder.of(none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_replenish5 = new Template(EventType.replenishment,
            "(.+)\nPopovnennya kartky:\n(.+). \n" +
                    "Suma: (.+).\n" +
                    "Platnyk: (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(shop)), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_replenish3 = new Template(EventType.replenishment,
            "OTP Smart: (.+)\r\n(.+)\r\nPopovnennya na sumu: (.+)\r\nZalyshok: (.+)\r\n",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
                    Placeholder.of(balance)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_deposit = new Template(EventType.deposit,
            "OTPdirekt:(.+): " +
                    "Popovnennya depozytu z rahunku (.+). " +
                    "Suma: (.+) " +
                    "Balans rahunku: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_deposit2 = new Template(EventType.deposit,
            "OTPdirekt:(.+): Zarakhuvannya sumy depozytu na rakhunok: (.+). Suma: (.+) Zalyshok: (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(balance, none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_deposit3 = new Template(EventType.deposit,
            "OTPdirekt:(.+): Zarakhuvannya vidsotkiv na rakhunok: (.+). Suma: (.+) Zalyshok: (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(balance, none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_deposit_withdraw = new Template(EventType.depositWithdraw,
            "OTPdirekt:(.+): " +
                    "Spysannya z depozytu na rahunok: (.+). " +
                    "Suma: (.+) " +
                    "Balans rahunku: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_credit_reminder = new Template(EventType.promo,
            "Uvaga! Za period (.+) " +
                    "za rakhunkom (.+) " +
                    "zaborgovanist skladae (.+). " +
                    "Obovyazkovyi platizh (.+) " +
                    "do (.+).", Arrays.<Placeholder>asList(Placeholder.of(none), Placeholder.of(none),
            Placeholder.of(none), Placeholder.of(none), Placeholder.of(none)
    ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_access = new Template(EventType.promo,
            "OTP Smart: (.+)\nParol dlya pershoho vhodu v systemu:\n(.+)\nDiysnyi do (.+) \n",
            Arrays.<Placeholder>asList(Placeholder.of(none), Placeholder.of(none), Placeholder.of(none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_access2 = new Template(EventType.promo,
            "OTP Smart: (.+)\r\nVhid do systemy.\r\nKod avtoryzatsii: (.+).\r\nDiysnyi do (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(none), Placeholder.of(none), Placeholder.of(none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);
    private static final Template otp_access_money = new Template(EventType.promo,
            "OTP Smart: (.+)\r\n(.+)\r\nSuma: (.+)\r\nKod avtoryzatsii: (.+)\r\nDiysnyi do (.+)",
            Arrays.<Placeholder>asList(Placeholder.of(none), Placeholder.of(none), Placeholder.of(none), Placeholder.of(none),
                    Placeholder.of(none)
            ), otpDateTimeRegex, otpDateTimeFormatter, otpMoneyRegex, otpMoneyFormat, otpCurrencyRegex);

    public static final TemplateProvider FAKE_TEMPLATE_PROVIDER = new TemplateProvider() {
        @Override
        public Optional<Template> newTemplate(String message) {
            return Optional.empty();
        }

        @Override
        public Event chooseTemplate(List<Event> templates) {
            return templates.get(0);
        }
    };


    private EventBus bus = new EventBus();

    @Test
    public void otpOnly_20160111231728() {
        new DumpReader(FAKE_TEMPLATE_PROVIDER,
                new AndroidBackupDataProvider(getClass().getResource("otpOnly-20160111231728.xml").getPath()),
                bus,
                Arrays.asList(
                        otp_cancel_limit, otp_cancel, otp_cancel2, otp_cancel_wo_braces,
                        otp_replenish, otp_replenish1, otp_replenish2, otp_replenish3, otp_replenish4, otp_replenish5,
                        otp_charge_currency, otp_charge4, otp_charge3, otp_charge2, otp_charge,
                        otp_withdrawal4, otp_withdrawal3, otp_withdrawal2, otp_withdrawal, otp_cashier_withdrawal,
                        otp_deposit, otp_deposit2, otp_deposit3, otp_deposit_withdraw,
                        otp_credit_reminder, otp_access, otp_access2, otp_access_money //otp_access_withrow_depo,
                )).maxErrors(5).execute();

        Map<EventType, List<Event>> types =
                bus.events().stream().collect(Collectors.groupingBy(Event::type));

        // -------------------------------------------------------------------------------------------------------------
        List<Event> charged = types.get(EventType.charge);
        List<Event> replenished = types.get(EventType.replenishment);
        List<Event> deposits = types.get(EventType.deposit);
        List<Event> withdrawals = types.get(EventType.depositWithdraw);
        List<Event> promo = types.get(EventType.promo);
        List<Event> failed = types.getOrDefault(EventType.failed, Collections.emptyList());

        printFailed(failed);
        assertThat(charged.size(), is(1194));
        assertThat(replenished.size(), is(186));
        assertThat(deposits.size(), is(69));
        assertThat(withdrawals.size(), is(62));
        assertThat(promo.size(), is(70));
        assertThat(failed.size(), is(0));

        Set<LocalDateTime> datas = bus.events().stream().map(Event::date).collect(Collectors.toSet());
        System.out.println(datas);
        final int[] i = {0};
        bus.events().forEach(event -> {
            if (event.type() != EventType.promo && event.date() == null)
                System.out.println("" + ++i[0] + "\t" + event);
        });
    }

    private void printFailed(List<Event> failed) {
        int maxSize = 50;
        System.out.println(failed.isEmpty()
                ? "\t\t\tGREAT JOB! Processed size: " + bus.events().size()
                : "\nlet's check that " + maxSize + "/" + failed.size() + ":");
        failed.stream().limit(maxSize).forEach(event -> System.out.println("\t" + event.data()));
    }

}
