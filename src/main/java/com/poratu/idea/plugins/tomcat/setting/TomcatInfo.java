package com.poratu.idea.plugins.tomcat.setting;

import java.io.Serializable;
import java.util.Objects;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 16:17
 */
public class TomcatInfo implements Serializable {
    private String name;
    private String version;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TomcatInfo that = (TomcatInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, path);
    }

    @Override
    public String toString() {
        return name;
    }
}
