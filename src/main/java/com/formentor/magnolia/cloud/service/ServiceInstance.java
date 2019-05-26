package com.formentor.magnolia.cloud.service;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ServiceInstance {
    private final String instanceId;
    private final String serviceId;
    private final String host;
    private final int port;
    private final Map<String, String> metadata;
}
