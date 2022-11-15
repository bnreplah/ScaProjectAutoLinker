package com.cadonuno.scaautolinker.util;

public class ApiCredentials {
    private final String apiId;
    private final String apiKey;

    private final String instance;

    public ApiCredentials(String apiId, String apiKey) {
        if (apiId == null || apiId.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode ID argument is mandatory (--veracode_id, -vi)");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode Key argument is mandatory (--veracode_key, -vk)");
        }
        this.apiId = apiId;
        this.apiKey = apiKey;
        this.instance = apiId.startsWith("vera01ei-") && apiKey.startsWith("vera01es-")
                ? "eu"
                : "com";
    }

    public String getApiId() {
        return this.apiId;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getInstance() {
        return instance;
    }
}
