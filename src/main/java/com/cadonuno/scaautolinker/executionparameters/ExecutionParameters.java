package com.cadonuno.scaautolinker.executionparameters;

import com.cadonuno.scaautolinker.selenium.WebDriverProvider;
import com.cadonuno.scaautolinker.util.ApiCredentials;
import com.cadonuno.scaautolinker.util.Logger;

import static com.cadonuno.scaautolinker.executionparameters.Parameters.*;

import java.util.Optional;

public class ExecutionParameters {
    private final ApiCredentials apiCredentials;
    private final String veracodeUsername;
    private final String veracodePassword;
    private final String projectName;
    private final String workspaceName;
    private final String applicationProfileName;
    private final String seleniumDriverName;
    private final String seleniumDriverLocation;
    private final String defaultBranch;

    protected ExecutionParameters(ApiCredentials apiCredentials,
                                  String veracodeUsername, String veracodePassword,
                                  String seleniumDriverName, String seleniumDriverLocation,
                                  String projectName, String workspaceName, String applicationProfileName,
                                  String defaultBranch) {
        validateInput(veracodeUsername, veracodePassword, seleniumDriverName,
                seleniumDriverLocation, projectName, workspaceName, applicationProfileName);
        this.veracodeUsername = veracodeUsername;
        this.veracodePassword = veracodePassword;
        this.projectName = projectName;
        this.workspaceName = workspaceName;
        this.applicationProfileName = applicationProfileName;
        this.seleniumDriverName = seleniumDriverName;
        this.seleniumDriverLocation = seleniumDriverLocation;
        this.apiCredentials = apiCredentials;
        this.defaultBranch = defaultBranch;
    }

    private void validateInput(String veracodeUsername, String veracodePassword, String seleniumDriverName,
                               String seleniumDriverLocation, String projectName,
                               String workspaceName, String applicationProfileName) {
        validateSingleInput(veracodeUsername, "Veracode username",
                VERACODE_USERNAME_FULL_ARGUMENT, VERACODE_USERNAME_SIMPLIFIED_ARGUMENT);
        validateSingleInput(veracodePassword, "Veracode password",
                VERACODE_PASSWORD_FULL_ARGUMENT, VERACODE_PASSWORD_SIMPLIFIED_ARGUMENT);
        validateSingleInput(seleniumDriverName, "Selenium driver name",
                SELENIUM_DRIVER_NAME_FULL_ARGUMENT, SELENIUM_DRIVER_NAME_SIMPLIFIED_ARGUMENT);
        validateSingleInput(seleniumDriverLocation, "Selenium driver location",
                SELENIUM_DRIVER_LOCATION_FULL_ARGUMENT, SELENIUM_DRIVER_LOCATION_SIMPLIFIED_ARGUMENT);

        validateSingleInput(projectName, "Project Name",
                PROJECT_NAME_FULL_ARGUMENT, PROJECT_NAME_SIMPLIFIED_ARGUMENT);
        validateSingleInput(workspaceName, "Workspace name",
                WORKSPACE_NAME_FULL_ARGUMENT, WORKSPACE_NAME_SIMPLIFIED_ARGUMENT);
        validateSingleInput(applicationProfileName, "Application Profile name",
                APPLICATION_PROFILE_NAME_FULL_ARGUMENT, APPLICATION_PROFILE_NAME_SIMPLIFIED_ARGUMENT);
    }

    private void validateSingleInput(String veracodeUsername, String fullName,
                                     String fullArgument, String simplifiedArgument) {
        if (veracodeUsername == null || veracodeUsername.isEmpty()) {
            throw new IllegalArgumentException(fullName + " argument is mandatory (" + fullArgument + ", " + simplifiedArgument + ")");
        }
    }

    public static Optional<ExecutionParameters> of(String[] commandLineArguments) {
        Logger.log("Parsing Execution Parameters");
        Optional<ExecutionParameters> executionParameters =
                Optional.of(parseParameters(new ParameterParser(commandLineArguments)));
        Logger.log("Finished parsing Execution Parameters");
        return executionParameters;
    }

    private static ExecutionParameters parseParameters(
            ParameterParser parameterParser) {
        Logger.isDebug = Optional.ofNullable(parameterParser.getParameterAsString("--debug", "-d"))
                .filter("true"::equals)
                .isPresent();
        Logger.isDebugSelenium = Optional.ofNullable(parameterParser.getParameterAsString("--debug_selenium", "-ds"))
                .filter("true"::equals)
                .isPresent();
        WebDriverProvider.isHeadless = Optional.ofNullable(parameterParser.getParameterAsString("--headless", "-headless"))
                .map("true"::equals)
                .orElse(true);
        return new ExecutionParameters(
                new ApiCredentials(
                        parameterParser.getParameterAsString(VERACODE_ID_FULL_ARGUMENT, VERACODE_ID_SIMPLIFIED_ARGUMENT),
                        parameterParser.getParameterAsString(VERACODE_KEY_FULL_ARGUMENT, VERACODE_KEY_SIMPLIFIED_ARGUMENT)),
                parameterParser.getParameterAsString(VERACODE_USERNAME_FULL_ARGUMENT, VERACODE_USERNAME_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(VERACODE_PASSWORD_FULL_ARGUMENT, VERACODE_PASSWORD_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(SELENIUM_DRIVER_NAME_FULL_ARGUMENT, SELENIUM_DRIVER_NAME_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(SELENIUM_DRIVER_LOCATION_FULL_ARGUMENT, SELENIUM_DRIVER_LOCATION_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(PROJECT_NAME_FULL_ARGUMENT, PROJECT_NAME_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(WORKSPACE_NAME_FULL_ARGUMENT, WORKSPACE_NAME_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(APPLICATION_PROFILE_NAME_FULL_ARGUMENT, APPLICATION_PROFILE_NAME_SIMPLIFIED_ARGUMENT),
                parameterParser.getParameterAsString(DEFAULT_BRANCH_FULL_ARGUMENT, DEFAULT_BRANCH_SIMPLIFIED_ARGUMENT));
    }

    public ApiCredentials getApiCredentials() {
        return apiCredentials;
    }

    public String getSeleniumDriverLocation() {
        return seleniumDriverLocation;
    }

    public String getSeleniumDriverName() {
        return seleniumDriverName;
    }

    public String getVeracodePassword() {
        return veracodePassword;
    }

    public String getVeracodeUsername() {
        return veracodeUsername;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getApplicationProfileName() {
        return applicationProfileName;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }
}
