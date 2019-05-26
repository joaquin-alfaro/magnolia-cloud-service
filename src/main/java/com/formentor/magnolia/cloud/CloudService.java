package com.formentor.magnolia.cloud;

import com.formentor.magnolia.cloud.service.ServiceDiscovery;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.objectfactory.ComponentProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This class is optional and represents the configuration for the magnolia-cloud-service module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-cloud-service</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
@Slf4j
@Getter
@Setter
public class CloudService implements info.magnolia.module.ModuleLifecycle {
    public final static String REST_CLIENT = "consul_service";
    public final static String MAGNOLIA_APP_NAME= "magnolia.cloud.appName";
    /* you can optionally implement info.magnolia.module.ModuleLifecycle */

    private Integer port;
    private String scheme;
    private String host;
    private String username;
    private String password;

    private final ComponentProvider componentProvider;

    @Inject
    public CloudService(ComponentProvider componentProvider, MagnoliaConfigurationProperties magnoliaConfigurationProperties, ServerConfiguration serverConfiguration) {
        this.componentProvider = componentProvider;

    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        try {
            ServiceDiscovery serviceDiscovery = componentProvider.getComponent(ServiceDiscovery.class);
            if (serviceDiscovery != null) {
                serviceDiscovery.registerService();
            }
        } catch (Exception e) {
            log.error("Errors during start of CloudService", e);
        }
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        try {
            ServiceDiscovery serviceDiscovery = componentProvider.getComponent(ServiceDiscovery.class);
            if (serviceDiscovery != null) {
                serviceDiscovery.deRegisterService();
            }
        } catch (Exception e) {
            log.error("Errors during stop of CloudService", e);
        }
    }

}
