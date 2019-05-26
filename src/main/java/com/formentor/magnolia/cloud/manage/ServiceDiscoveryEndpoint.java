package com.formentor.magnolia.cloud.manage;

import com.formentor.magnolia.cloud.service.ServiceDiscovery;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for service registry management
 */
@Path("/magnolia-cloud-service")
public class ServiceDiscoveryEndpoint {

    private final ServiceDiscovery serviceDiscovery;

    @Inject
    public ServiceDiscoveryEndpoint(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @POST
    @Path("/register")
    @Produces({MediaType.TEXT_PLAIN})
    public Response register() {
        try {
            if (serviceDiscovery.registerService()) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/deregister")
    @Produces({MediaType.TEXT_PLAIN})
    public Response deregister() {
        try {
            if (serviceDiscovery.deRegisterService()) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
