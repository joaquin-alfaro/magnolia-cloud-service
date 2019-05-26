package com.formentor.magnolia.cloud.consul;

import com.ecwid.consul.v1.agent.model.NewService;
import com.formentor.magnolia.cloud.CloudService;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.init.MagnoliaConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Slf4j
public class ConsulRegistration {

    private static final String BASIC_AUTH_HEADER= "Authorization"; // Basic auth header name
    public static final char SEPARATOR = '-'; // Instance ID separator.

    public static NewService createServiceRegistration(CloudService definition, MagnoliaConfigurationProperties magnoliaConfigurationProperties, ServerConfiguration serverConfiguration) {
        NewService newService = new NewService();

        newService.setId(getInstanceId(magnoliaConfigurationProperties));

        String appName = getAppName(magnoliaConfigurationProperties);
        newService.setName(normalizeForDns(appName));

        newService.setTags(createTags(serverConfiguration, magnoliaConfigurationProperties));
        newService.setEnableTagOverride(false);
        newService.setPort(getPort());
        setCheck(newService, magnoliaConfigurationProperties.getProperty(MagnoliaConfigurationProperties.MAGNOLIA_SERVERNAME), magnoliaConfigurationProperties.getProperty(MagnoliaConfigurationProperties.MAGNOLIA_WEBAPP));

        return newService;
    }

    /**
     * Returns the id of magnolia service in Consul
     *
     * servername:appname:port
     *
     * @param properties
     * @return
     */
    private static String getInstanceId(MagnoliaConfigurationProperties properties) {
        final String appName = getAppName(properties);
        final String instanceId = properties.getProperty(MagnoliaConfigurationProperties.MAGNOLIA_SERVERNAME) + ":" + appName + ":" + getPort();
        return normalizeForDns(instanceId);
    }

    /**
     * Returns the app name
     *
     * 1st the value of property magnolia.cloud.appName
     * 2nd the name of webapp with prefix "magnolia-"
     *
     * @param properties
     * @return
     */
    private static String getAppName(MagnoliaConfigurationProperties properties) {
        final String appName = properties.getProperty(CloudService.MAGNOLIA_APP_NAME);
        if (!StringUtils.isBlank(appName)) {
            return appName;
        }
        return "magnolia-" + properties.getProperty(MagnoliaConfigurationProperties.MAGNOLIA_WEBAPP);
    }

    private static String normalizeForDns(String s) {
        if (s == null || !Character.isLetter(s.charAt(0))
                || !Character.isLetterOrDigit(s.charAt(s.length() - 1))) {
            throw new IllegalArgumentException(
                    "Consul service ids must not be empty, must start "
                            + "with a letter, end with a letter or digit, "
                            + "and have as interior characters only letters, "
                            + "digits, and hyphen: " + s);
        }

        StringBuilder normalized = new StringBuilder();
        Character prev = null;
        for (char curr : s.toCharArray()) {
            Character toAppend = null;
            if (Character.isLetterOrDigit(curr)) {
                toAppend = curr;
            }
            else if (prev == null || !(prev == SEPARATOR)) {
                toAppend = SEPARATOR;
            }
            if (toAppend != null) {
                normalized.append(toAppend);
                prev = toAppend;
            }
        }

        return normalized.toString();
    }

    /**
     * Create list of tags
     * @param serverConfiguration
     * @return
     */
    private static List<String> createTags(ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        List<String> tags = new LinkedList<>();
        // store the isAdmin flag in the tags so that clients will be able to figure out they are accessing to "authoring"
        tags.add("magnolia.authorInstance=" + serverConfiguration.isAdmin());
        tags.add("magnolia.develop=" + magnoliaConfigurationProperties.getBooleanProperty("magnolia.develop"));
        tags.add("webapp=" + magnoliaConfigurationProperties.getProperty(MagnoliaConfigurationProperties.MAGNOLIA_WEBAPP));

        return tags;
    }

    private static void setCheck(NewService service, String hostname, String webapp) {
        if (service.getCheck() == null) {
            Integer checkPort;
            checkPort = service.getPort();
            service.setCheck(createCheck(checkPort, hostname, webapp));
        }
    }

    /**
     * Create service check
     *
     * @param port
     * @param servername
     * @param webapp
     * @return
     */
    private static NewService.Check createCheck(Integer port,
                                                String servername,
                                                String webapp) {

        NewService.Check check = new NewService.Check();
        /**
         * TODO configure DeregisterCriticalServiceAfter in module definition
         */
        check.setDeregisterCriticalServiceAfter("90m");

        String healthCheckPath = "/.rest/health";
        if (webapp != null) healthCheckPath = "/" + webapp + healthCheckPath;

        // Use IP instead of servername to avoid problems DNS
        String hostname = getIpAddress();

        check.setHttp(String.format("%s://%s:%s%s", "http",
                hostname, port, healthCheckPath));
        check.setHeader(createBasicAuthHeader("superuser", "superuser"));
        check.setInterval("10s");

        check.setTlsSkipVerify(true);
        return check;
    }

    /**
     * Returns basic authentication header
     *
     * @param username
     * @param password
     * @return
     */
    private static Map<String, List<String>> createBasicAuthHeader(String username, String password) {
        HashMap<String, List<String>> header = new HashMap<>();
        header.put(BASIC_AUTH_HEADER, new ArrayList<>(Arrays.asList("Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes())))));
        return header;
    }

    /**
     * Returns de host IP
     * @return
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws UnknownHostException
     */
    private static String getIpAddress() {
        try {
            final String host = InetAddress.getLocalHost().getHostAddress();
            return host;
        } catch (UnknownHostException e) {
            log.error("Errors getting the host IP", e);
        }
        return null;
    }

    /**
     * Returns the Port of the service
     *
     * TODO It would be nice if this method was located in the class InitPathsPropertySource of Magnolia
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws UnknownHostException
     */
    private static Integer getPort() {
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = null;
            objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            final Integer port = Integer.valueOf(objectNames.iterator().next().getKeyProperty("port"));

            return port;
        } catch (MalformedObjectNameException e) {
            log.error("Errors getting Port", e);
        }

        return null;
    }

}
