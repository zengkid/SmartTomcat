package com.poratu.idea.plugins.tomcat.utils;

import com.intellij.execution.Location;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;
import com.poratu.idea.plugins.tomcat.setting.TomcatServerManagerState;
import com.poratu.idea.plugins.tomcat.setting.TomcatServersConfigurable;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public final class PluginUtils {
    private static final int MIN_PORT_VALUE = 0;
    private static final int MAX_PORT_VALUE = 65535;

    private PluginUtils() {
    }

    /**
     * Generate a sequent name based on the existing names
     *
     * @param existingNames existing names, e.g. ["tomcat 7", "tomcat 8", "tomcat 9"]
     * @param preferredName preferred name, e.g. "tomcat 8"
     * @return sequent name, e.g. "tomcat 8 (2)"
     */
    public static String generateSequentName(List<String> existingNames, String preferredName) {
        int maxSequent = 0;
        for (String existingName : existingNames) {
            Pattern pattern = Pattern.compile("^" + StringUtil.escapeToRegexp(preferredName) + "(?:\\s\\((\\d+)\\))?$");
            Matcher matcher = pattern.matcher(existingName);
            if (matcher.matches()) {
                String seq = matcher.group(1);
                if (seq == null) {
                    // No sequent implies that the sequent is 1
                    maxSequent = 1;
                } else {
                    maxSequent = Math.max(maxSequent, Integer.parseInt(seq));
                }
            }
        }

        return maxSequent == 0 ? preferredName : preferredName + " (" + (maxSequent + 1) + ")";
    }

    public static void chooseTomcat(Consumer<TomcatInfo> callback) {
        chooseTomcat(null, callback);
    }

    public static void chooseTomcat(UnaryOperator<String> nameGenerator, Consumer<TomcatInfo> callback) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory
                .createSingleFolderDescriptor()
                .withTitle("Select Tomcat Server")
                .withDescription("Select the directory of the Tomcat Server");

        FileChooser.chooseFile(descriptor, null, null, file -> TomcatServerManagerState
                .createTomcatInfo(file.getPath(), nameGenerator)
                .ifPresent(callback));
    }

    public static Path getWorkingPath(TomcatRunConfiguration configuration) {
        if(!StringUtils.isBlank(configuration.getCatalinaBase())) {
            /* CATALINA_BASE override from intellij run configuration */
            return Paths.get(configuration.getCatalinaBase());
        }
        String userHome = System.getProperty("user.home");
        Project project = configuration.getProject();
        Module module = configuration.getModule();

        if (module == null) {
            return null;
        }

        return Paths.get(userHome, ".SmartTomcat", project.getName(), module.getName());
    }

    public static Path getTomcatLogsDirPath(TomcatRunConfiguration configuration) {
        Path workingDir = getWorkingPath(configuration);
        if (workingDir != null) {
            return workingDir.resolve("logs");
        }
        return null;
    }

    @SuppressWarnings("HttpUrlsUsage")
    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ignored) {
            // Some Java implementations do not support these features
        }

        dbf.setExpandEntityReferences(false);

        return dbf.newDocumentBuilder();
    }

    @SuppressWarnings("HttpUrlsUsage")
    public static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();

        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = factory.newTransformer();
        try {
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (IllegalArgumentException ignored) {
            // ignore
        }
        return transformer;
    }

    public static void openTomcatConfiguration() {
        ShowSettingsUtil.getInstance().showSettingsDialog(null, TomcatServersConfigurable.class);
    }

    public static int parsePort(String text) throws ConfigurationException {
        if (StringUtil.isEmpty(text)) {
            throw new ConfigurationException("Port cannot be empty");
        }

        try {
            int port = Integer.parseInt(text);
            if (port < MIN_PORT_VALUE || port > MAX_PORT_VALUE) {
                throw new ConfigurationException("Port number must be between " + MIN_PORT_VALUE + " and " + MAX_PORT_VALUE);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Port number must be an integer");
        }
    }

    public static String extractContextPath(Module module) {
        String name = module.getName();
        String s = StringUtil.trimEnd(name, ".main");
        return ArrayUtil.getLastElement(s.split("\\."));
    }

    public static List<VirtualFile> findWebRoots(Module module) {
        List<VirtualFile> webRoots = new ArrayList<>();
        if (module == null) {
            return webRoots;
        }

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        ModuleFileIndex fileIndex = moduleRootManager.getFileIndex();
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots(false);
        List<VirtualFile> parentRoots = Stream.of(sourceRoots)
                .map(VirtualFile::getParent)
                .distinct()
                .collect(Collectors.toList());

        for (VirtualFile parentRoot : parentRoots) {
            fileIndex.iterateContentUnderDirectory(parentRoot, file -> {
                Path path = Paths.get(file.getPath(), "WEB-INF");
                if (Files.exists(path)) {
                    webRoots.add(file);
                }
                return true;
            }, file -> {
                if (file.isDirectory()) {
                    String path = file.getPath();
                    return webRoots.stream().noneMatch(root -> file.getPath().startsWith(root.getPath())) && !path.contains("node_modules");
                }
                return false;
            });
        }

        return webRoots;
    }

    public static List<VirtualFile> findWebRoots(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        List<VirtualFile> webRoots = new ArrayList<>();

        for (Module module : modules) {
            webRoots.addAll(findWebRoots(module));
        }

        return webRoots;
    }

    public static boolean isUnderTestSources(@Nullable Location<?> location) {
        if (location == null) {
            return false;
        }

        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return false;
        }

        return ProjectFileIndex.getInstance(location.getProject()).isInTestSourceContent(file);
    }

    /**
     * Get all modules except the module with name ends with ".test"
     */
    public static List<Module> getModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        return Arrays.stream(modules).filter(module -> !module.getName().endsWith(".test")).collect(Collectors.toList());
    }

    /**
     * Prefer the module with name ends with ".main" and has the least number of dots in the name,
     * e.g. `webapp.main` will be chosen over `webapp.library.main`
     * If no module with name ends with ".main", return the first module
     */
    public static @Nullable Module guessModule(Project project) {
        List<Module> modules = getModules(project);
        Module result = null;
        int dotCountInModuleName = Integer.MAX_VALUE;

        for (Module module : modules) {
            if (module.getName().endsWith(".main")) {
                int dotCount = StringUtil.countChars(module.getName(), '.');
                if (dotCount < dotCountInModuleName) {
                    result = module;
                    dotCountInModuleName = dotCount;
                }
            }
        }

        if (result != null) {
            return result;
        }

        return modules.stream().findFirst().orElse(null);
    }

    /**
     * Find the module containing the file
     */
    public static @Nullable Module findContainingModule(@Nullable String filePath, @NotNull Project project) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }

        VirtualFile virtualFile = VfsUtil.findFile(Paths.get(filePath), true);
        if (virtualFile == null) {
            return null;
        }

        return ModuleUtilCore.findModuleForFile(virtualFile, project);
    }
}
