package com.yrrlsv.fin;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;

public class AndroidBackupDataProvider implements DataProvider {

    private Iterator<Message> iterator;

    public AndroidBackupDataProvider(String path) {
        Messages messages;
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(Messages.class, Message.class).createUnmarshaller();
            messages = (Messages) unmarshaller.unmarshal(new FileInputStream(path));
        } catch (JAXBException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        iterator = messages != null ? messages.list().iterator() : Collections.emptyIterator();
    }

    @Override
    public Message nextMessage() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
