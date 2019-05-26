package com.formentor.magnolia.cloud.service;

import java.util.List;

public interface ServiceDiscovery<T> {
    boolean registerService();
    boolean deRegisterService();
    boolean deRegisterService(T service);
    boolean registerService(T service);
    List<ServiceInstance> getInstances(String serviceName);
}
