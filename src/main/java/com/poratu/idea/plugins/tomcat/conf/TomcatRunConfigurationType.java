package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:11 PM
 */
public class TomcatRunConfigurationType implements ConfigurationType {

    private static final Icon TOMCAT_ICON = IconLoader.getIcon("/icon/tomcat.svg");

    @Override
    public String getDisplayName() {
        return "Smart Tomcat";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Smart Tomcat";
    }

    @Override
    public Icon getIcon() {
        return TOMCAT_ICON;
    }

    @NotNull
    @Override
    public String getId() {
        return "com.poratu.idea.plugins.tomcat";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new TomcatConfigurationFactory(this)};
    }
}
