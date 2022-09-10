package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfigurationType;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

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
        List<VirtualFile> webRoots = PluginUtils.findWebRoots(module);

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
    public boolean isConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context) {
        Module module = context.getModule();
        List<VirtualFile> webRoots = PluginUtils.findWebRoots(module);

        return webRoots.stream().anyMatch(webRoot -> webRoot.getPath().equals(configuration.getDocBase()));
    }

}
