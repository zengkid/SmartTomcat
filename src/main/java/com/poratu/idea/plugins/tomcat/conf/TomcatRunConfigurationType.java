package com.poratu.idea.plugins.tomcat.conf;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Fixed TomcatRunConfigurationType - ONLY fixes infinite loop
 * Keeps all Phase 2 functionality intact
 */
public class TomcatRunConfigurationType implements ConfigurationType {

    private static final String ID = "com.poratu.idea.plugins.tomcat";
    private static final String DISPLAY_NAME = "Tomcat";
    private static final String DESCRIPTION = "Enhanced Tomcat server with professional deployment logging";

    // FIX: Cache the factory to prevent infinite recreation
    private volatile TomcatConfigurationFactory cachedFactory;

    @Override
    @NotNull
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    @NotNull
    public String getConfigurationTypeDescription() {
        return DESCRIPTION;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        try {
            return IconLoader.getIcon("/icon/tomcat.svg", TomcatRunConfigurationType.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    @Override
    @NotNull
    public ConfigurationFactory[] getConfigurationFactories() {
        // FIX: Only create factory once to prevent infinite loop
        if (cachedFactory == null) {
            synchronized (this) {
                if (cachedFactory == null) {
                    cachedFactory = new TomcatConfigurationFactory(this);
                    System.out.println("DevTomcat: Factory created and cached");
                }
            }
        }
        return new ConfigurationFactory[]{cachedFactory};
    }

    /**
     * Configuration Factory - Fixed to prevent infinite loop
     */
    public static class TomcatConfigurationFactory extends ConfigurationFactory {

        protected TomcatConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
            // Removed debug output that was causing spam
        }

        @Override
        @NotNull
        public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            System.out.println("DevTomcat: Creating TomcatRunConfiguration for: " + project.getName());

            try {
                EnhancedTomcatRunConfiguration config = new EnhancedTomcatRunConfiguration(project, this, "");
                System.out.println("DevTomcat: EnhancedTomcatRunConfiguration created successfully");
                return config;

            } catch (Exception e) {
                System.err.println("DevTomcat: ERROR creating configuration: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        @Override
        @NotNull
        public String getName() {
            return "Tomcat";
        }

        @Override
        @NotNull
        public String getId() {
            return ID;
        }

        @Override
        public boolean isApplicable(@NotNull Project project) {
            return true;
        }

        @Override
        public @NotNull RunConfiguration createConfiguration(@Nullable String name, @NotNull RunConfiguration template) {
            if (template instanceof EnhancedTomcatRunConfiguration) {
                return ((EnhancedTomcatRunConfiguration) template).clone();
            } else if (template instanceof EnhancedTomcatRunConfiguration) {
                return ((EnhancedTomcatRunConfiguration) template).clone();
            }
            return createTemplateConfiguration(template.getProject());
        }
    }
}