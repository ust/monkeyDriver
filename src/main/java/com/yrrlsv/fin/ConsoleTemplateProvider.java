package com.yrrlsv.fin;

import java.util.List;
import java.util.Optional;

public class ConsoleTemplateProvider implements TemplateProvider {
    @Override
    public Optional<Template> newTemplate(String message) {
        return null;
    }

    @Override
    public Event chooseTemplate(List<Event> templates) {
        return null;
    }
}
