package se.pinnacle.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class Connection {

    private final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private final String HTTP_HEADER_ACCEPT = "Accept";
    private final String HTTP_HEADER_ACCEPT_CHARSET = "Accept-Charset";
    private final String APPLICATION_XML = "application/xml";
    private final String CHARSET = "UTF-8";
    private String sessionToken;

    public Connection(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String sendGetRequest(URL service) throws IOException {
        String result = null;
        try {
            HttpGet get = new HttpGet(service.toURI());
            get.setHeader("Authorization", sessionToken);
            get.setHeader(HTTP_HEADER_ACCEPT, APPLICATION_XML);
            get.setHeader(HTTP_HEADER_CONTENT_TYPE, APPLICATION_XML);
            get.setHeader(HTTP_HEADER_ACCEPT_CHARSET, CHARSET);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            result = EntityUtils.toString(response.getEntity());
        } catch (URISyntaxException e) {
        }
        return result;
    }
}
