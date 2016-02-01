package com.yrrlsv.fin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DumpReader {


    public static void main(String[] args) {
        new DumpReader(new ConsoleTemplateProvider(),
                new AndroidBackupDataProvider(args[0]),
                new EventBus(), null)
                .execute();
    }

    private TemplateProvider templateProvider;
    private DataProvider dataProvider;
    private CoreService coreService;
    private EventBus eventBus;

    public DumpReader(TemplateProvider templateProvider, DataProvider dataProvider, EventBus eventBus, Collection<Template> templates) {
        this.templateProvider = templateProvider;
        this.dataProvider = dataProvider;
        this.eventBus = eventBus;
        this.coreService = new CoreService();
        if (templates != null) {
            for (Template template : templates) {
                this.coreService.addTemplate(template);
            }
        }
    }

    public void execute() {
        List<String> failed = new ArrayList<>();
        for (String message = dataProvider.nextMessage(); message != null; message = dataProvider.nextMessage()) {

            List<Template> templates = coreService.seekTemplate(message);
            Template template = null;
            if (templates.isEmpty()) {
                //template = templateProvider.newTemplate();
                //coreService.addTemplate(template);
                failed.add(message);
            } else if (templates.size() == 1) {
                template = templates.get(0);
            } else {
                template = templateProvider.chooseTemplate(templates);
            }

            eventBus.fire(template != null
                    ? coreService.createEvent(template, message)
                    : new Event(EventType.failed, Collections.singletonMap(Field.source, message)));
        }
    }

}
