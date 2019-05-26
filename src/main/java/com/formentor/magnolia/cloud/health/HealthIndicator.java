package com.formentor.magnolia.cloud.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HealthIndicator {
    private static final String DEFAULT_MESSAGE = "Health check failed";

    public final Health health() {
        Health.Builder builder = new Health.Builder();
        try {
            doHealthCheck(builder);
        }
        catch (Exception ex) {
            if (log.isWarnEnabled()) {
                String message = ex.getMessage();
                log.warn(StringUtils.isNotBlank(message) ? message : DEFAULT_MESSAGE,
                        ex);
            }
            builder.down(ex);
        }
        return builder.build();
    }

    protected void doHealthCheck(Health.Builder builder) {
        builder.up();
    }
}
