package com.denglitong.zuulservice.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.denglitong.zuulservice.filters.FilterHeaderKey.CORRELATION_ID;
import static com.denglitong.zuulservice.filters.FilterType.PRE;
import static com.netflix.zuul.context.RequestContext.getCurrentContext;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
@Component
public class TrackingFilter extends ZuulFilter {

    public static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    public static final int FILTER_ORDER = 1;
    public static final boolean SHOULD_FILTER = true;

    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    @Override
    public String filterType() {
        return PRE.getValue();
    }

    /**
     * 不同类型的过滤器的执行顺序
     */
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
        if (filterUtils.isPresent(CORRELATION_ID)) {
            logger.info("{} is found in tracking filter: {}", CORRELATION_ID.getName(),
                    filterUtils.getFilterValue(CORRELATION_ID));
        } else {
            filterUtils.setFilterValue(CORRELATION_ID, generateCorrelationId());
            logger.info("{} generated in tracking filter: {}", CORRELATION_ID.getName(),
                    filterUtils.getFilterValue(CORRELATION_ID));
        }
        logger.info("Processing incoming request for {}", getCurrentContext().getRequest().getRequestURI());
        return null;
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
