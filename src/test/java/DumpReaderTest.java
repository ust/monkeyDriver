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
                    "Suma: (.+) . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+)UAH.",
            Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
                    Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance)
            ));
    private static final Template otp_charge_uah = new Template(EventType.charge,
            "(.+)\nSplata za tovar/poslugu.\n" +
                    "Kartka (.+). " +
                    "Suma:\n(.+) (.+). " +
                    "Misce:\n(.+).\n" +
                    "Zalyshok: (.+) UAH", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance)
    ));
    private static final Template otp_withdrawal = new Template(EventType.charge,
            "(.+)\nOtrymannya gotivky v ATM\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+) (.+). Misce:\n" +
                    "OTP BANK ATM UAH. \n" +
                    "Zalyshok: (.+) UAH", Arrays.<Placeholder>asList(Placeholder.of(date), Placeholder.of(account),
            Placeholder.of(amount, currency), Placeholder.of(shop), Placeholder.of(balance)
    ));
    private static final Template otp_withdrawal2 = Template.flatTemplate(EventType.charge,
            "OTPdirekt:(.+): Otrymannya gotivky. " +
                    "Kartka (.+). " +
                    "Suma: (.+)UAH . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+)UAH.", Arrays.asList(date, account, amount, shop, balance));
    private static final Template otp_cashier_withdrawal = Template.flatTemplate(EventType.charge,
            "OTPdirekt:(.+): " +
                    "Spysannya z rahunku: (.+). " +
                    "Suma: (.+) UAH " +
                    "Zalyshok: (.+) UAH " +
                    "Otrymuvach: (.+)", Arrays.asList(date, account, amount, balance, shop));
    private static final Template otp_cancel_uah = Template.flatTemplate(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): " +
                    "Povernennya tovaru. Kartka (.+). " +
                    "Suma: (.+)UAH \\((.+)UAH\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+)UAH.", Arrays.asList(date, account, amount, none, shop, balance));
    private static final Template otp_cancel_uah2 = Template.flatTemplate(EventType.replenishment,
            "OTPdirekt:VIDMINA: (.+): Splata za tovar/poslugu. " +
                    "Kartka (.+). " +
                    "Suma: (.+)UAH \\((.+)UAH\\). " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+)UAH.", Arrays.asList(date, account, amount, none, shop, balance));
    private static final Template otp_cancel2_uah = Template.flatTemplate(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Povernennya tovaru. " +
                    "Kartka (.+). " +
                    "Suma: (.+)UAH . " +
                    "Misce: (.+). " +
                    "Zalyshok: (.+)UAH.", Arrays.asList(date, account, amount, shop, balance));
    private static final Template otp_replenish_uah = Template.flatTemplate(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) UAH " +
                    "Zalyshok: (.+) UAH " +
                    "Platnyk: (.+)", Arrays.asList(date, account, amount, balance, shop));
    private static final Template otp_replenish_usd = Template.flatTemplate(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) USD " +
                    "Zalyshok: (.+) USD " +
                    "Platnyk: (.+)", Arrays.asList(date, account, amount, balance, shop));
    private static final Template otp_deposit_uah = Template.flatTemplate(EventType.deposit,
            "OTPdirekt:(.+): " +
                    "Popovnennya depozytu z rahunku (.+). " +
                    "Suma: (.+) UAH " +
                    "Balans rahunku: (.+) UAH", Arrays.asList(date, account, amount, balance));
    private static final Template otp_deposit_usd = Template.flatTemplate(EventType.deposit,
            "OTPdirekt:(.+): " +
                    "Popovnennya depozytu z rahunku (.+). " +
                    "Suma: (.+) USD " +
                    "Balans rahunku: (.+) USD", Arrays.asList(date, account, amount, balance));
    private static final Template otp_deposit_withdraw_uah = Template.flatTemplate(EventType.depositWithdraw,
            "OTPdirekt:(.+): " +
                    "Spysannya z depozytu na rahunok: (.+). " +
                    "Suma: (.+) UAH " +
                    "Balans rahunku: (.+) UAH", Arrays.asList(date, account, amount, balance));
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
            "Uvaha!Dodatkovi perevahy z kartkoyu Premium World vid OTP Bank." +
                    "Vidteper dostup do Launge Zony aeroportu Boryspil dlya Vas bezkoshtovnyy!" +
                    "Detali:0800 505 125 ", Collections.emptyList());
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
//            System.out.println("unrecognized message");
//            System.out.println(message);
//            throw new IllegalStateException("method shouldn't be invoked");
        }

        @Override
        public Event chooseTemplate(List<Event> templates) {
            return templates.get(0);
        }
    };


    private EventBus bus = new EventBus();

    @Test
    public void some() {
        new DumpReader(FAKE_TEMPLATE_PROVIDER,
                new AndroidBackupDataProvider(getClass().getResource("otp_charge_1.xml").getPath()),
                bus, Arrays.asList(otp_charge, otp_charge_uah)).execute();

        assertThat(bus.events().size(), is(1));
    }

    @Test
    public void chooseTemplate() {
        new DumpReader(FAKE_TEMPLATE_PROVIDER,
                new AndroidBackupDataProvider(getClass().getResource("otp_charge_2.xml").getPath()),
                bus, Arrays.asList(otp_charge, otp_charge_uah, otp_withdrawal)).execute();

        assertThat(bus.events().size(), is(2));
    }

    @Test
    public void otpOnly_20160111231728() {
        new DumpReader(FAKE_TEMPLATE_PROVIDER,
                new AndroidBackupDataProvider(getClass().getResource("otpOnly-20160111231728.xml").getPath()),
                bus,
                Arrays.asList(
                        otp_charge, otp_charge2, otp_charge_uah, otp_withdrawal, otp_withdrawal2, otp_cashier_withdrawal,
                        otp_cancel_uah, otp_cancel_uah2, otp_cancel2_uah, otp_replenish_uah, otp_replenish_usd,
                        otp_deposit_usd, otp_deposit_uah,
                        otp_deposit_withdraw_uah,
                        otp_promo_dep, otp_promo_cap, otp_promo_mc, otp_promo_tour, otp_lounge, otp_promo_hot, otp_info
                )).execute();

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

        // -------------------------------------------------------------------------------------------------------------
        List<Event> haveCurrency = bus.events().stream()
                .filter(event1 -> event1.data().containsKey(currency))
                .collect(Collectors.toList());
//        System.out.println("\nCURRENCY recognized: " + haveCurrency.size());
        Map<String, List<Event>> perCurrency = haveCurrency.stream()
                .collect(Collectors.groupingBy(event1 -> event1.data().get(currency)));
//        System.out.println("\nCURRENCY values: " + perCurrency.keySet());


        // -------------------------------------------------------------------------------------------------------------
        assertThat(charged.size(), is(1092));
        assertThat(replenished.size(), is(115));
        assertThat(deposits.size(), is(59));
        assertThat(withdrawals.size(), is(50));
        assertThat(promo.size(), is(6));
        assertThat(failed.size(), is(274));

        assertThat(haveCurrency.size(), is(213));

        System.out.println(failed.isEmpty() ? "\nGREAT JOB!" : "\nlet's check that 5/" + failed.size() + ":");
        failed.stream().limit(5).forEach(event -> System.out.println("\t" + event.data()));
    }

    private void printAccountsStat(List<Event> events) {
        events.stream().collect(Collectors.groupingBy(event -> event.data().get(account), Collectors.counting()))
                .forEach((eventType, count) -> System.out.println(String.format("\t%s, %d", eventType, count)));
    }
}
