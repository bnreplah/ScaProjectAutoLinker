package com.cadonuno.scaautolinker.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.cadonuno.scaautolinker.util.Logger;
import com.cadonuno.scaautolinker.util.Project;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

public class ScaProjectUpdater {
    private static final String AGENT_BASED_BASE_URL = "https://sca.analysiscenter.veracode.";
    private static final String LOGIN_BASE_URL = "https://web.analysiscenter.veracode.";
    private static final String LOGIN_URL_PATH = "/login/";
    private static final String USERNAME_FIELD_ID = "okta-signin-username";
    private static final String PASSWORD_FIELD_ID = "okta-signin-password";
    private static final String LOGIN_BUTTON_ID = "okta-signin-submit";
    private static final String USER_NAME_ICON_ID = "icon_user";
    private static final String SETTINGS_BUTTON_CSS_SELECTOR = ".link--obvious > .font--16";
    private static final int MAX_ATTEMPTS_PER_PROJECT = 10;
    private static final String LINK_TO_APPLICATION_MENU_OPTION = "//div[7]/div/div/div[2]/div/div/div/div[3]";
    private static final String APPLICATION_SELECT_OPTION_CSS = ".srcclr-react-select__value-container--has-value";
    private static final String SAVE_BUTTON_XPATH = "//button[text()='Save']";
    private static final String BRANCH_SELECT_OPTION_XPATH = "//div[@class='css-1hwfws3 srcclr-react-select__value-container srcclr-react-select__value-container--has-value']";
    private static final String DROPDOWN_SELECT_BASE_XPATH = "//div[@class='css-15k3avv srcclr-react-select__menu']/div";

    private final String seleniumDriverName;
    private final String seleniumDriverLocation;
    private final String veracodeUsername;
    private final String veracodePassword;
    private WebDriver webDriver;

    public ScaProjectUpdater(String seleniumDriverName,
                             String seleniumDriverLocation,
                             String veracodeUsername,
                             String veracodePassword) {
        this.seleniumDriverName = seleniumDriverName;
        this.seleniumDriverLocation = seleniumDriverLocation;
        this.veracodeUsername = veracodeUsername;
        this.veracodePassword = veracodePassword;
    }

    public void linkToApplication(String instance, Project project, String applicationToLinkTo, String defaultBranch) {
        System.setProperty(seleniumDriverName, seleniumDriverLocation);
        if (!Logger.isDebugSelenium) {
            System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        }
        webDriver = WebDriverProvider.getDriver(seleniumDriverName);
        try {
            if (defaultBranch == null) {
                Logger.log(" - Trying to link project " + project.getFullName() + "to the application: " + applicationToLinkTo);
            } else {
                Logger.log(" - Trying to link branch '" + defaultBranch + "' of the project " + project.getFullName() +
                        " to the application: " + applicationToLinkTo);
            }
            linkToApplicationInternal(instance, project, applicationToLinkTo, defaultBranch, 1);
        } finally {
            webDriver.quit();
        }
    }

    private void linkToApplicationInternal(String instance, Project project, String applicationToLinkTo,
                                           String defaultBranch, int currentAttempt) {
        try {
            if (!isLoggedIn(instance)) {
                loginToPlatform(instance);
            }
            openProject(instance, project);
            openProjectSettings();
            if (defaultBranch != null && !defaultBranch.trim().isEmpty()) {
                setDefaultBranch(defaultBranch, project.getFullName());
            }
            performLink(applicationToLinkTo, project.getFullName());
            if (defaultBranch == null) {
                Logger.log(" - Project " + project.getFullName() + " was linked to the application: " + applicationToLinkTo);
            } else {
                Logger.log(" - Branch '" + defaultBranch + "' of the project " + project.getFullName() +
                        " was linked to the application: " + applicationToLinkTo);
            }
        } catch (TimeoutException e) {
            if (currentAttempt < MAX_ATTEMPTS_PER_PROJECT) {
                Logger.debug("Rerunning");
                Logger.debug(e::printStackTrace);
                linkToApplicationInternal(instance, project, applicationToLinkTo, defaultBranch, ++currentAttempt);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isLoggedIn(String instance) {
        Logger.log("Checking if logged in");
        webDriver.get("https://web.analysiscenter.veracode." + instance);
        return SeleniumHelper.hasElementRightNow(webDriver, By.id(USER_NAME_ICON_ID));
    }

    private void openProjectSettings() throws TimeoutException {
        SeleniumHelper.waitForElementPresentAndClickIt(webDriver, By.cssSelector(SETTINGS_BUTTON_CSS_SELECTOR));
        waitForLoadBranchSelectScreen();
    }

    private void waitForLoadBranchSelectScreen() throws TimeoutException {
        boolean hasLoaded = false;
        Instant start = Instant.now();
        while (!hasLoaded) {
            WebElement branchSelectOption =
                    SeleniumHelper.getElement(webDriver, By.xpath(BRANCH_SELECT_OPTION_XPATH)).orElse(null);
            if (branchSelectOption != null) {
                hasLoaded = !SeleniumHelper.hasElementRightNow(branchSelectOption, By.xpath("..//div[text()='Loading...']"));
            }
            if (!hasLoaded) {
                SeleniumHelper.checkTimeout(start, webDriver);
            }
        }
    }

    private void setDefaultBranch(String defaultBranch, String projectName) throws TimeoutException {
        SeleniumHelper.clickElement(webDriver, By.xpath(BRANCH_SELECT_OPTION_XPATH));
        WebElement branchSelectOption =
                SeleniumHelper.getElement(webDriver, By.xpath(DROPDOWN_SELECT_BASE_XPATH))
                        .orElseThrow(() -> new RuntimeException("Unable to set default branch on project: " + projectName));
        if (!SeleniumHelper.hasElementRightNow(branchSelectOption, By.xpath("..//div[text()='" + defaultBranch + "']"))) {
            throw new RuntimeException("Project " + projectName +
                    " does not contain a branch named " + defaultBranch);
        }
        SeleniumHelper.clickElement(webDriver, branchSelectOption, By.xpath("..//div[text()='" + defaultBranch + "']"));
        SeleniumHelper.clickElement(webDriver, By.xpath(SAVE_BUTTON_XPATH));
    }

    private void openProject(String instance, Project project) throws TimeoutException {
        webDriver.get(AGENT_BASED_BASE_URL + instance +
                "/workspaces/" + project.getWorkspace().getSiteId() +
                "/projects/" + project.getSiteId());
    }

    private void performLink(String applicationToLinkTo, String projectName) throws TimeoutException {
        openLinkToApplicationMenu();
        SeleniumHelper.clickElement(webDriver, By.cssSelector(APPLICATION_SELECT_OPTION_CSS));
        WebElement branchSelectOption =
                SeleniumHelper.getElement(webDriver, By.xpath(DROPDOWN_SELECT_BASE_XPATH + "[1]"))
                        .orElseThrow(() -> new RuntimeException("Project " + projectName + " cannot be linked to an application profile!"));
        By xpath = By.xpath("..//div[text()='" + applicationToLinkTo + "']");
        if (!SeleniumHelper.hasElementRightNow(branchSelectOption, xpath)) {
            throw new RuntimeException("Unable to find application called " + applicationToLinkTo);
        }
        SeleniumHelper.clickElement(webDriver, branchSelectOption, xpath);
        SeleniumHelper.clickElement(webDriver, By.xpath(SAVE_BUTTON_XPATH));
    }

    private void openLinkToApplicationMenu() throws TimeoutException {
        SeleniumHelper.waitForElementPresentAndClickIt(webDriver, By.xpath(LINK_TO_APPLICATION_MENU_OPTION));
        waitForLoadLinkToApplicationScreen();
    }

    private void waitForLoadLinkToApplicationScreen() throws TimeoutException {
        boolean hasLoaded = false;
        Instant start = Instant.now();
        while (!hasLoaded) {
            WebElement branchSelectOption =
                    SeleniumHelper.getElement(webDriver, By.cssSelector(APPLICATION_SELECT_OPTION_CSS)).orElse(null);
            if (branchSelectOption != null) {
                hasLoaded = !SeleniumHelper.hasElementRightNow(branchSelectOption, By.xpath("..//div[text()='Loading...']"));
            }
            if (!hasLoaded) {
                SeleniumHelper.checkTimeout(start, webDriver);
            }
        }
    }

    private void loginToPlatform(String instance) throws TimeoutException {
        Logger.log("Logging into the Veracode platform");
        webDriver.get(LOGIN_BASE_URL + instance + LOGIN_URL_PATH);
        webDriver.manage().window().setSize(new Dimension(1920, 1080));
        SeleniumHelper.waitForElementPresent(webDriver, By.id(LOGIN_BUTTON_ID));
        webDriver.findElement(By.id(USERNAME_FIELD_ID)).sendKeys(veracodeUsername);
        webDriver.findElement(By.id(PASSWORD_FIELD_ID)).sendKeys(veracodePassword);
        SeleniumHelper.clickElement(webDriver, By.id(LOGIN_BUTTON_ID));
        SeleniumHelper.waitForElementPresent(webDriver, By.id(USER_NAME_ICON_ID));
        Logger.log("Logged into the Veracode platform");
        Logger.debug("Landed on URL: " + webDriver.getCurrentUrl());
    }
}
