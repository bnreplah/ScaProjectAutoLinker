package com.cadonuno.scaautolinker.util.apihandlers;

import com.google.common.net.UrlEscapers;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import com.cadonuno.scaautolinker.util.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ApiCaller {
    private static final String URL_BASE = "api.veracode.";
    private static final String WORKSPACE_API_URL = "v3/workspaces";
    private static final String PROJECT_API_URL = "/projects";
    private static final String SIZE_FILTER = "?size=500";
    private static final String GET_REQUEST = "GET";
    public static final int REQUEST_TIMEOUT = 0;

    public static Optional<Workspace> getWorkspaceByName(ApiCredentials apiCredentials, String workspaceName) {
        return runApi(WORKSPACE_API_URL + SIZE_FILTER +
                        "&filter%5Bworkspace%5D=" + encodeUrlParameter(workspaceName), GET_REQUEST,
                null, apiCredentials)
                .flatMap(JsonHandler::getWorkspacesFromUrl)
                .flatMap(workspaces -> findExactNameMatch(workspaces, workspaceName, Workspace::getName));
    }

    public static Optional<Project> getProjectByName(ApiCredentials apiCredentials,
                                                 Workspace workspace, String projectName) {
        return runApi(WORKSPACE_API_URL + "/" + workspace.getGuid() + PROJECT_API_URL + SIZE_FILTER +
                        "&filter%5Bproject%5D=" + encodeUrlParameter(projectName),
                GET_REQUEST, null, apiCredentials)
                .flatMap(apiCallResult -> JsonHandler.getProjectsFromUrl(apiCallResult, workspace))
                .flatMap(projects -> findExactNameMatch(projects, projectName, Project::getName));
    }

    private static <T> Optional<T> findExactNameMatch(List<T> listToEvaluate, String nameToCheck,
                                                      Function<T, String> nameGetter) {
        return listToEvaluate.stream()
                .filter(element -> nameToCheck.equals(nameGetter.apply(element)))
                .findFirst();
    }

    private static String encodeUrlParameter(String additionalFilter) {
        return UrlEscapers.urlFragmentEscaper().escape(additionalFilter);
    }

    private static Optional<JSONObject> runApi(String apiUrl, String requestType,
                                               String jsonParameters, ApiCredentials apiCredentials) {
        try {
            final URL applicationsApiUrl = new URL("https://" + URL_BASE + apiCredentials.getInstance() + "/srcclr/" + apiUrl);
            final String authorizationHeader =
                    HmacRequestSigner.getVeracodeAuthorizationHeader(apiCredentials, applicationsApiUrl, requestType);

            final HttpsURLConnection connection = (HttpsURLConnection) applicationsApiUrl.openConnection();
            connection.setConnectTimeout(REQUEST_TIMEOUT);
            connection.setReadTimeout(REQUEST_TIMEOUT);
            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Authorization", authorizationHeader);

            if (jsonParameters != null) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonParameters.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }
            }

            try (InputStream responseInputStream = connection.getInputStream()) {
                return Optional.of(readResponse(responseInputStream));
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
                | IOException | JSONException e) {
            Logger.log("Unable to run API at: " + apiUrl + "\nWith parameters: " + jsonParameters);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /*
     * A simple method to read an input stream (containing JSON) to System.out.
     */
    private static JSONObject readResponse(InputStream responseInputStream) throws IOException, JSONException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] responseBytes = new byte[16384];
        int x;
        while ((x = responseInputStream.read(responseBytes, 0, responseBytes.length)) != -1) {
            outputStream.write(responseBytes, 0, x);
        }
        outputStream.flush();
        return new JSONObject(outputStream.toString());
    }
}
