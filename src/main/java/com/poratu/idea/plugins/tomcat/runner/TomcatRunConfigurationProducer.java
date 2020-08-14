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
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfoConfigs;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TomcatRunConfigurationProducer extends LazyRunConfigurationProducer<TomcatRunConfiguration> {


    @Override
    protected boolean setupConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {

        boolean result = isConfigurationFromContext(configuration, context);

        if (result) {
            List<TomcatInfo> tomcatInfos = TomcatInfoConfigs.getInstance().getTomcatInfos();
            if (tomcatInfos != null && tomcatInfos.size() > 0) {
                TomcatInfo tomcatInfo = tomcatInfos.get(0);
                configuration.setTomcatInfo(tomcatInfo);
            } else  {
                throw new RuntimeException("Not found any Tomcat Server, please add Tomcat Server first.");
            }

            VirtualFile virtualFile = context.getLocation().getVirtualFile();
            Module module = context.getModule();
            configuration.setName(module.getName());
            configuration.setDocBase(virtualFile.getCanonicalPath());
            configuration.setContextPath("/" + module.getName());
            configuration.setModuleName(module.getName());

            final RunnerAndConfigurationSettings settings =
                    RunManager.getInstance(context.getProject()).createConfiguration(configuration, getConfigurationFactory());
            settings.setName(module.getName() + " in SmartTomcat");
        }
        return result;
    }


    @Override
    public boolean isConfigurationFromContext(@NotNull TomcatRunConfiguration configuration, @NotNull ConfigurationContext context) {
        boolean result = false;

        VirtualFile vf = context.getLocation().getVirtualFile();
        if (vf != null && vf.isDirectory()) {
            Module module = context.getModule();
            if (module != null) {
                Optional<VirtualFile> webModule = getWebModule(module);
                boolean isWebModule = webModule.isPresent();
                if (isWebModule) {
                    VirtualFile virtualFile = webModule.get();
                    if (vf.getCanonicalPath().equals(virtualFile.getCanonicalPath())) {
                        result = true;
                    }
                }
            }
        }
        return result;
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
