package com.formentor.magnolia.cloud.health;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST endpoint for health check
 */
@Path("/health")
public class HealthEndpoint {

    private final HealthIndicator healthIndicator;

    @Inject
    public HealthEndpoint(HealthIndicator healthIndicator) {
        this.healthIndicator = healthIndicator;
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    public Health health() {
        return healthIndicator.health();
    }

}
