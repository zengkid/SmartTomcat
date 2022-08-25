package com.poratu.idea.plugins.tomcat.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public final class PluginUtils {

    private PluginUtils() {
    }

    /**
     * Generate a sequent name based on the existing names
     * @param existingNames existing names, e.g. ["tomcat 7", "tomcat 8", "tomcat 9"]
     * @param preferredName preferred name, e.g. "tomcat 8"
     * @return sequent name, e.g. "tomcat 8 (2)"
     */
    @NotNull
    public static String generateSequentName(@NotNull List<String> existingNames, @NotNull String preferredName) {
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

    @Nullable
    public static Path getWorkingPath(TomcatRunConfiguration configuration) {

        String userHome = System.getProperty("user.home");
        Project project = configuration.getProject();
        Module module = configuration.getModule();

        if (module == null) {
            return null;
        }

        return Paths.get(userHome, ".SmartTomcat", project.getName(), module.getName());
    }

    @Nullable
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
        }

        dbf.setExpandEntityReferences(false);

        return dbf.newDocumentBuilder();
    }

    public static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();

        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        return factory.newTransformer();
    }
}
