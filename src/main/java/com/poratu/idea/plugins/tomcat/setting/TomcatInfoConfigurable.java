package com.poratu.idea.plugins.tomcat.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TomcatInfoConfigurable extends NamedConfigurable<TomcatInfo> {
    private final TomcatInfo tomcatInfo;
    private final TomcatInfoComponent tomcatInfoView;
    private String displayName;
    private final TomcatNameValidator<String> nameValidator;

    public TomcatInfoConfigurable(TomcatInfo tomcatInfo, Runnable treeUpdater, TomcatNameValidator<String> nameValidator) {
        super(true, treeUpdater);
        this.tomcatInfo = tomcatInfo;
        this.tomcatInfoView = new TomcatInfoComponent(tomcatInfo);
        this.displayName = tomcatInfo.getName();
        this.nameValidator = nameValidator;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public TomcatInfo getEditableObject() {
        return tomcatInfo;
    }

    @Override
    public String getBannerSlogan() {
        return null;
    }

    @Override
    public JComponent createOptionsPanel() {
        return tomcatInfoView.getMainPanel();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected void checkName(@NonNls @NotNull String name) throws ConfigurationException {
        super.checkName(name);
        if (name.equals(tomcatInfo.getName())) {
            return;
        }
        nameValidator.validate(name);
    }

    @Override
    public boolean isModified() {
        return !displayName.equals(tomcatInfo.getName());
    }

    @Override
    public void apply() {
        tomcatInfo.setName(displayName);
    }
}

@FunctionalInterface
interface TomcatNameValidator<T> {
    void validate(T t) throws ConfigurationException;
}
