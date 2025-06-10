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
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfigurationType;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Enhanced DevTomcat Run Configuration Producer - Phase 2
 *
 * Provides intelligent run configuration creation with:
 * - Smart context path detection
 * - Enhanced web root discovery
 * - Development mode optimization
 * - Multiple deployment artifact support
 */
public class TomcatRunConfigurationProducer extends LazyRunConfigurationProducer<EnhancedTomcatRunConfiguration> {

    private static final String DEVTOMCAT_REGISTRY_KEY = "devTomcat.disableRunConfigurationProducer";
    private static final String DEFAULT_CONTEXT_PREFIX = "Tomcat: ";

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        TomcatRunConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(TomcatRunConfigurationType.class);
        return configurationType.getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull EnhancedTomcatRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        // Check if DevTomcat configuration producer is disabled
        if (Registry.is(DEVTOMCAT_REGISTRY_KEY)) {
            return false;
        }

        Module module = context.getModule();
        if (module == null) {
            return false;
        }

        // Skip if it contains a main class to avoid conflict with Application run configuration
        PsiClass psiClass = ApplicationConfigurationType.getMainClass(context.getPsiLocation());
        if (psiClass != null) {
            return false;
        }

        // Enhanced web root discovery
        List<VirtualFile> webRoots = findWebRoots(context.getLocation());
        if (webRoots.isEmpty()) {
            return false;
        }

        // Setup Tomcat server configuration
        if (!setupTomcatServer(configuration)) {
            return false;
        }

        // Enhanced context path and configuration setup
        setupEnhancedConfiguration(configuration, module, webRoots);

        return true;
    }

    @Override
    public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
        // Phase 2: DevTomcat configurations are preferred for web modules
        if (self.getConfiguration() instanceof EnhancedTomcatRunConfiguration) {
            return isWebModuleContext(self.getSourceElement());
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull EnhancedTomcatRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        if (Registry.is(DEVTOMCAT_REGISTRY_KEY)) {
            return false;
        }

        List<VirtualFile> webRoots = findWebRoots(context.getLocation());
        return webRoots.stream().anyMatch(webRoot ->
                webRoot.getPath().equals(configuration.getDocBase()));
    }

    /**
     * Enhanced web roots discovery with Spring Boot and Maven/Gradle support
     */
    private List<VirtualFile> findWebRoots(@Nullable Location<?> location) {
        if (location == null) {
            return ContainerUtil.emptyList();
        }

        // Skip test files
        boolean isTestFile = PluginUtils.isUnderTestSources(location);
        if (isTestFile) {
            return ContainerUtil.emptyList();
        }

        Module module = location.getModule();
        if (module == null) {
            return ContainerUtil.emptyList();
        }

        // Enhanced web root discovery for different project types
        List<VirtualFile> webRoots = PluginUtils.findWebRoots(module);

        // Phase 2: Add Spring Boot static resources detection
        if (webRoots.isEmpty()) {
            webRoots = findSpringBootWebRoots(module);
        }

        // Phase 2: Add Maven/Gradle webapp detection
        if (webRoots.isEmpty()) {
            webRoots = findMavenGradleWebRoots(module);
        }

        return webRoots;
    }

    /**
     * Setup Tomcat server configuration with intelligent defaults
     */
    private boolean setupTomcatServer(@NotNull EnhancedTomcatRunConfiguration configuration) {
        List<TomcatInfo> tomcatInfos = TomcatServerManagerState.getInstance().getTomcatInfos();

        if (tomcatInfos.isEmpty()) {
            // Phase 2: Could show notification to configure Tomcat server
            return false;
        }

        // Use the first available Tomcat server
        configuration.setTomcatInfo(tomcatInfos.get(0));
        return true;
    }

    /**
     * Enhanced configuration setup with intelligent naming and paths
     */
    private void setupEnhancedConfiguration(@NotNull EnhancedTomcatRunConfiguration configuration,
                                            @NotNull Module module,
                                            @NotNull List<VirtualFile> webRoots) {
        // Enhanced context path extraction
        String contextPath = extractEnhancedContextPath(module);

        // Phase 2: Enhanced naming convention
        String configName = createConfigurationName(contextPath, module);
        configuration.setName(configName);

        // Set document base to the first web root
        configuration.setDocBase(webRoots.get(0).getPath());

        // Enhanced context path with validation
        String normalizedContextPath = normalizeContextPath(contextPath);
        configuration.setContextPath(normalizedContextPath);

        // Phase 2: Enable development mode optimizations by default
        enableDevelopmentModeDefaults(configuration);
    }

    /**
     * Extract enhanced context path with fallbacks for different project types
     */
    private String extractEnhancedContextPath(@NotNull Module module) {
        // Try original plugin utils first
        String contextPath = PluginUtils.extractContextPath(module);

        if (contextPath == null || contextPath.trim().isEmpty()) {
            // Fallback to module name
            contextPath = module.getName();

            // Clean up common suffixes
            contextPath = contextPath.replaceAll("[-_](web|webapp|app|main)$", "");
        }

        return contextPath;
    }

    /**
     * Create intelligent configuration name
     */
    private String createConfigurationName(@NotNull String contextPath, @NotNull Module module) {
        if (isSpringBootModule(module)) {
            return DEFAULT_CONTEXT_PREFIX + contextPath + " (Spring Boot)";
        } else if (isMavenModule(module)) {
            return DEFAULT_CONTEXT_PREFIX + contextPath + " (Maven)";
        } else if (isGradleModule(module)) {
            return DEFAULT_CONTEXT_PREFIX + contextPath + " (Gradle)";
        } else {
            return DEFAULT_CONTEXT_PREFIX + contextPath;
        }
    }

    /**
     * Normalize context path to ensure it starts with /
     */
    private String normalizeContextPath(@NotNull String contextPath) {
        if (contextPath.trim().isEmpty()) {
            return "/";
        }

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        return contextPath;
    }

    /**
     * Enable development mode defaults for better development experience
     */
    private void enableDevelopmentModeDefaults(@NotNull EnhancedTomcatRunConfiguration configuration) {
        // Phase 2: These would be implemented when we add the configuration options
        // For now, this sets up the foundation for development mode features

        // Future: Enable hot deployment by default
        // Future: Enable deployment logging by default
        // Future: Set optimal JVM parameters for development
    }

    /**
     * Check if this is a web module context
     */
    private boolean isWebModuleContext(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }

        // Check if we're in a web-related file
        String fileName = element.getContainingFile().getName().toLowerCase();
        return fileName.endsWith(".jsp") ||
                fileName.endsWith(".jspx") ||
                fileName.endsWith(".html") ||
                fileName.endsWith(".xhtml") ||
                fileName.contains("web.xml");
    }

    /**
     * Find Spring Boot static resources directories
     */
    private List<VirtualFile> findSpringBootWebRoots(@NotNull Module module) {
        // Phase 2: Spring Boot detection logic
        // Look for src/main/resources/static, src/main/resources/public, etc.
        return ContainerUtil.emptyList(); // Placeholder for now
    }

    /**
     * Find Maven/Gradle webapp directories
     */
    private List<VirtualFile> findMavenGradleWebRoots(@NotNull Module module) {
        // Phase 2: Maven/Gradle webapp detection
        // Look for src/main/webapp, web/, etc.
        return ContainerUtil.emptyList(); // Placeholder for now
    }

    /**
     * Check if module is a Spring Boot module
     */
    private boolean isSpringBootModule(@NotNull Module module) {
        // Phase 2: Spring Boot detection logic
        // Check for Spring Boot dependencies, annotations, etc.
        return false; // Placeholder for now
    }

    /**
     * Check if module is a Maven module
     */
    private boolean isMavenModule(@NotNull Module module) {
        // Phase 2: Maven detection logic
        // Check for pom.xml
        return false; // Placeholder for now
    }

    /**
     * Check if module is a Gradle module
     */
    private boolean isGradleModule(@NotNull Module module) {
        // Phase 2: Gradle detection logic
        // Check for build.gradle or build.gradle.kts
        return false; // Placeholder for now
    }
}