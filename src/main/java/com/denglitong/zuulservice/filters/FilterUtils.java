package com.denglitong.zuulservice.filters;

import com.netflix.zuul.context.RequestContext;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;

/**
 * @author litong.deng@foxmail.com
 * @date 2021/11/3
 */
@Component
public class FilterUtils {

    public static final String SERVICE_ID = "serviceId";

    public final String getFilterValue(FilterHeaderKey headerKey) {
        RequestContext ctx = getCurrentContext();

        if (ctx.getRequest().getHeader(headerKey.getName()) != null) {
            return ctx.getRequest().getHeader(headerKey.getName());
        }
        return ctx.getZuulRequestHeaders().get(headerKey.getName());
    }

    public void setFilterValue(FilterHeaderKey headerKey, String value) {
        getCurrentContext().addZuulRequestHeader(headerKey.getName(), value);
    }

    public boolean isPresent(FilterHeaderKey headerKey) {
        return !StringUtils.isEmpty(getFilterValue(headerKey));
    }

    public String getServiceId() {
        RequestContext ctx = getCurrentContext();
        // we might not have a serviceId if we are using a static, non-eureka route.
        return ctx.get(SERVICE_ID) == null
                ? Strings.EMPTY
                : ctx.get(SERVICE_ID).toString();
    }
}
