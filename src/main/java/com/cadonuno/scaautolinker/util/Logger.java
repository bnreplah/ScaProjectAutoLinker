package com.cadonuno.scaautolinker.util;

import java.util.Arrays;

public final class Logger {
    public static boolean isDebug = false;
    public static int currentLevel = 0;
    public static boolean isDebugSelenium = false;

    private Logger() {

    }

    public static void log(String aMessage) {
        Arrays.stream(aMessage.split("\n"))
                        .forEach(Logger::logInternal);
    }

    private static void logInternal(Object aMessage) {
        for (int level = 0; level < currentLevel; level++) {
            System.out.print("  ");
        }
        System.out.println(aMessage);
    }

    public static void debug(Runnable runnable) {
        if (isDebug) {
            runnable.run();
        }
    }

    public static void debug(String aMessage) {
        if (isDebug) {
            log(aMessage);
        }
    }

    public static void printLine() {
        log("_".repeat(Math.max(0, 60 - currentLevel * 2)));
    }
}
