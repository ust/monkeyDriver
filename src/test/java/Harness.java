import com.yrrlsv.fin.Message;
import com.yrrlsv.fin.Messages;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Harness {

    @Test public void loadFile() {
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

        Matcher matcher = Pattern.compile(template).matcher(sms);
        System.out.println("count: " + matcher.groupCount());
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println(matcher.group(i));
            }
        }
    }

    @Test
    public void explained() {
        String text = "Thanks, this is your value : 100. And this is your account number : 219AD098";
        Pattern pattern = Pattern
                .compile("Thanks, this is your value : (.+). And this is your account number : (.+)");
        Matcher matcher = pattern.matcher(text);
        System.out.println("count: " + matcher.groupCount());
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println(matcher.group(i));
            }
        }
//        String outputText = "[value] = " + matcher.group(1)
//                + " | [accountNumber] = " + matcher.group(2);
//        System.out.println(outputText);
    }
}
