package dev.ivfrost.hydro_backend.devices.internal;

import dev.ivfrost.hydro_backend.devices.DeviceAuthRequest;
import dev.ivfrost.hydro_backend.devices.DeviceFetchException;
import dev.ivfrost.hydro_backend.devices.DeviceLinkException;
import dev.ivfrost.hydro_backend.devices.DeviceLinkRequest;
import dev.ivfrost.hydro_backend.devices.DeviceLoadEvent;
import dev.ivfrost.hydro_backend.devices.DeviceNotFoundException;
import dev.ivfrost.hydro_backend.devices.DeviceProvisionRequest;
import dev.ivfrost.hydro_backend.devices.DeviceProvisionResponse;
import dev.ivfrost.hydro_backend.devices.DeviceResponse;
import dev.ivfrost.hydro_backend.devices.DeviceTokenProvider;
import dev.ivfrost.hydro_backend.devices.DeviceUpdateRequest;
import dev.ivfrost.hydro_backend.devices.DuplicateMacAddressException;
import dev.ivfrost.hydro_backend.tokens.DeviceMqttTokenPayload;
import dev.ivfrost.hydro_backend.tokens.DeviceTokenResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final DeviceCacheService deviceCacheService;
  private final DeviceTokenProvider deviceTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * Provisions a new device and generates a secret for ownership verification.
   *
   * @param req the device provision request DTO
   * @return the provisioned device response DTO
   * @throws DuplicateMacAddressException if a device with the same MAC address already exists
   */
  @CacheEvict(value = "allDevicesCache", allEntries = true)
  @Transactional
  public DeviceProvisionResponse provisionDevice(DeviceProvisionRequest req) {

    if (deviceRepository.existsByMacAddress(req.macAddress())) {
      throw new DuplicateMacAddressException(req.macAddress());
    }

    Device device = convertRequestToDevice(req);

    // Generate, hash and set device secret
    String rawSecret = RandomStringUtils.secure().nextAlphanumeric(32);
    String hashed = SecretHashUtil.hash(rawSecret);
    device.setSecretHash(hashed);

    // Save device
    Device saved = deviceRepository.save(device);

    // Return device details along with the raw secret
    return DeviceUtil.convertProvisionDeviceToResponse(saved, rawSecret);
  }


  /**
   * Links an unlinked device to a user using the device secret as ownership proof
   *
   * @param req the device link request DTO (contains device secret)
   * @throws DeviceLinkException     if the device is already linked
   * @throws DeviceNotFoundException if the device is not found
   */
  @CacheEvict(value = "allDevicesCache", allEntries = true)
  @Transactional
  public void linkDevice(DeviceLinkRequest req, Long userId, boolean unlink) {

    String rawSecret = req.secret();

    // Fetch only devices that are not linked yet
    List<Device> candidates = deviceRepository.findAllByUserIdIsNull();

    // Find the device whose hash matches the raw secret
    Device device = candidates.stream()
        .filter(d -> SecretHashUtil.matches(rawSecret, d.getSecretHash()))
        .findFirst()
        .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

    if (!unlink) {
      // Linking
      if (device.getUserId() != null) {
        throw new DeviceLinkException("Device is already linked to a user");
      }

      device.setUserId(userId);
      device.setLinkedAt(Instant.now());
      device.setDisplayOrder(calculateDeviceOrder(userId));
      deviceRepository.save(device);

    } else {
      // Unlinking
      if (device.getUserId() == null || !Objects.equals(device.getUserId(), userId)) {
        throw new DeviceLinkException("Device is not linked to this user");
      }

      device.setUserId(null);
      device.setDisplayOrder(0L);
      deviceRepository.save(device);
    }
  }

  /**
   * Verify device ownership
   *
   * @param userId   the user to verify ownership against
   * @param deviceId the ID of the device to verify
   * @throws DeviceNotFoundException  if the device is not found
   * @throws IllegalArgumentException if the device does not belong to the specified user
   */
  public void verifyDeviceOwnership(Long userId, Long deviceId) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    if (!Objects.equals(device.getUserId(), userId)) {
      throw new IllegalArgumentException("Device does not belong to the specified user");
    }
  }

  /**
   * Retrieves devices owned by a specific user, by user ID (Admin only).
   *
   * @param userId the ID of the user whose devices are to be retrieved
   * @return a list of device response DTOs
   * @throws DeviceFetchException if no devices are found for the user
   */
  public List<DeviceResponse> getDevicesByUserId(Long userId) {
    List<Device> devices = deviceRepository.findAllByUserId(userId);
    log.debug("Fetched {} devices for user ID {}", devices.size(), userId);

    if (devices.isEmpty()) {
      throw new DeviceFetchException("No devices found for user");
    }
    return devices
        .stream()
        .map(DeviceUtil::convertDeviceToResponse)
        .sorted(Comparator.comparing(DeviceResponse::order))
        .toList();
  }

  /**
   * Retrieves all devices provisioned in the system (Admin only, paginated).
   *
   * @return a list of all device response DTOs
   * @throws DeviceFetchException if no devices are found
   */
  public Page<DeviceResponse> getAllDevices(Pageable pageable) {
    Page<Device> devices = deviceCacheService.getAllDevices(pageable);
    if (devices.isEmpty()) {
      throw new DeviceFetchException("No devices found in the system");
    }
    return devices.map(DeviceUtil::convertDeviceToResponse);
  }

  /**
   * Updates the friendly friendlyName of a specific device by its ID.
   */
  @Transactional
  public DeviceResponse updateDeviceFriendlyName(long deviceId, DeviceUpdateRequest req) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    verifyDeviceOwnership(req.userId(), deviceId);
    device.setFriendlyName(req.friendlyName());
    return DeviceUtil.convertDeviceToResponse(deviceRepository.save(device));
  }


  /**
   * Updates fields of a specific device by its ID.
   *
   * @param req the device update request DTO
   * @return the updated device response DTO
   * @throws DeviceNotFoundException  if the device is not found
   * @throws IllegalArgumentException if the device does not belong to the user
   */
  public DeviceResponse updateDeviceDetails(long deviceId, DeviceUpdateRequest req) {
    Device device = deviceRepository.findById(deviceId).orElseThrow(
        () -> new DeviceNotFoundException(deviceId));

    // Verify ownership
    if (!Objects.equals(device.getUserId(), req.userId())) {
      throw new IllegalArgumentException("Device does not belong to the user");
    }
    String technicalName = req.technicalName();
    String firmware = req.firmware();
    String name = req.friendlyName();

    if (technicalName != null && !technicalName.isEmpty()) {
      device.setTechnicalName(technicalName);
    }
    if (firmware != null && !firmware.isEmpty()) {
      device.setFirmware(firmware);
    }
    if (name != null && !name.isEmpty()) {
      device.setFriendlyName(name);
    }

    return DeviceUtil.convertDeviceToResponse(deviceRepository.save(device));
  }

  /**
   * Delete a device by its ID (Admin only).
   *
   * @param deviceId the ID of the device to delete
   * @throws DeviceNotFoundException if the device is not found
   */
  public void deleteDeviceById(Long deviceId) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    deviceRepository.delete(device);
  }

  /**
   * Updates the last seen timestamp of a device to the current time. This should be called whenever
   * the device interacts with the system (e.g., on MQTT connection, API request, etc.) to keep
   * track of active devices.
   *
   * @param deviceId the ID of the device to update
   * @throws DeviceNotFoundException if the device is not found
   */
  public void updateLastSeen(Long deviceId) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new DeviceNotFoundException(deviceId));

    device.setLastSeen(Instant.now());
    deviceRepository.save(device);
  }


  /**
   * Updates the live order of devices for a user in Redis. This is used to maintain the display
   * order of devices in the UI without hitting the database on every change.
   *
   * @param deviceIds the list of device IDs in the new order
   */
  public void updateLiveOrder(Long userId, List<Long> deviceIds) {
    String key = "device_order:" + userId;

    // Clear existing list
    redisTemplate.delete(key);

    // Push new order
    redisTemplate.opsForList().rightPushAll(
        key,
        deviceIds.stream().map(String::valueOf).toList()
    );
  }

  /**
   * Persists the live device order from Redis to the database. This should be called when the user
   * explicitly saves their device order or when they abandon the tab/page to ensure the order is
   * not lost.
   *
   * @param userId the ID of the user whose device order is being persisted
   */
  public void persistDeviceOrder(Long userId) {
    String key = "device_order:" + userId;
    List<String> deviceIdStrings = redisTemplate.opsForList().range(key, 0, -1);
    if (deviceIdStrings == null || deviceIdStrings.isEmpty()) {
      return; // No order to persist
    }

    List<Long> deviceIds = deviceIdStrings.stream()
        .map(Long::valueOf)
        .toList();

    for (int i = 0; i < deviceIds.size(); i++) {
      Long deviceId = deviceIds.get(i);
      Device device = deviceRepository.findById(deviceId)
          .orElseThrow(() -> new DeviceNotFoundException(deviceId));
      if (Objects.equals(device.getUserId(), userId)) {
        device.setDisplayOrder((long) (i + 1));
        deviceRepository.save(device);
      }
    }
  }

  /**
   * Authenticates a device and returns an MQTT JWT token. Device publishes to the topic:
   * hydro/{device-secret}/*
   */
  public DeviceTokenResponse getMqttAuthToken(DeviceAuthRequest req) {
    // Load device by ID and verify secret matches
    Device device = deviceRepository.findById(req.deviceId())
        .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

    // Verify the provided secret matches the stored hash
    if (!SecretHashUtil.matches(req.secret(), device.getSecretHash())) {
      throw new DeviceNotFoundException("Device secret does not match");
    }

    return deviceTokenProvider.generateMqttToken(new DeviceMqttTokenPayload(
        device.getId(),
        device.getSecretHash()
    ));
  }

  /*--------------------------*/
  /* Helper Methods */
  /*--------------------------*/

  /**
   * Converts a DeviceProvisionRequest DTO to a Device entity.
   *
   * @param req the device provision request DTO
   * @return the device entity
   */
  private Device convertRequestToDevice(DeviceProvisionRequest req) {
    Device device = new Device();
    device.setTechnicalName(req.technicalName());
    device.setFirmware(req.firmware());
    device.setMacAddress(req.macAddress());
    return device;
  }

  /**
   * Calculates the next display order for a user's devices.
   *
   * @param userId the user whose devices are being ordered
   * @return the next display order
   */
  private long calculateDeviceOrder(Long userId) {
    List<Device> devices = deviceCacheService.getDevicesByUserId(userId);
    return devices.stream()
        .map(Device::getDisplayOrder)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder())
        .map(maxOrder -> maxOrder + 1)
        .orElse(1L);
  }

  /**
   * Retrieves a device by its ID.
   *
   * @param deviceId the ID of the device to retrieve
   * @return the device entity
   * @throws DeviceNotFoundException if the device is not found
   */
  public Device getDeviceById(Long deviceId) {
    return deviceCacheService.getDeviceById(deviceId);
  }

  /**
   * Regenerates a device's secret.
   *
   * @param deviceId the ID of the device
   * @return the new secret in raw form (not hashed)
   * @throws DeviceNotFoundException if the device is not found
   */
  @Transactional
  public String regenerateDeviceSecret(Long deviceId) {
    Device device = getDeviceById(deviceId);
    String rawSecret = RandomStringUtils.secure().nextAlphanumeric(32);
    String hashed = SecretHashUtil.hash(rawSecret);
    device.setSecretHash(hashed);
    return rawSecret;
  }

  /**
   * Retrieves the secret for a device by its ID (decrypted). The secret is automatically decrypted
   * by the JPA converter.
   *
   * @param deviceId the ID of the device
   * @return the device's secret (decrypted)
   * @throws DeviceNotFoundException if the device is not found
   */
  public String getSecretByDeviceId(Long deviceId) {
    Device device = getDeviceById(deviceId);
    return device.getSecretHash();
  }

  /**
   * Builds the list of MQTT topics the current user can access based on their devices.
   *
   * @return the list of MQTT topics
   */
  public List<String> getUserDeviceTopics(Long userId) {
    List<Device> devices = deviceRepository.findAllByUserId(userId);
    return devices.stream()
        .map(device -> "hydro/" + userId + "/" + device.getKey() + "/#")
        .toList();
  }

  @ApplicationModuleListener
  public void on(DeviceLoadEvent e) {
    getDevicesByUserId(e.userId());
    log.info("Loaded devices for user ID {} into cache", e.userId());
  }
}


/**
 * Service for caching device queries using Spring Cache abstraction. Reduces database load for
 * frequently accessed device data.
 */
@Slf4j
@AllArgsConstructor
@Service
class DeviceCacheService {

  private final DeviceRepository deviceRepository;

  /**
   * Retrieves a device by its ID from cache. Cache is invalidated when the device is updated.
   *
   * @param deviceId the ID of the device to retrieve
   * @return the device if found
   * @throws DeviceNotFoundException if the device is not found
   */
  @Cacheable(
      value = "deviceByIdCache",
      key = "#deviceId"
  )
  public Device getDeviceById(Long deviceId) {
    return deviceRepository.findById(deviceId)
        .orElseThrow(() -> new DeviceNotFoundException(deviceId));
  }


  /**
   * Retrieves all devices for a specific user from cache
   *
   * @param userId the ID of the user whose devices are to be retrieved
   * @return list of devices owned by the user
   */
  @Cacheable(
      value = "devicesByUserIdCache",
      key = "#userId"
  )
  public List<Device> getDevicesByUserId(Long userId) {
    return deviceRepository.findAllByUserId(userId);
  }

  /**
   * Retrieves all devices in the system from cache, with pagination
   *
   * @return list of all devices
   */
  @Cacheable(
      value = "allDevicesCache",
      key = "#pageable.pageNumber + '-' + #pageable.pageSize"
  )
  public Page<Device> getAllDevices(Pageable pageable) {
    return deviceRepository.findAll(pageable);
  }

}