package com.denglitong.zuulservice.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.denglitong.zuulservice.context.UserContextHolder.getContext;
import static com.denglitong.zuulservice.filters.FilterHeaderKey.*;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
@Component
public class UserContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

        getContext().setCorrelationId(httpServletRequest.getHeader(CORRELATION_ID.getName()));
        getContext().setUserId(httpServletRequest.getHeader(USER_ID.getName()));
        getContext().setAuthToken(httpServletRequest.getHeader(AUTH_TOKEN.getName()));
        getContext().setOrgId(httpServletRequest.getHeader(ORG_ID.getName()));

        logger.debug("Special Routes Service Incoming Correlation id: {}", getContext().getCorrelationId());

        filterChain.doFilter(httpServletRequest, servletResponse);
    }
}
