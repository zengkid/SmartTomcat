package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfigurationType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class TomcatRunConfigurationProducer extends LazyRunConfigurationProducer<TomcatRunConfiguration> {


    @Override
    protected boolean setupConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {

        Module module = context.getModule();
        Optional<VirtualFile> webModule = getWebModule(module);

        boolean isWebModule = webModule.isPresent();
        if (isWebModule) {

            VirtualFile virtualFile = webModule.get();
            configuration.setName(module.getName());
            configuration.setDocBase(virtualFile.getCanonicalPath());
            configuration.setContextPath("/" + module.getName());
            configuration.setModuleName(module.getName());

            ConfigurationFactory configurationFactory = getConfigurationFactory();
            final RunnerAndConfigurationSettings settings =
                    RunManager.getInstance(context.getProject()).createConfiguration(configuration, configurationFactory);

            settings.setName(configuration.getName() + " in " + module.getName());
        }

        return isWebModule;
    }


    @Override
    public boolean isConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context) {

        Module module = context.getModule();
        Optional<VirtualFile> webModule = getWebModule(module);


        boolean present = webModule.isPresent();
        return present;
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        TomcatRunConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(TomcatRunConfigurationType.class);
        return configurationType.getConfigurationFactories()[0];
    }

    private Optional<VirtualFile> getWebModule(@NotNull Module module) {
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(false);

        Optional<VirtualFile> webmodule = Stream.of(sourceRoots).map(VirtualFile::getParent).distinct().flatMap(f ->
                Stream.of(f.getChildren()).filter(c -> {
                    Path path = Paths.get(c.getCanonicalPath(), "WEB-INF");
                    return path.toFile().exists();
                })).distinct().findFirst();
        return webmodule;
    }


}
