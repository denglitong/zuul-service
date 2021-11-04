package com.denglitong.zuulservice.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.denglitong.zuulservice.filters.FilterHeaderKey.CORRELATION_ID;
import static com.denglitong.zuulservice.filters.FilterType.POST;
import static com.netflix.zuul.context.RequestContext.getCurrentContext;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
@Component
public class ResponseFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);
    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    @Override
    public String filterType() {
        return POST.getValue();
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

        logger.debug("Adding the correlation id to the outbound headers {}",
                filterUtils.getFilterValue(CORRELATION_ID));

        ctx.getResponse().addHeader(CORRELATION_ID.getName(), filterUtils.getFilterValue(CORRELATION_ID));

        logger.debug("Completing outgoing request for {}", ctx.getRequest().getRequestURI());

        return null;
    }
}
