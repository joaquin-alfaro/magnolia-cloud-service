package com.formentor.magnolia.cloud.publishing.sender;

import com.formentor.magnolia.cloud.service.ServiceDiscovery;
import com.formentor.magnolia.cloud.service.ServiceInstance;
import com.google.common.collect.Lists;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.publishing.PublishingCoreModule;
import info.magnolia.publishing.definition.ConfiguredReceiverDefinition;
import info.magnolia.publishing.definition.ReceiverDefinition;
import info.magnolia.publishing.packager.Packager;
import info.magnolia.publishing.pool.ThreadPool;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

@Slf4j
public class CloudSender extends info.magnolia.publishing.sender.DefaultSender {

    private final ServiceDiscovery serviceDiscovery;
    private final Provider<PublishingCoreModule> moduleProvider;

    @Inject
    public CloudSender(ServiceDiscovery serviceDiscovery, Context context, Provider<PublishingCoreModule> moduleProvider, ComponentProvider componentProvider, Packager packager, Provider<SystemContext> systemContextProvider, ThreadPool threadPool, SimpleTranslator i18n) {
        super(context, moduleProvider, componentProvider, packager, systemContextProvider, threadPool, i18n);
        this.moduleProvider = moduleProvider;
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    protected List<ReceiverDefinition> getReceivers() {
        ConfiguredReceiverDefinition receiver = moduleProvider.get().getReceivers().size() > 0 ? (ConfiguredReceiverDefinition)moduleProvider.get().getReceivers().get(0) : null;
        mapUrlWithServiceDiscovery(receiver);
        return (receiver != null) ? Lists.newArrayList(receiver) : Lists.newArrayList();
    }

    private void mapUrlWithServiceDiscovery(ConfiguredReceiverDefinition receiver) {

        String url = receiver.getUrl();
        // Get scheme, domain and context from the uri
        String scheme = "";
        if (url.indexOf("://")>0) {
            scheme = url.substring(0, url.indexOf("://") + "://".length());
        }
        String domain = "";
        if (url.indexOf("://")>0) {
            domain = url.substring(url.indexOf("://") + "://".length());
        }
        String context = "";
        if (domain.indexOf("/") >0) {
            context = domain.substring(domain.indexOf("/"));
            domain = domain.substring(0, domain.indexOf("/"));
        }

        List<ServiceInstance> serviceInstances = serviceDiscovery.getInstances(domain);
        // If the domain is not registered the return the same url
        if (serviceInstances == null || serviceInstances.isEmpty()) {
            return;
        }

        /**
         * TODO
         * Select the instance with the meta-data "public-master" because in a JCR-cluster just publish in master
         * If no "public-master" then publish in all the instances
         */
        final ServiceInstance serviceInstance = serviceInstances.get(0);
        receiver.setUrl(scheme + serviceInstance.getHost() + ":" + serviceInstance.getPort() + context);
    }
}
