package com.poratu.idea.plugins.downlaod;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;


/**
 * Author : zengkid
 * Date   : 2017-03-07
 * Time   : 03:03
 */
public class DownloadFile
{
    public static void main(String[] args) {
        String url = "http://maven.aliyun.com/nexus/service/local/repositories/central/content/ch/qos/logback/logback-core/1.2.1/logback-core-1.2.1.jar";
        try (InputStream inputStream = new URL(url).openStream()) {

            Path target = Paths.get("/temp/1.jar");
            if (!target.getParent().toFile().exists()) {
                target.getParent().toFile().mkdirs();
            }
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
