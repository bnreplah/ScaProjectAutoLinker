package com.cadonuno.scaautolinker;

import com.cadonuno.scaautolinker.executionparameters.ExecutionParameters;
import com.cadonuno.scaautolinker.selenium.ScaProjectUpdater;
import com.cadonuno.scaautolinker.util.apihandlers.ApiCaller;

public class Main {

    public static void main(String[] args) {
        ExecutionParameters.of(args).ifPresent(Main::execute);
    }

    private static void execute(ExecutionParameters executionParameters) {
        ApiCaller.getWorkspaceByName(executionParameters.getApiCredentials(),
                        executionParameters.getWorkspaceName())
                .flatMap(workspace -> ApiCaller.getProjectByName(executionParameters.getApiCredentials(),
                        workspace, executionParameters.getProjectName()))
                .ifPresent(project ->
                    new ScaProjectUpdater(
                            executionParameters.getSeleniumDriverName(), executionParameters.getSeleniumDriverLocation(),
                            executionParameters.getVeracodeUsername(), executionParameters.getVeracodePassword())
                            .linkToApplication(executionParameters.getApiCredentials().getInstance(),
                                    project, executionParameters.getApplicationProfileName(),
                                    executionParameters.getDefaultBranch())
                );
    }
}
