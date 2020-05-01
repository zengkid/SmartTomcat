package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.poratu.idea.plugins.tomcat.conf.TomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:01 AM
 */
public class TomcatRunner extends DefaultJavaProgramRunner {
    private static final String RUNNER_ID = "SmartTomcatRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        return (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)) && runProfile instanceof TomcatRunConfiguration;
    }

}
