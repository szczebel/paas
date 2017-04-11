package paas.desktop.remoting;

import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

@SuppressWarnings("unused")
public abstract class RestCall<T> {
    final Class<T> expectedClass;
    final MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    private final String url;
    final HttpHeaders headers;
    private final ResponseErrorHandler errorHandler = new ErrorHandler();
    private Supplier<? extends RestTemplate> templateSupplier = RestTemplate::new;

    RestCall(String url, Class<T> expectedClass) {
        this.url = url;
        this.expectedClass = expectedClass;
        this.headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    public static <T> RestGet<T> restGet(String url, Class<T> expectedClass) {
        return new RestGet<>(url, expectedClass);
    }

    static <T> RestGetList<T> restGetList(String url, Class<T[]> expectedClass) {
        return new RestGetList<>(url, expectedClass);
    }

    static <T> RestPost<T> restPost(String url, Class<T> expectedClass) {
        return new RestPost<>(url, expectedClass);
    }

    public static RestPostVoid restPostVoid(String url) {
        return new RestPostVoid(url);
    }

    public static RestDelete restDelete(String url) {
        return new RestDelete(url);
    }

    public RestCall<T> httpBasic(String username, String password) {
        String plainClientCredentials = String.join(":", username, password);
        String base64ClientCredentials = Base64.getEncoder().encodeToString((plainClientCredentials.getBytes()));
        headers.add("Authorization", "Basic " + base64ClientCredentials);
        return this;
    }

    public RestCall<T> authToken(String token) {
        headers.add("Authorization", "Bearer " + token);
        return this;
    }

    public RestCall<T> restTemplate(Supplier<? extends RestTemplate> factory) {
        this.templateSupplier = factory;
        return this;
    }

    RestCall<T> param(String key, Object value) {
        params.add(key, value);
        return this;
    }

    final String originalUrl() {
        return url;
    }

    String url() {
        String retval = originalUrl();
        if(!params.isEmpty()) retval += "?";
        for (String key : params.keySet()) {
            List<Object> values = params.get(key);
            for (Object value : values) {
                retval += key + "=" + String.valueOf(value) + "&";
            }
        }
        return retval;
    }

    RestTemplate template() {
        RestTemplate restTemplate = templateSupplier.get();
        restTemplate.setErrorHandler(errorHandler);
        return restTemplate;
    }

    public HttpEntity<?> req() {
        return new HttpEntity<>(headers);
    }

    RestCall<T> contentType(MediaType type) {
        headers.setContentType(type);
        return this;
    }

    public T execute() {
        throw new UnsupportedOperationException("Override to provide implementation");
    }

    public static abstract class RestParametrizedCall<T> extends RestCall<T> {

        RestParametrizedCall(String url, Class<T> expectedClass) {
            super(url, expectedClass);
        }

        public HttpEntity<MultiValueMap<String, Object>> req() {
            return new HttpEntity<>(params, headers);
        }

        @Override
        String url() {
            return originalUrl();
        }
    }

    public static class RestGetList<T> extends RestCall<T[]> {

        private RestGetList(String url, Class<T[]> expectedClass) {
            super(url, expectedClass);
        }

        @Override
        public T[] execute() {
            return template().exchange(
                    url(),
                    HttpMethod.GET,
                    req(),
                    expectedClass
            ).getBody();
        }
    }

    public static class RestGet<T> extends RestCall<T> {

        private RestGet(String url, Class<T> expectedClass) {
            super(url, expectedClass);
        }

        public T execute() {
            return template().exchange(url(), HttpMethod.GET, req(), expectedClass).getBody();
        }

    }

    public static class RestPost<T> extends RestParametrizedCall<T> {

        private final String url;
        private final Class<T> expectedClass;

        private RestPost(String url, Class<T> expectedClass) {
            super(url, expectedClass);
            this.url = url;
            this.expectedClass = expectedClass;
        }

        public T execute() {
            return template().postForObject(url, req(), expectedClass);
        }

    }

    public static class RestDelete extends RestCall<Void> {

        private RestDelete(String url) {
            super(url, Void.class);
        }

        public Void execute() {
            template().exchange(url(), HttpMethod.DELETE, req(), Void.class);
            return null;
        }
    }

    public static class RestPostVoid extends RestParametrizedCall<Void> {

        private RestPostVoid(String url) {
            super(url, Void.class);
        }

        public Void execute() {
            template().postForEntity(url(), req(), Void.class);
            return null;
        }

    }

    private static class ErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            HttpStatus statusCode = getHttpStatusCode(response);
            String responseString = new String(getResponseBody(response));
            throw new RestClientResponseException(
                    responseString,
                    statusCode.value(),
                    statusCode.getReasonPhrase(),
                    response.getHeaders(),
                    getResponseBody(response),
                    Charset.defaultCharset()
            );
        }

        private HttpStatus getHttpStatusCode(ClientHttpResponse response) throws IOException {
            try {
                return response.getStatusCode();
            } catch (IllegalArgumentException var4) {
                throw new UnknownHttpStatusCodeException(
                        response.getRawStatusCode(),
                        response.getStatusText(),
                        response.getHeaders(),
                        this.getResponseBody(response),
                        null
                );
            }
        }

        private byte[] getResponseBody(ClientHttpResponse response) {
            try {
                InputStream responseBody = response.getBody();
                if (responseBody != null) {
                    return FileCopyUtils.copyToByteArray(responseBody);
                }
            } catch (IOException ignored) {
            }

            return new byte[0];
        }
    }

}
