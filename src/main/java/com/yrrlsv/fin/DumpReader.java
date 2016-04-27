package com.yrrlsv.fin;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private int errorsThreshold = Integer.MAX_VALUE;

    public DumpReader(TemplateProvider templateProvider, DataProvider dataProvider, EventBus eventBus, Collection<Template> templates) {
        this.templateProvider = templateProvider;
        this.dataProvider = dataProvider;
        this.eventBus = eventBus;
        this.coreService = new CoreService(new HashSet<>(templates));
    }

    public void execute() {
        int failed = 0;
        for (String message = dataProvider.nextMessage(); message != null; message = dataProvider.nextMessage()) {

            List<Event> results;
            try {
                results = coreService.parse(message);
            } catch (Exception e) {
                System.out.println("failed parsing message: " + message);
                e.printStackTrace();
                if (++failed < errorsThreshold) continue;
                else break;
            }
            Event event = null;
            if (results.isEmpty()) {
                Optional<Template> templateOptional = templateProvider.newTemplate(message);
                if (templateOptional.isPresent()) {
                    coreService.addTemplate(templateOptional.get());
                    event = coreService.newEvent(templateOptional.get(), message).get();
                }
            } else if (results.size() == 1) {
                event = results.get(0);
            } else {
                event = templateProvider.chooseTemplate(results);
            }

            eventBus.fire(event != null ? event : Event.failed(message));
        }
    }

    public DumpReader maxErrors(int errorsThreshold) {
        this.errorsThreshold = errorsThreshold;
        return this;
    }
}
