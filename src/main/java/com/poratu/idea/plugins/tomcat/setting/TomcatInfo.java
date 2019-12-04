package com.poratu.idea.plugins.tomcat.setting;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 16:17
 */
public class TomcatInfo {
    private String name;
    private String version;
    private String path;
    private int number;

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

        if (number != that.number) return false;
        return name != null && name.equals(that.name) || name == null && that.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + number;
        return result;
    }

    @Override
    public String toString() {
        return name + (number > 0 ? "(" + number + ")" : "");
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
