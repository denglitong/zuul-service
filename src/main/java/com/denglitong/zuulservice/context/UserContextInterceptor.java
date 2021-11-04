package com.denglitong.zuulservice.context;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.denglitong.zuulservice.context.UserContextHolder.getContext;
import static com.denglitong.zuulservice.filters.FilterHeaderKey.AUTH_TOKEN;
import static com.denglitong.zuulservice.filters.FilterHeaderKey.CORRELATION_ID;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
@Component
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add(CORRELATION_ID.getName(), getContext().getCorrelationId());
        headers.add(AUTH_TOKEN.getName(), getContext().getAuthToken());

        return execution.execute(request, body);
    }
}
