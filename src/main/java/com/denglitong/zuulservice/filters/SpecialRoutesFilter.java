package com.denglitong.zuulservice.filters;

import com.denglitong.zuulservice.model.AbTestingRoute;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static com.denglitong.zuulservice.filters.FilterType.ROUTE;
import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
public class SpecialRoutesFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesFilter.class);
    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = false;

    private FilterUtils filterUtils;
    private RestTemplate restTemplate;
    private ProxyRequestHelper requestHelper = new ProxyRequestHelper();

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String filterType() {
        return ROUTE.getValue();
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = getCurrentContext();

        AbTestingRoute abTestRoute = getAbRoutingInfo(filterUtils.getServiceId());
        if (abTestRoute != null && useSpecialRoute(abTestRoute)) {
            String oldEndpoint = ctx.getRequest().getRequestURI();
            String newEndpoint = abTestRoute.getEndPoint();
            String serviceId = ctx.get(FilterUtils.SERVICE_ID).toString();

            String route = buildRouteString(oldEndpoint, newEndpoint, serviceId);
            forwardToSpecialRoute(route);
        }

        return null;
    }

    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
        int index = oldEndpoint.indexOf(serviceName);

        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        logger.info("Target route: {}/{}", newEndpoint, strippedRoute);
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    private void forwardToSpecialRoute(String route) {
        RequestContext context = getCurrentContext();

        HttpServletRequest request = context.getRequest();
        MultiValueMap<String, String> headers = requestHelper.buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = requestHelper.buildZuulRequestQueryParams(request);
        String verb = request.getMethod().toUpperCase();
        InputStream requestEntity = getRequestBody(request);

        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        requestHelper.addIgnoredHeaders();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpResponse response = forward(httpClient, verb, route, request, headers, params, requestEntity);
            setResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HttpResponse forward(CloseableHttpClient httpClient,
                                 String verb, String uri,
                                 HttpServletRequest request,
                                 MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params,
                                 InputStream requestEntity) throws IOException {
        // what this line do?
        Map<String, Object> info = requestHelper.debug(verb, uri, headers, params, requestEntity);

        int contentLength = request.getContentLength();
        ContentType contentType = request.getContentType() != null ? ContentType.create(request.getContentType()) : null;
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength, contentType);

        HttpRequest httpRequest;
        switch (HttpMethod.valueOf(verb.toUpperCase())) {
            case POST:
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setEntity(entity);
                httpRequest = httpPost;
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(uri);
                httpPut.setEntity(entity);
                httpRequest = httpPut;
                break;
            case PATCH:
                HttpPatch httpPatch = new HttpPatch(uri);
                httpPatch.setEntity(entity);
                httpRequest = httpPatch;
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);
        }
        httpRequest.setHeaders(convertHeaders(headers));

        URL host = new URL(uri);
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());

        return httpClient.execute(httpHost, httpRequest);
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new Header[0]);
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (Header header : headers) {
            String name = header.getName();
            map.computeIfAbsent(name, n -> map.put(n, new ArrayList<>()));
            Objects.requireNonNull(map.get(name)).add(header.getValue());
        }
        return map;
    }

    private void setResponse(HttpResponse response) throws IOException {
        requestHelper.setResponse(
                response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        try {
            return request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok
            return null;
        }
    }

    private AbTestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbTestingRoute> restExchange = null;
        try {
            restExchange = restTemplate.exchange(
                    "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    GET, null, AbTestingRoute.class, serviceName);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == NOT_FOUND) return null;
            throw ex;
        }
        return restExchange.getBody();
    }

    public boolean useSpecialRoute(AbTestingRoute testRoute) {
        if (testRoute.getActive().equals("N")) return false;

        Random random = new Random();
        int value = random.nextInt(10) + 1;
        return testRoute.getWeight() < value;
    }
}
