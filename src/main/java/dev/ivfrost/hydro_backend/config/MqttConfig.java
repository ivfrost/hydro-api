package dev.ivfrost.hydro_backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

  @Value("${mqtt.broker.url}")
  private String mqttBrokerUrl;
  @Value("${api.mqtt.username}")
  private String mqttUsername;
  @Value("${api.mqtt.password}")
  private String mqttPassword;

  @Bean
  public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[]{mqttBrokerUrl});
    options.setCleanSession(true);
    options.setUserName(mqttUsername);
    options.setPassword(mqttPassword.toCharArray());
    factory.setConnectionOptions(options);
    return factory;
  }

}

