package com.poratu.idea.plugins.tomcat.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import com.poratu.idea.plugins.tomcat.setting.TomcatInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public abstract class PluginUtils {

//    public static String getJavaHome() {
//        Sdk sdk = getProjectJDK();
//        String javahome;
//        if (sdk == null) {
//            JdkBundle jdkBundle = JdkBundle.createBoot();
//            javahome = jdkBundle.getLocation().getAbsolutePath();
//        } else {
//            javahome = sdk.getHomePath();
//        }
//        return javahome;
//    }

    private static Sdk getProjectJDK() {
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        return allJdks.length == 0 ? null : allJdks[0];
    }

    public static TomcatInfo getTomcatInfo(String tomcatHome) {
        final TomcatInfo tomcatInfo = new TomcatInfo();
        tomcatInfo.setPath(tomcatHome);

        try {
            File jarFile = new File(tomcatHome, "lib/catalina.jar");
            if (!jarFile.exists()) {
                throw new RuntimeException("tomcat path [" + tomcatHome + "] is incorrect!");
            }
            try (JarFile jar = new JarFile(jarFile)) {
                ZipEntry entry = jar.getEntry("org/apache/catalina/util/ServerInfo.properties");

                Properties p = new Properties();
                try (InputStream is = jar.getInputStream(entry)) {
                    p.load(is);
                }

                String serverInfo = p.getProperty("server.info");
                String serverNumber = p.getProperty("server.number");
                tomcatInfo.setName(serverInfo);
                tomcatInfo.setVersion(serverNumber);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tomcatInfo;
    }

    public static Path getWorkPath(TomcatRunConfiguration configuration) {

        String userHome = System.getProperty("user.home");
        Project project = configuration.getProject();
        Module module = configuration.getModule();

        return Paths.get(userHome, ".SmartTomcat", project.getName(), module.getName());
    }
}
