package fr.raraph84.raraboxpanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpRequest {

    private final String url;
    private String method = "GET";
    private String response;
    private int responseCode;

    public HttpRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public HttpRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public void send() throws IOException {

        HttpURLConnection con = (HttpURLConnection) new URL(url.replaceAll(" ", "%20")).openConnection();
        con.setRequestMethod(method);
        con.setConnectTimeout(2000);

        responseCode = con.getResponseCode();

        Reader streamReader = new InputStreamReader(con.getErrorStream() != null ? con.getErrorStream() : con.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(streamReader);

        String inputLine;
        StringBuilder data = new StringBuilder();
        while ((inputLine = in.readLine()) != null) data.append(inputLine);
        in.close();
        con.disconnect();

        response = data.toString();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ResponseCodeType getResponseCodeType() {
        return ResponseCodeType.fromCode(responseCode);
    }

    public String getResponse() {
        return response;
    }

    public enum ResponseCodeType {

        INFORMATION("1"),

        SUCCESS("2"),

        REDIRECT("3"),

        CLIENT_ERROR("4"),

        SERVER_ERROR("5");

        private final String startWith;

        ResponseCodeType(String startWith) {
            this.startWith = startWith;
        }

        private static ResponseCodeType fromCode(int code) {
            return Arrays.stream(values()).filter((responseCodeType) -> Integer.toString(code).startsWith(responseCodeType.startWith)).findFirst().orElse(null);
        }
    }
}
