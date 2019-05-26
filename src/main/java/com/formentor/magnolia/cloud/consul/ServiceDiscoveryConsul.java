package com.formentor.magnolia.cloud.consul;

import com.formentor.magnolia.cloud.CloudService;
import com.formentor.magnolia.cloud.consul.dto.ServiceDto;
import com.formentor.magnolia.cloud.service.ServiceDiscovery;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rest.client.registry.RestClientRegistry;
import info.magnolia.resteasy.client.RestEasyClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;

@Slf4j
public abstract class ServiceDiscoveryConsul implements ServiceDiscovery {

    private final String SERVICE_ID = "magnolia-author";

    private final RestClientRegistry restClientRegistry;

    @Inject
    public ServiceDiscoveryConsul(RestClientRegistry restClientRegistry) {
        this.restClientRegistry = restClientRegistry;

    }

    @Override
    public boolean registerService() {
        ConsulAPIClient consulAPIClient = getConsulAPIClient();

        if (consulAPIClient == null) {
            return false;
        }

        // Builds the service registration request
        ServiceDto serviceDto = new ServiceDto();

        /*
        {
          "ID": "magnolia-public-ha1",
          "Name": "magnolia-public-ha",
          "Tags": [
            "magnolia-public-ha"
          ],
          "Port": 8083,
          "Meta": {
            "bundle_version": "1.0-SNAPSHOT"
          },
          "EnableTagOverride": false,
          "Check": {
            "args": ["curl", "http://localhost:8083/magnolia-public/health"],
            "interval": "10s"
          },
          "Weights": {
            "Passing": 10,
            "Warning": 1
          }
        }
         */
        log.info("CloudService - Registering {}", SERVICE_ID);
        serviceDto.setID(SERVICE_ID);
        serviceDto.setName("magnolia-author-ha");
        serviceDto.setTags(new ArrayList<>());
        serviceDto.getTags().add("magnolia-author-ha");
        serviceDto.setPort(8080);
        serviceDto.setEnableTagOverride(false);
        ServiceDto.Check check = new ServiceDto.Check();
        check.setArgs(new ArrayList<>());
        check.getArgs().add("curl");
        check.getArgs().add("-u superuser:superuser");
        check.getArgs().add("http://localhost:8080/.rest/health");
        check.setInterval("10s");
        serviceDto.setCheck(check);
        ServiceDto.Weights weights = new ServiceDto.Weights();
        weights.setPassing(10);
        weights.setWarning(1);
        serviceDto.setWeights(weights);

        try {
            consulAPIClient.register(serviceDto);
            log.info("CloudService - Service {} registered", SERVICE_ID);
            return true;
        } catch (Exception e) {
            log.error("Errors during register of service {} in Consul", serviceDto, e);
            return false;
        }
    }

    @Override
    public boolean deRegisterService() {
        log.info("CloudService - deRegistering {}", SERVICE_ID);
        ConsulAPIClient consulAPIClient = getConsulAPIClient();
        if (consulAPIClient == null) {
            return false;
        }
        try {
            consulAPIClient.deRegister(SERVICE_ID);
            log.info("CloudService - Service {} registered", SERVICE_ID);
            return true;
        } catch (Exception e) {
            log.error("Errors during deregister of service {} in Consul", SERVICE_ID, e);
        }
        return false;
    }

    /**
     * Get Rest client
     * @return
     */
    private ConsulAPIClient getConsulAPIClient() {
        RestEasyClient client;
        ConsulAPIClient consulAPIClient;
        try {
            client = (RestEasyClient) restClientRegistry.getRestClient(CloudService.REST_CLIENT);
            consulAPIClient = client.getClientService(ConsulAPIClient.class);
        } catch (RegistrationException e) {
            log.error("Errors getting Rest Client {}", CloudService.REST_CLIENT, e);
            return null;
        }

        return consulAPIClient;
    }
}
