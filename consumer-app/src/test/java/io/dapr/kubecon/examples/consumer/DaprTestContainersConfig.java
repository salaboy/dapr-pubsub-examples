package io.dapr.kubecon.examples.consumer;

import io.dapr.testcontainers.Component;
import io.dapr.testcontainers.DaprContainer;
import io.dapr.testcontainers.DaprLogLevel;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class DaprTestContainersConfig {

  @Bean
  public Network getDaprNetwork() {
    Network defaultDaprNetwork = new Network() {
      @Override
      public String getId() {
        return "dapr-network";
      }

      @Override
      public void close() {

      }

      @Override
      public Statement apply(Statement base, Description description) {
        return null;
      }
    };

    List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter("dapr-network").exec();
    if (networks.isEmpty()) {
      Network.builder()
              .createNetworkCmdModifier(cmd -> cmd.withName("dapr-network"))
              .build().getId();
      return defaultDaprNetwork;
    } else {
      return defaultDaprNetwork;
    }
  }

   @Bean
   public RabbitMQContainer rabbitMQContainer(Network daprNetwork){
      return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"))
              .withExposedPorts(5672)
              .withNetworkAliases("rabbitmq")
              //.withReuse(true) //comment out to run the tests
              .withNetwork(daprNetwork);

   }

   @Bean
   @ServiceConnection
   public DaprContainer daprContainer(Network daprNetwork, RabbitMQContainer rabbitMQContainer){

     Map<String, String> rabbitMqProperties = new HashMap<>();
     rabbitMqProperties.put("connectionString", "amqp://guest:guest@rabbitmq:5672");
     rabbitMqProperties.put("user", "guest");
     rabbitMqProperties.put("password", "guest");


     return new DaprContainer("daprio/daprd:1.14.1")
             .withAppName("consumer-app-dapr")
             .withNetwork(daprNetwork)
             .withComponent(new Component("pubsub", "pubsub.rabbitmq", "v1", rabbitMqProperties))
             .withDaprLogLevel(DaprLogLevel.DEBUG)
             .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
             .withAppPort(8081)
             //.withReusablePlacement(true) //comment out to run the tests
             .withAppChannelAddress("host.testcontainers.internal")
             .dependsOn(rabbitMQContainer);
   }




}
