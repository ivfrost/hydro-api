package dev.ivfrost.hydro_backend.devices;

import java.util.List;

public interface DeviceTopicProvider {

  List<String> getTopicsForUser(Long userId);

}
