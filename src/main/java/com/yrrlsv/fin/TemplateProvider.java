package com.yrrlsv.fin;

import java.util.List;

public interface TemplateProvider {
    Template newTemplate();

    Template chooseTemplate(List<Template> templates);
}
