package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    private TomcatInfo current;

    public TomcatInfo getCurrent() {
        return current;
    }

    public void setCurrent(TomcatInfo current) {
        this.current = current;
    }

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

//    @Nullable
//    public static TomcatInfoConfigs getInstance(Project project) {
//        TomcatInfoConfigs sfec = ServiceManager.getService(project, TomcatInfoConfigs.class);
//        return sfec;
//    }

    public int getMaxVersion(TomcatInfo tomcatInfo) {
        Optional<TomcatInfo> maxTomcatInfo = tomcatInfos.stream().filter(it ->
                it.getName().equals(tomcatInfo.getName()) && it.getNumber() == tomcatInfo.getNumber()).max(Comparator.comparingInt(TomcatInfo::getNumber));
        int max = 0;
        if (maxTomcatInfo.isPresent()) {
            max = maxTomcatInfo.get().getNumber();
        }
        return max;
    }
}
