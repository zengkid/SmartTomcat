package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfigurationType;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TomcatRunConfigurationProducer extends LazyRunConfigurationProducer<TomcatRunConfiguration> {
    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(TomcatRunConfigurationType.class);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        Module module = context.getModule();
        if (module == null) {
            return false;
        }

        // Skip if it contains a main class, to avoid conflict with the default Application run configuration
        PsiClass psiClass = ApplicationConfigurationType.getMainClass(context.getPsiLocation());
        if (psiClass != null) {
            return false;
        }

        List<VirtualFile> webRoots = findWebRoots(context.getLocation());
        if (webRoots.isEmpty()) {
            return false;
        }

        List<TomcatInfo> tomcatInfos = TomcatServerManagerState.getInstance().getTomcatInfos();
        if (!tomcatInfos.isEmpty()) {
            configuration.setTomcatInfo(tomcatInfos.get(0));
        }
        String contextPath = PluginUtils.extractContextPath(module);
        configuration.setName("Tomcat: " + contextPath);
        configuration.setDocBase(webRoots.get(0).getPath());
        configuration.setContextPath("/" + contextPath);

        return true;
    }

    @Override
    public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context) {
        List<VirtualFile> webRoots = findWebRoots(context.getLocation());
        return webRoots.stream().anyMatch(webRoot -> webRoot.getPath().equals(configuration.getDocBase()));
    }

    private List<VirtualFile> findWebRoots(@Nullable Location<?> location) {
        if (location == null) {
            return ContainerUtil.emptyList();
        }

        boolean isTestFile = PluginUtils.isUnderTestSources(location);
        if (isTestFile) {
            return ContainerUtil.emptyList();
        }

        return PluginUtils.findWebRoots(location.getModule());
    }

}
