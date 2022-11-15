package com.cadonuno.scaautolinker.executionparameters;

import com.cadonuno.scaautolinker.util.Logger;

import java.util.*;

public final class ParameterParser {
    private final Map<String, String> parsedParameters;

    public ParameterParser(String[] commandLineArguments) {
        parsedParameters = new HashMap<>();
        String currentParameter = "";
        for (String argument : commandLineArguments) {
            if (argument.charAt(0) == '-') {
                currentParameter = argument.toLowerCase(Locale.ROOT);
            } else {
                Logger.debug("Reading Parameter: " + currentParameter + " with value " + argument);
                parsedParameters.put(currentParameter, argument);
            }
        }
    }

    public String getParameterAsString(String fullName, String simpleName) {
        return parsedParameters.getOrDefault(fullName, parsedParameters.get(simpleName));
    }
}
