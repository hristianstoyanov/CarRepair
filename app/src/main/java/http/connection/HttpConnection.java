package http.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection {

    public static HttpURLConnection getHttpURLConnection(String url, String requestMethod, boolean doOutput,int connectTimeout, int readTimeout) throws IOException {
        URL connectedUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) connectedUrl.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setDoOutput(doOutput);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
//        connection.connect();

        return connection;
    }
}
