package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
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

@State(name = "ServerConfiguration", storages = @Storage("smart.tomcat.xml"))
public class TomcatInfoConfigs implements PersistentStateComponent<TomcatInfoConfigs> {

    @XCollection(elementTypes = TomcatInfo.class)
    private final List<TomcatInfo> tomcatInfos = new ArrayList<>();

    @Nullable
    public static TomcatInfoConfigs getInstance() {
        TomcatInfoConfigs sfec = ServiceManager.getService(TomcatInfoConfigs.class);
        return sfec;
    }

    public List<TomcatInfo> getTomcatInfos() {
        return tomcatInfos;
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
