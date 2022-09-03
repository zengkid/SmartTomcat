package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TomcatRunnerSettingsEditor extends SettingsEditor<TomcatRunConfiguration> {

    private final TomcatRunnerSettingsForm form;

    public TomcatRunnerSettingsEditor(Project project) {
        form = new TomcatRunnerSettingsForm(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull TomcatRunConfiguration configuration) {
        form.resetFrom(configuration);
    }

    @Override
    protected void applyEditorTo(@NotNull TomcatRunConfiguration configuration) throws ConfigurationException {
        form.applyTo(configuration);
    }

    @Override
   protected @NotNull JComponent createEditor() {
        return form.getMainPanel();
    }

}
