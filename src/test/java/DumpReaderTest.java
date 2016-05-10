import com.yrrlsv.fin.AndroidBackupDataProvider;
import com.yrrlsv.fin.DumpReader;
import com.yrrlsv.fin.Event;
import com.yrrlsv.fin.EventBus;
import com.yrrlsv.fin.EventType;
import com.yrrlsv.fin.Placeholder;
import com.yrrlsv.fin.Template;
import com.yrrlsv.fin.TemplateProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private static final Template otp_charge = Template.flatTemplate(EventType.charge,
            "(.+)\nSplata za tovar/poslugu.\n" +
                    "Kartka (.+). " +
                    "Suma:\n(.+) (.+). " +
                    "Misce:\n(.+).\n" +
                    "Zalyshok: (.+)", Arrays.asList(date, account, amount, currency, shop, balance));
    private static final Template otp_charge2 = new Template(EventType.charge,
            "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
            ));
    private static final Template otp_charge_currency = new Template(EventType.charge,
            "OTPdirekt:(.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+). \\((.+)\\)" +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
            ));
    private static final Template otp_withdrawal = new Template(EventType.charge,
            "(.+)\nOtrymannya gotivky v ATM\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+). Misce:\n" +
                    "(.+). \n" +
                    "Zalyshok: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ));
    private static final Template otp_withdrawal2 = new Template(EventType.charge,
            "OTPdirekt:(.+): Otrymannya gotivky. " +
                    "Kartka (.+). " +
                    "Suma: (.+) . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ));
    private static final Template otp_cashier_withdrawal = new Template(EventType.charge,
            "OTPdirekt:(.+): " +
                    "Spysannya z rahunku: (.+). " +
                    "Suma: (.+) " +
                    "Zalyshok: (.+) " +
                    "Otrymuvach: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none), Placeholder.of(shop)
    ));
    private static final Template otp_cancel = new Template(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
    ));
    private static final Template otp_cancel2 = new Template(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): " +
                    "Povernennya tovaru. Kartka (.+). " +
                    "Suma: (.+) \\((.+)\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(none), Placeholder.of(shop), Placeholder.of(balance, none)
    ));
    private static final Template otp_cancel_wo_braces = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Povernennya tovaru. " +
                    "Kartka (.+). " +
                    "Suma: (.+) . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+).", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance, none)
    ));
    private static final Template otp_replenish = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) " +
                    "Zalyshok: (.+) " +
                    "Platnyk: (.+)",
            Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency, none),
                    Placeholder.of(balance), Placeholder.of(shop)));
    //    private static final Template otp_replenish_wo_balance = new Template(EventType.replenishment,
    //            "OTPdirekt:(.+): " +
    //                    "Popovnennya rahunku: (.+). " +
    //                    "Suma: (.+) " +
    //                    "Zalyshok: --- UAH " +
    //                    "Platnyk: (.+)",
    //            Arrays.asList(Placeholder.of(date), Placeholder.of(account), Placeholder.of(amount, currency),
    //                    Placeholder.of(shop)));
    private static final Template otp_deposit = new Template(EventType.deposit,
            "OTPdirekt:(.+): " +
                    "Popovnennya depozytu z rahunku (.+). " +
                    "Suma: (.+) " +
                    "Balans rahunku: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none)
    ));
    private static final Template otp_deposit_withdraw = new Template(EventType.depositWithdraw,
            "OTPdirekt:(.+): " +
                    "Spysannya z depozytu na rahunok: (.+). " +
                    "Suma: (.+) " +
                    "Balans rahunku: (.+)", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(balance, none)
    ));
    private static final Template otp_credit_reminder = new Template(EventType.promo,
            "Uvaga! Za period (.+) " +
                    "za rakhunkom (.+) " +
                    "zaborgovanist skladae (.+). " +
                    "Obovyazkovyi platizh (.+) " +
                    "do (.+).", Arrays.<Placeholder>asList(Placeholder.of(none), Placeholder.of(none),
            Placeholder.of(none), Placeholder.of(none), Placeholder.of(none)
    ));
    private static final Template otp_promo_dep = Template.flatTemplate(EventType.promo,
            "Vidkriite depozyt do 20% richnykh v OTP Bank! " +
                    "Shchiro bazhaiemo dostatku ta protsvitannia! " +
                    "Det.: 0800300050", Collections.emptyList());
    private static final Template otp_promo_cap = Template.flatTemplate(EventType.promo,
            "22% на 3 місяці та 23\\%\\+ на рік. " +
                    "Тільки до 31 жовтня! Вклад Подвійний від OTP Bank та OTP Capital. " +
                    "0800505125, otpbank.com.ua", Collections.emptyList());
    private static final Template otp_promo_mc = Template.flatTemplate(EventType.promo,
            "Rozrahovuytes kartkoyu MasterCard Premium Gold Cup vid OTP Bank ta vigravayte omriyanu podorozh. " +
                    "Reestratsiya v Aktsiyi - sms zi slovom Aktsiya na nomer 6101. " +
                    "Detali: 0 800 505 125. Vartist vidpravku sms - zgidno z tarifami vashogo operatora", Collections.emptyList());
    private static final Template otp_promo_tour = Template.flatTemplate(EventType.promo,
            "Turistichniy sertifIkat na 20 000 grn chekae na Vas. " +
                    "Rozrahovuytes рremialnoyu kartkoyu MasterCard World vid OTP Bank ta vigravayte. " +
                    "Bilshe rozrahunkiv, bilshe shansiv!Reestratsiya v Aktsiyi - " +
                    "sms zi slovom Promo na nomer 6101. Detali:0800505125. " +
                    "Vartist vidpravku sms - zgidno z tarifami vashogo operatora", Collections.emptyList());
    private static final Template otp_lounge = Template.flatTemplate(EventType.promo,
            "Uvaha\\!Dodatkovi perevahy z kartkoyu Premium World vid OTP Bank." +
                    "Vidteper dostup do Launge Zony aeroportu Boryspil dlya Vas bezkoshtovnyy\\!" +
                    "Detali\\:0800 505 125 ", Collections.emptyList());
    private static final Template otp_promo_hot = Template.flatTemplate(EventType.promo,
            "Гаряча пропозиція від OTP! " +
                    "ОТП Інвест\\+: більше 35% за рік. " +
                    "Від 50 000 грн. 0800505125, otpсapital.com.ua",
            Collections.emptyList());
    private static final Template otp_info = Template.flatTemplate(EventType.promo,
            "Shanovnii kliente, kredytna liniia do vashogo rakhunka 26253455156239UAH - " +
                    "vidkryta v rozmiri 10000 UAH. OTP Bank.", Collections.emptyList());
    //

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
                        otp_charge, otp_charge2/*, otp_charge_currency*/, otp_withdrawal, otp_withdrawal2, otp_cashier_withdrawal,
                        otp_cancel2, otp_cancel, otp_cancel_wo_braces, //otp_replenish_wo_balance,
                        otp_replenish, otp_deposit, otp_deposit_withdraw, otp_credit_reminder,
                        otp_promo_dep, otp_promo_cap, otp_promo_mc, otp_promo_tour, otp_lounge, otp_promo_hot, otp_info
                )).maxErrors(5).execute();

        Map<EventType, List<Event>> types =
                bus.events().stream().collect(Collectors.groupingBy(Event::type));

        // -------------------------------------------------------------------------------------------------------------
        List<Event> charged = types.get(EventType.charge);
        List<Event> replenished = types.get(EventType.replenishment);
        List<Event> deposits = types.get(EventType.deposit);
        List<Event> withdrawals = types.get(EventType.depositWithdraw);
        List<Event> failed = types.get(EventType.failed);
        List<Event> promo = types.get(EventType.promo);

        System.out.println("cards charges:");
        printAccountsStat(charged);
        System.out.println("accounts replenishment:");
        printAccountsStat(replenished);
        System.out.println("deposits replenishment:");
        printAccountsStat(deposits);
        System.out.println("deposits withdrawals:");
        printAccountsStat(withdrawals);

        //System.out.println("currencies: " + charged.stream().map(Event::currency).collect(Collectors.toSet()));

        // -------------------------------------------------------------------------------------------------------------
        assertThat(charged.size(), is(1108));
        assertThat(replenished.size(), is(110));
        assertThat(deposits.size(), is(59));
        assertThat(withdrawals.size(), is(62));
        assertThat(promo.size(), is(18));
        assertThat(failed.size(), is(239));

        System.out.println(failed.isEmpty() ? "\nGREAT JOB!" : "\nlet's check that 5/" + failed.size() + ":");
        failed.stream().limit(5).forEach(event -> System.out.println("\t" + event.data()));
    }

    private void printAccountsStat(List<Event> events) {
        if (events != null)
            events.stream().collect(Collectors.groupingBy(Event::payer, Collectors.counting()))
                    .forEach((eventType, count) -> System.out.println(String.format("\t%s, %d", eventType, count)));
    }
}
