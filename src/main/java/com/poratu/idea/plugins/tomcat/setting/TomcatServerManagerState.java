package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import com.poratu.idea.plugins.tomcat.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 15:20
 */

@State(name = "ServerConfiguration", storages = @Storage("smart.tomcat.xml"))
public class TomcatServerManagerState implements PersistentStateComponent<TomcatServerManagerState> {

    @XCollection(elementTypes = TomcatInfo.class)
    private final List<TomcatInfo> tomcatInfos = new ArrayList<>();

    public static TomcatServerManagerState getInstance() {
        return ApplicationManager.getApplication().getService(TomcatServerManagerState.class);
    }

    @NotNull
    public List<TomcatInfo> getTomcatInfos() {
        return tomcatInfos;
    }

    @Nullable
    @Override
    public TomcatServerManagerState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TomcatServerManagerState tomcatSettingsState) {
        XmlSerializerUtil.copyBean(tomcatSettingsState, this);
    }

    public static Optional<TomcatInfo> createTomcatInfo(String tomcatHome) {
        return createTomcatInfo(tomcatHome, TomcatServerManagerState::generateTomcatName);
    }

    public static Optional<TomcatInfo> createTomcatInfo(String tomcatHome, UnaryOperator<String> nameGenerator) {
        File jarFile = Paths.get(tomcatHome, "lib/catalina.jar").toFile();
        if (!jarFile.exists()) {
            Messages.showErrorDialog("Can not find catalina.jar in " + tomcatHome, "Error");
            return Optional.empty();
        }

        final TomcatInfo tomcatInfo = new TomcatInfo();
        tomcatInfo.setPath(tomcatHome);

        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry("org/apache/catalina/util/ServerInfo.properties");
            Properties p = new Properties();
            try (InputStream is = jar.getInputStream(entry)) {
                p.load(is);
            }
            String serverInfo = p.getProperty("server.info");
            String serverNumber = p.getProperty("server.number");
            String name = nameGenerator == null ? generateTomcatName(serverInfo) : nameGenerator.apply(serverInfo);
            tomcatInfo.setName(name);
            tomcatInfo.setVersion(serverNumber);
        } catch (IOException e) {
            Messages.showErrorDialog("Can not read server version in " + tomcatHome, "Error");
            return Optional.empty();
        }

        return Optional.of(tomcatInfo);
    }

    private static String generateTomcatName(String name) {
        List<TomcatInfo> existingServers = getInstance().getTomcatInfos();
        List<String> existingNames = existingServers.stream()
                .map(TomcatInfo::getName)
                .collect(Collectors.toList());

        return PluginUtils.generateSequentName(existingNames, name);
    }

}
