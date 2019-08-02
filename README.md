# Magnolia Cloud Service - Service Discovery

Registers magnolia instances in Consul Service Discovery 

## Features
Exposes endpoint GET /.rest/health as health indicator of magnolia

~~~~
{
    "status": "UP"
}
~~~~
Registers magnolia instance as a service in Consul service discovery

![Consul services](_dev/consul-services.png)

Set of endpoints to register and deregister magnolia in Consul  
`POST /.rest/magnolia-cloud-service/register`
`POST /.rest/magnolia-cloud-service/deregister`

Configuration of *receivers* in *publishing module* with *service name* instead of the IP or DNS of  magnolia public instances. Magnolia public instances register in Consul with a *service name* and consul translates *service name* to the IP of the server where magnolia is deployed.
> If the domain or IP is not registered in Consul a *service* then it works as usual  

![Example of configuration](_dev/publishing-core-receivers.png)

Integration with [Consul](https://www.consul.io/ "Consul") v1.4.3

## Usage
The java interface com.formentor.magnolia.cloud.ServiceDiscovery specifies the implementation of the service to register magnolia in a Service Discovery system

~~~~
public interface ServiceDiscovery<T> {
    boolean registerService();
    boolean deRegisterService();
    boolean deRegisterService(T service);
    boolean registerService(T service);
    List<ServiceInstance> getInstances(String serviceName);
}
~~~~
It can be used any Service Discovery using the below interface.

#### Configuration  
**Name of the service**  

The property *magnolia.cloud.appName* of magnolia specifies the name of the service registered in Service Discover. The same name can be use by magnolia instances that have the same purpose. For example *magnolia-author* for the author instance and *magnolia-public* for public instances.   

~~~~
# Name used as a cloud service
magnolia.cloud.appName=magnolia-public-by-formentor
~~~~

**Consul configuration**  
Consul is configured in the configuration of *magnolia-cloud-service* module  
![Configuración consul](_dev/consul-config.png)
 
**Receiver is Publishing core**  
The module implements a custom *DefaultSender* that translates the domain of the url of receivers to the real IP.
If the domain is not registered as a service in Consul then uses the url.
![Example of configuration](_dev/publishing-core-receivers.png)

## Demo
The module includes a bootstrap that configures a Consul deployed in localhost.
Steps to use this module

1- Install consul following the guide of hashicorp (it is really simple) https://learn.hashicorp.com/consul/getting-started/install

2- Start Consul
~~~~
$ consul agent -server -bootstrap-expect=1 -data-dir=./consul-server-data -node=sever-one -domain domain_name -bind 127.0.0.1 -enable-script-checks=true -ui
~~~~

3- Add the dependency of this module to a magnolia bundle, build the bundle and start

4- The bundle of magnolia will register in Consul as an instance of the service with the name especified in the property *magnolia.cloud.appName*
Services can be seen using the ui of Consul  
![Servicios en Consul](_dev/consul-ui.png)

**NOTE**
>The use of service discovery is recommended when magnolia is consumed by many services and it is usual to create and destroy instances of magnolia. In this case you avoid to change the IP in every service.

## Contribute to the Magnolia component ecosystem
It's easy to create components for Magnolia and share them on github and npm. I invite you to do so and join the community. Let's stop wasting time by developing the same thing again and again, rather let's help each other out by sharing our work and create a rich library of components.

## License

MIT

## Contributors

Formentor Studio, http://formentor-studio.com/

Joaquín Alfaro, @Joaquin_Alfaro
