package io.dapr.kubecon.examples.consumer;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SubscriberRestController {

  private List<CloudEvent> events = new ArrayList<>();

  @PostMapping("subscribe")
  @Topic(pubsubName = "pubsub", name = "topic")
  public void subscribe(@RequestBody CloudEvent<DeviceEvent> cloudEvent){
    System.out.println("CONSUME +++++ " + cloudEvent);
    System.out.println("DATA +++++ " + cloudEvent.getData());
    events.add(cloudEvent);
  }

  @GetMapping("events")
  public List<CloudEvent> getAllEvents() {
    return events;
  }

  @PostMapping("info")
  public InfoRequest info(@RequestBody InfoRequest request){
    request.setContent(request.getContent()+"-validated");
    System.out.println("INFO REQUEST: " + request);
    return request;
  }

}

