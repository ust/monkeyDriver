import com.yrrlsv.fin.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DumpReaderTest {

    private static final Template otp_charge = new Template(EventType.charge,
            "(.+)\nSplata za tovar/poslugu.\n" +
                    "Kartka (.+). " +
                    "Suma:\n(.+) UAH. " +
                    "Misce:\n(.+).\n" +
                    "Zalyshok: (.+)");
    private static final Template otp_charge_uah = new Template(EventType.charge,
            "(.+)\nSplata za tovar/poslugu.\n" +
                    "Kartka (.+). " +
                    "Suma:\n(.+) UAH. " +
                    "Misce:\n(.+).\n" +
                    "Zalyshok: (.+) UAH");
    private static final Template otp_withdrawal = new Template(EventType.charge,
            "(.+)\nOtrymannya gotivky v ATM\n" +
                    "Kartka (.+). Suma:\n" +
                    "(.+) UAH. Misce:\n" +
                    "OTP BANK ATM UAH. \n" +
                    "Zalyshok: (.+) UAH");
    private static final Template otp_replenish_uah = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) UAH " +
                    "Zalyshok: (.+) UAH " +
                    "Platnyk: (.+)");
    private static final Template otp_replenish_usd = new Template(EventType.replenishment,
            "OTPdirekt:(.+): " +
                    "Popovnennya rahunku: (.+). " +
                    "Suma: (.+) USD " +
                    "Zalyshok: (.+) USD " +
                    "Platnyk: (.+)");
    private static final Template otp_deposit_usd = new Template(EventType.deposit,
            "OTPdirekt:(.+): " +
                    "Popovnennya depozytu z rahunku (.+). " +
                    "Suma: (.+) USD " +
                    "Balans rahunku: (.+) USD");
    private static final Template otp_promo_dep = new Template(EventType.promo,
            "Vidkriite depozyt do 20% richnykh v OTP Bank! " +
                    "Shchiro bazhaiemo dostatku ta protsvitannia! " +
                    "Det.: 0800300050");

    public static final TemplateProvider FAKE_TEMPLATE_PROVIDER = new TemplateProvider() {
        @Override
        public Template newTemplate() {
            throw new IllegalStateException("method shouldn't be invoked");
        }

        @Override
        public Template chooseTemplate(List<Template> templates) {
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
                        otp_charge, otp_charge_uah, otp_withdrawal,
                        otp_replenish_uah, otp_replenish_usd,
                        otp_deposit_usd,
                        otp_promo_dep
                )).execute();

        Map<EventType, List<Event>> result =
                bus.events().stream().collect(Collectors.groupingBy(Event::type));

        List<Event> charged = result.get(EventType.charge);
        List<Event> replenished = result.get(EventType.replenishment);
        List<Event> deposits = result.get(EventType.deposit);
        List<Event> failed = result.get(EventType.failed);
        List<Event> promo = result.get(EventType.promo);

        System.out.println("cards charges:");
        charged.stream().collect(Collectors.groupingBy(event -> event.data().get(Field.account), Collectors.counting()))
                .forEach((eventType, count) -> System.out.println(String.format("\t%s, %d", eventType, count)));
        System.out.println("accounts replenishment:");
        replenished.stream().collect(Collectors.groupingBy(event -> event.data().get(Field.account), Collectors.counting()))
                .forEach((eventType, count) -> System.out.println(String.format("\t%s, %d", eventType, count)));
        System.out.println("deposits replenishment:");
        deposits.stream().collect(Collectors.groupingBy(event -> event.data().get(Field.account), Collectors.counting()))
                .forEach((eventType, count) -> System.out.println(String.format("\t%s, %d", eventType, count)));

        System.out.println(failed.isEmpty() ? "\nGREAT JOB!" : "\nlet's check this one:\n\t" + failed.get(0).data());

        assertThat(charged.size(), is(213));
        assertThat(replenished.size(), is(103));
        assertThat(deposits.size(), is(45));
        assertThat(promo.size(), is(1));

        assertThat(failed.size(), is(1234));
    }
}
