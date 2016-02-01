package com.yrrlsv.fin;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "sms")
public class Message {
    @XmlAttribute(name = "body")
    private String text;
    @XmlAttribute
    private Long date;

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", date=" + date +
                '}';
    }
}
