package com.poratu.idea.plugins.tomcat.runner;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.poratu.idea.plugins.tomcat.conf.EnhancedTomcatRunConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:00 AM
 */
public class TomcatDebugger extends GenericDebuggerRunner {
    private static final String RUNNER_ID = "SmartTomcatDebugger";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof EnhancedTomcatRunConfiguration);
    }

}
