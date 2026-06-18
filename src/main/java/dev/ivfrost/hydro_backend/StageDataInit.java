package dev.ivfrost.hydro_backend;

import dev.ivfrost.hydro_backend.devices.internal.Device;
import dev.ivfrost.hydro_backend.devices.internal.DeviceRepository;
import dev.ivfrost.hydro_backend.devices.internal.SecretHashUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("stage")
@RequiredArgsConstructor
@Component
public class StageDataInit implements ApplicationRunner {

  private final DeviceRepository deviceRepository;

  @Override
  public void run(@NonNull ApplicationArguments args) {
    if (deviceRepository.findByKey("HYDRO-AE70F").isEmpty()) {
      deviceRepository.save(Device.builder()
          .key("HYDRO-AE70F")
          .macAddress("00:11:22:33:44:55")
          .firmware("1.0.0")
          .secretHash(SecretHashUtil.hash("a9fe0a11f0e3c57f8bf59ca78d25599d"))
          .technicalName("hydrolink-esp32")
          .friendlyName("Greenhouse Irrigation Controller")
          .location("Greenhouse")
          .build());
    }
    if (deviceRepository.findByKey("HYDRO-BB80G").isEmpty()) {
      deviceRepository.save(Device.builder()
          .key("HYDRO-BB80G")
          .macAddress("66:77:88:99:AA:BB")
          .firmware("1.0.0")
          .secretHash(SecretHashUtil.hash("b8fe0a11f0e3c57f8bf59ca78d25599d"))
          .technicalName("hydrolink-esp32")
          .friendlyName("Garden Irrigation Controller")
          .location("Garden")
          .build());
    }
  }
}
