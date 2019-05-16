package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:12 PM
 */
public class TomcatConfigurationFactory extends ConfigurationFactory {
    private static final String FACTORY_NAME = "SMART_TOMCAT_FACTORY";

    protected TomcatConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {

        return new TomcatRunConfiguration(project, this, "SmartTomcat");
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }
}
