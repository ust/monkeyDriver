package com.yrrlsv.fin;

import java.util.List;
import java.util.Optional;

public interface TemplateProvider {

    Optional<Template> newTemplate(String message);

    Event chooseTemplate(List<Event> templates);

}
