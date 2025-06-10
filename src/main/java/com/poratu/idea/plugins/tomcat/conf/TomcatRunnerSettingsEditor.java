package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.poratu.idea.plugins.tomcat.ui.ServerConfigurationTab;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Settings editor that wraps ServerConfigurationTab
 * Provides compatibility with IntelliJ's SettingsEditor system
 * Works with EnhancedTomcatRunConfiguration for Phase 2 features
 */
public class TomcatRunnerSettingsEditor extends SettingsEditor<EnhancedTomcatRunConfiguration> {

    private final ServerConfigurationTab serverTab;

    public TomcatRunnerSettingsEditor(@NotNull Project project) {
        this.serverTab = new ServerConfigurationTab(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull EnhancedTomcatRunConfiguration configuration) {
        serverTab.resetFrom(configuration);
    }

    @Override
    protected void applyEditorTo(@NotNull EnhancedTomcatRunConfiguration configuration) throws ConfigurationException {
        serverTab.applyTo(configuration);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return serverTab;
    }
}