package com.formentor.magnolia.cloud.consul;

import com.formentor.magnolia.cloud.consul.dto.ServiceDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

public interface ConsulAPIClient {
    // http://127.0.0.1:8500/v1/agent/service/register
    @PUT
    @Path("agent/service/register")
    @Consumes({MediaType.APPLICATION_JSON})
    void register(ServiceDto serviceDto);

    // http://127.0.0.1:8500/v1/agent/service/deregister/:my-service-id
    @PUT
    @Path("agent/service/deregister/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    void deRegister(@PathParam("id") String id);
}
