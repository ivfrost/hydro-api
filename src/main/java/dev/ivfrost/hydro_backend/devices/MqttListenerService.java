package dev.ivfrost.hydro_backend.devices;

import dev.ivfrost.hydro_backend.devices.internal.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
class MqttListenerService {

  private final DeviceService deviceService;

  @Value("${mqtt.broker.url}")
  private String mqttBrokerUrl;
  @Value("${mqtt.client.id}")
  private String mqttClientId;
  @Value("${mqtt.topic.wildcard}")
  private String mqttTopicWildcard;
  @Value("${mqtt.username}")
  private String mqttUsername;
  @Value("${mqtt.password}")
  private String mqttPassword;

  private MqttClient client;

  @Async
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    try {
      client = new MqttClient(mqttBrokerUrl, mqttClientId);
      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(true);
      options.setUserName(mqttUsername);
      options.setPassword(mqttPassword.toCharArray());
      options.setAutomaticReconnect(true);

      client.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
          try {
            String[] tokens = topic.split("/");
            String key = tokens[2];
            log.debug("Received MQTT message from device {}: {}", key, new String(message.getPayload()));
            deviceService.updateLastSeen(key);
          } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
            e.printStackTrace();
          }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
      });

      client.connect(options);
      client.subscribe(mqttTopicWildcard);

    } catch (MqttException e) {
      System.err.println("Error al conectar el cliente MQTT: " + e.getMessage());
    }
  }
}