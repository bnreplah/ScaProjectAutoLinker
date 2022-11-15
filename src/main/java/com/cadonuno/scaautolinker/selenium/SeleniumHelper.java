package com.cadonuno.scaautolinker.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public final class SeleniumHelper {
    private static final int POLLING_TIMEOUT = 30;

    private SeleniumHelper() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    public static boolean hasElement(WebDriver webDriver, By elementToCheck) {
        try {
            waitForElementPresent(webDriver, elementToCheck);
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public static boolean hasElement(WebDriver webDriver, WebElement parentElement, By elementToCheck) {
        try {
            waitForElementPresent(webDriver, parentElement, elementToCheck);
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    public static boolean hasElementRightNow(WebDriver webDriver, By elementToCheck) {
        try {
            webDriver.findElement(elementToCheck);
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public static boolean hasElementRightNow(WebElement parentElement, By elementToCheck) {
        try {
            parentElement.findElement(elementToCheck);
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    private static void waitForElementPresent(WebDriver webDriver, WebElement parentElement, By elementToCheck) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();
        while (!hasFound) {
            try {
                parentElement.findElement(elementToCheck);
                hasFound = true;
            } catch (NoSuchElementException notFound) {
                checkTimeout(start, webDriver);
            }
        }
    }

    public static void waitForElementPresent(WebDriver webDriver, By elementToCheck) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();
        while (!hasFound) {
            try {
                webDriver.findElement(elementToCheck);
                hasFound = true;
            } catch (NoSuchElementException notFound) {
                checkTimeout(start, webDriver);
            }
        }
    }

    public static void checkTimeout(Instant start, WebDriver webDriver) throws TimeoutException {
        if (getTimeElapsed(start) > POLLING_TIMEOUT) {
            timeout();
        }
        if (webDriver != null && webDriver.getCurrentUrl().contains("/login?logout")) {
            timeout();
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            //nothing to do here
        }
    }

    public static void clickElement(WebDriver webDriver, By elementToClick) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();
        while (!hasFound) {
            try {
                webDriver.findElement(elementToClick).click();
                hasFound = true;
            } catch (ElementNotInteractableException | NoSuchElementException notFound) {
                checkTimeout(start, webDriver);
            }
        }
    }

    public static void clickElement(WebDriver webDriver, WebElement parentElement, By elementToClick) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();
        while (!hasFound) {
            try {
                parentElement.findElement(elementToClick).click();
                hasFound = true;
            } catch (ElementNotInteractableException | NoSuchElementException notFound) {
                checkTimeout(start, webDriver);
            }
        }
    }

    public static void selectOptionByVisibleText(WebDriver webDriver, By elementToSelect,
                                                 String textToSelect) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();
        WebElement foundElement = null;
        while (!hasFound) {
            try {
                foundElement = webDriver.findElement(elementToSelect);
                hasFound = true;
            } catch (ElementNotInteractableException | NoSuchElementException notFound) {
                checkTimeout(start, webDriver);
            }
        }
        new Select(foundElement).selectByVisibleText(textToSelect);
    }

    private static void timeout() throws TimeoutException {
        throw new TimeoutException("Timed out when running command");
    }

    private static long getTimeElapsed(Instant start) {
        return Duration.between(start, Instant.now()).getSeconds();
    }

    public static Optional<WebElement> getElement(WebDriver webDriver, By elementToGet){
        Instant start = Instant.now();
        while (true) {
            try {
                return Optional.ofNullable(webDriver.findElement(elementToGet));
            } catch (ElementNotInteractableException | NoSuchElementException notFound) {
                try {
                    checkTimeout(start, webDriver);
                } catch (TimeoutException timeout) {
                    return Optional.empty();
                }
            }
        }
    }

    public static Optional<WebElement> getElement(WebDriver webDriver, WebElement parentElement, By elementToGet) {
        Instant start = Instant.now();
        while (true) {
            try {
                return Optional.ofNullable(parentElement.findElement(elementToGet));
            } catch (ElementNotInteractableException | NoSuchElementException notFound) {
                try {
                    checkTimeout(start, webDriver);
                } catch (TimeoutException timeout) {
                    return Optional.empty();
                }
            }
        }
    }

    public static void waitForCondition(WebDriver webDriver, By elementToCheck,
                                        Predicate<WebElement> webElementPredicate) throws TimeoutException {
        waitForConditionInternal(webDriver, elementToCheck, webElementPredicate, true);
    }

    public static void waitForConditionOrMissingElement(WebDriver webDriver, By elementToCheck,
                                                        Predicate<WebElement> webElementPredicate) throws TimeoutException {
        waitForConditionInternal(webDriver, elementToCheck, webElementPredicate, false);
    }

    private static void waitForConditionInternal(WebDriver webDriver, By elementToCheck,
                                                 Predicate<WebElement> webElementPredicate,
                                                 boolean failOnMissingElement) throws TimeoutException {
        Instant start = Instant.now();
        boolean isAwaitingCondition = true;
        while (isAwaitingCondition) {
            try {
                WebElement foundElement = webDriver.findElement(elementToCheck);
                isAwaitingCondition = !webElementPredicate.test(foundElement);
            } catch (ElementNotInteractableException notFound) {
                checkTimeout(start, webDriver);
            } catch (NoSuchElementException nonExisting) {
                if (failOnMissingElement) {
                    checkTimeout(start, webDriver);
                } else {
                    isAwaitingCondition = false;
                }
            }
        }
    }

    public static void waitForElementPresentAndClickIt(WebDriver webDriver, By elementToClick) throws TimeoutException {
        waitForElementPresent(webDriver, elementToClick);
        clickElement(webDriver, elementToClick);
    }
}
