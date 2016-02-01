package com.yrrlsv.fin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "smses")
public class Messages {
    @XmlElement(name = "sms")
    private List<Message> messages;

    public List<Message> list() {
        return messages;
    }

    @Override
    public String toString() {
        return "Messages{" +
                "messages=" + messages +
                '}';
    }
}
