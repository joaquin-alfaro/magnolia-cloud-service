package com.formentor.magnolia.cloud.consul;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.formentor.magnolia.cloud.CloudService;
import com.formentor.magnolia.cloud.service.ServiceDiscovery;
import com.formentor.magnolia.cloud.service.ServiceInstance;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.init.MagnoliaConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Slf4j
public class ConsulServiceDiscovery implements ServiceDiscovery<NewService> {

    private final ConsulClient client;
    /**
     * Not stored in Module definition to allow the integration with others service discovery.
     */
    private final NewService serviceConfig;


    @Inject
    public ConsulServiceDiscovery(CloudService definition, MagnoliaConfigurationProperties magnoliaConfigurationProperties, ServerConfiguration serverConfiguration) {
        this.client = createConsulClient(definition);
        this.serviceConfig = ConsulRegistration.createServiceRegistration(definition, magnoliaConfigurationProperties, serverConfiguration);
    }

    @Override
    public boolean registerService() {
        return registerService(this.serviceConfig);
    }

    @Override
    public boolean deRegisterService() {
        return deRegisterService(this.serviceConfig);
    }

    @Override
    public boolean registerService(NewService service) {
        if (client == null) {
            log.warn("Error registering service with consul: "
                    + service, new RuntimeException("Consul client is null"));
            return false;
        }

        // Register the service in Consul
        client.agentServiceRegister(service);

        log.info("Registering service with consul: " + service);
        try {
            this.client.agentServiceRegister(service);
            return true;
        }
        catch (ConsulException e) {
            log.warn("Error registering service with consul: "
                    + service, e);
        }

        return false;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {

        List<ServiceInstance> instances = new ArrayList<>();
        /**
         * TODO
         *
         * Add .setTag() to the builder to filter magnolia of type "master" to avoid publishing in "slave" node
         */
        HealthServicesRequest healthServicesRequest = HealthServicesRequest.newBuilder().build();

        Response<List<HealthService>> services;
        services = this.client.getHealthServices(serviceName, healthServicesRequest);
        for (HealthService service : services.getValue()) {
            final String host = findHost(service);

            instances.add(ServiceInstance.builder()
                    .instanceId(service.getService().getId())
                    .serviceId(serviceName)
                    .host(host)
                    .port(service.getService().getPort())
                    .metadata(getMetadata(service))
                    .build());
        }

        return instances;
    }

    @Override
    public boolean deRegisterService(NewService service) {
        if (client == null) {
            log.warn("Error deregistering service with consul: "
                    + service, new RuntimeException("Consul client is null"));
            return false;
        }

        // Register the service in Consul
        client.agentServiceRegister(service);

        log.info("Deregistering service with consul: " + service);
        try {
            this.client.agentServiceDeregister(service.getId());
            return true;
        }
        catch (ConsulException e) {
            log.warn("Error deregistering service with consul: "
                    + service, e);
        }

        return false;
    }

    /**
     * Create client with consul agent
     * @param definition
     * @return
     */
    private ConsulClient createConsulClient(CloudService definition) {
        if (StringUtils.isBlank(definition.getHost()) || definition.getPort() == null) {
            return null;
        }

        final int agentPort = definition.getPort();
        final String agentHost = !StringUtils.isEmpty(definition.getScheme())
                ? definition.getScheme() + "://" + definition.getHost()
                : definition.getHost();

        /*
        if (consulProperties.getTls() != null) {
            ConsulProperties.TLSConfig tls = consulProperties.getTls();
            TLSConfig tlsConfig = new TLSConfig(tls.getKeyStoreInstanceType(),
                    tls.getCertificatePath(), tls.getCertificatePassword(),
                    tls.getKeyStorePath(), tls.getKeyStorePassword());
            return new ConsulClient(agentHost, agentPort, tlsConfig);
        }
        */
        return new ConsulClient(agentHost, agentPort);
    }

    private static String findHost(HealthService healthService) {
        HealthService.Service service = healthService.getService();
        HealthService.Node node = healthService.getNode();

        if (!StringUtils.isBlank(service.getAddress())) {
            return fixIPv6Address(service.getAddress());
        }
        else if (!StringUtils.isBlank(node.getAddress())) {
            return fixIPv6Address(node.getAddress());
        }
        return node.getNode();
    }

    private static String fixIPv6Address(String address) {
        try {
            InetAddress inetAdr = InetAddress.getByName(address);
            if (inetAdr instanceof Inet6Address) {
                return "[" + inetAdr.getHostName() + "]";
            }
            return address;
        }
        catch (UnknownHostException e) {
            log.debug("Not InetAddress: " + address + " , resolved as is.");
            return address;
        }
    }

    private static Map<String, String> getMetadata(HealthService healthService) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        List<String> tags = healthService.getService().getTags();
        if (tags != null) {
            for (String tag : tags) {
                String[] parts = tag.split("=");
                switch (parts.length) {
                    case 0:
                        break;
                    case 1:
                        metadata.put(parts[0], parts[0]);
                        break;
                    case 2:
                        metadata.put(parts[0], parts[1]);
                        break;
                    default:
                        String[] end = Arrays.copyOfRange(parts, 1, parts.length);
                        metadata.put(parts[0], end.toString());
                        break;
                }

            }
        }

        return metadata;
    }

}
