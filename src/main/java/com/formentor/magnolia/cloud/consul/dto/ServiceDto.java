package com.formentor.magnolia.cloud.consul.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ServiceDto {

    private String ID;
    private String name;
    private List<String> tags = null;
    private String address;
    private Integer port;
    private Boolean enableTagOverride;
    private Check check;
    private Weights weights;

    @Data
    @NoArgsConstructor
    public static class Check {
        private String deregisterCriticalServiceAfter;
        private List<String> args = null;
        private String HTTP;
        private String interval;
        private String TTL;
    }

    @Data
    @NoArgsConstructor
    public static class Weights {
        private Integer passing;
        private Integer warning;

    }

}

