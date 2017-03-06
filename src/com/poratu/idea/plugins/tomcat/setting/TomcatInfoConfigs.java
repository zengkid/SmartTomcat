package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 15:20
 */

@State(
        name = "TomcatInfoConfigs",
        storages = {
                @Storage("TomcatInfoConfigs.xml")}
)
public class TomcatInfoConfigs implements PersistentStateComponent<TomcatInfoConfigs> {

    private List<TomcatInfo> tomcatInfos = new ArrayList<>();

    public List<TomcatInfo> getTomcatInfos() {
        return tomcatInfos;
    }

    public void setTomcatInfos(List<TomcatInfo> tomcatInfos) {
        this.tomcatInfos = tomcatInfos;
    }

    public void addTomcatInfo(TomcatInfo tomcatInfo) {
        this.tomcatInfos.add(tomcatInfo);
    }

    @Nullable
    @Override
    public TomcatInfoConfigs getState() {
        return this;
    }

    @Override
    public void loadState(TomcatInfoConfigs tomcatInfoConfigs) {
        XmlSerializerUtil.copyBean(tomcatInfoConfigs, this);

    }

    @Nullable
    public static TomcatInfoConfigs getInstance() {
        TomcatInfoConfigs sfec = ServiceManager.getService(TomcatInfoConfigs.class);
        return sfec;
    }

    @Nullable
    public static TomcatInfoConfigs getInstance(Project project) {
        TomcatInfoConfigs sfec = ServiceManager.getService(project, TomcatInfoConfigs.class);
        return sfec;
    }
}
