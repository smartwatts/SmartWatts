package com.smartwatts.edge.protocol;

import com.smartwatts.edge.model.DeviceReading;
import com.smartwatts.edge.model.DeviceCommand;
import com.smartwatts.edge.service.EdgeSecurityService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class MQTTProtocolHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MQTTProtocolHandler.class);
    
    @Value("${edge.mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    @Value("${edge.mqtt.client.id:smartwatts-edge-${random.uuid}}")
    private String clientId;
    
    @Value("${edge.mqtt.username:}")
    private String username;
    
    @Value("${edge.mqtt.password:}")
    private String password;
    
    @Value("${edge.mqtt.clean.session:true}")
    private boolean cleanSession;
    
    private MqttClient mqttClient;
    private final ConcurrentHashMap<String, Consumer<DeviceReading>> deviceCallbacks = new ConcurrentHashMap<>();
    
    // Security service for device validation
    private final EdgeSecurityService edgeSecurityService;
    
    @Autowired
    public MQTTProtocolHandler(EdgeSecurityService edgeSecurityService) {
        this.edgeSecurityService = edgeSecurityService;
    }
    
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing MQTT protocol handler...");
            
            // Create MQTT client
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            
            // Configure connection options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            
            // Set callback for incoming messages
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("MQTT connection lost", cause);
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleIncomingMessage(topic, message);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Message delivery completed
                }
            });
            
            // Connect to broker
            mqttClient.connect(options);
            logger.info("MQTT client connected to broker: {}", brokerUrl);
            
            // Subscribe to device topics
            subscribeToDeviceTopics();
            
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT protocol handler", e);
        }
    }
    
    /**
     * Subscribe to MQTT topics for device communication
     */
    private void subscribeToDeviceTopics() {
        try {
            // Subscribe to device data topics
            String[] topics = {
                "smartwatts/devices/+/data",      // Device readings
                "smartwatts/devices/+/status",    // Device status updates
                "smartwatts/devices/+/alerts"     // Device alerts
            };
            
            int[] qos = {1, 1, 2}; // Quality of Service levels
            
            mqttClient.subscribe(topics, qos);
            logger.info("Subscribed to MQTT topics: {}", String.join(", ", topics));
            
        } catch (Exception e) {
            logger.error("Failed to subscribe to MQTT topics", e);
        }
    }
    
    /**
     * Handle incoming MQTT messages
     */
    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            logger.debug("Received MQTT message on topic {}: {}", topic, payload);
            
            // Parse topic to extract device ID and message type
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 4) {
                String deviceId = topicParts[2];
                String messageType = topicParts[3];
                
                switch (messageType) {
                    case "data":
                        handleDeviceData(deviceId, payload);
                        break;
                    case "status":
                        handleDeviceStatus(deviceId, payload);
                        break;
                    case "alerts":
                        handleDeviceAlert(deviceId, payload);
                        break;
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to handle incoming MQTT message", e);
        }
    }
    
    /**
     * Handle device data messages
     */
    private void handleDeviceData(String deviceId, String payload) {
        try {
            // Security validation: Check if device can send data
            if (!edgeSecurityService.canDeviceSendData(deviceId)) {
                logger.warn("Device {} is not verified or cannot send data - blocking message", deviceId);
                edgeSecurityService.logSecurityEvent(deviceId, "MQTT_DATA_BLOCKED", 
                    "Device not verified or cannot send data", false);
                return;
            }
            
            // Extract and validate device auth secret if present
            String authSecret = edgeSecurityService.extractDeviceAuthSecret(payload);
            if (authSecret != null && !edgeSecurityService.validateDeviceAuthSecret(deviceId, authSecret)) {
                logger.warn("Device {} provided invalid auth secret - blocking message", deviceId);
                edgeSecurityService.logSecurityEvent(deviceId, "MQTT_AUTH_FAILED", 
                    "Invalid authentication secret", false);
                return;
            }
            
            // Parse device reading from JSON payload
            // This would use Jackson ObjectMapper in production
            DeviceReading reading = parseDeviceReading(payload);
            reading.setDeviceId(deviceId);
            
            // Notify registered callbacks
            Consumer<DeviceReading> callback = deviceCallbacks.get(deviceId);
            if (callback != null) {
                callback.accept(reading);
            }
            
            // Log successful data processing
            edgeSecurityService.logSecurityEvent(deviceId, "MQTT_DATA_PROCESSED", 
                "Device data processed successfully", true);
            
            logger.info("Processed device data from {}: {} kWh", deviceId, reading.getEnergyConsumption());
            
        } catch (Exception e) {
            logger.error("Failed to handle device data for {}", deviceId, e);
            edgeSecurityService.logSecurityEvent(deviceId, "MQTT_DATA_ERROR", 
                "Error processing device data: " + e.getMessage(), false);
        }
    }
    
    /**
     * Handle device status messages
     */
    private void handleDeviceStatus(String deviceId, String payload) {
        // Security validation: Check if device can send data
        if (!edgeSecurityService.canDeviceSendData(deviceId)) {
            logger.warn("Device {} is not verified - blocking status update", deviceId);
            edgeSecurityService.logSecurityEvent(deviceId, "MQTT_STATUS_BLOCKED", 
                "Device not verified - status update blocked", false);
            return;
        }
        
        logger.info("Device {} status update: {}", deviceId, payload);
        // Implement status update logic
        
        edgeSecurityService.logSecurityEvent(deviceId, "MQTT_STATUS_PROCESSED", 
            "Device status processed successfully", true);
    }
    
    /**
     * Handle device alert messages
     */
    private void handleDeviceAlert(String deviceId, String payload) {
        // Security validation: Check if device can send data
        if (!edgeSecurityService.canDeviceSendData(deviceId)) {
            logger.warn("Device {} is not verified - blocking alert", deviceId);
            edgeSecurityService.logSecurityEvent(deviceId, "MQTT_ALERT_BLOCKED", 
                "Device not verified - alert blocked", false);
            return;
        }
        
        logger.warn("Device {} alert: {}", deviceId, payload);
        // Implement alert handling logic
        
        edgeSecurityService.logSecurityEvent(deviceId, "MQTT_ALERT_PROCESSED", 
            "Device alert processed successfully", true);
    }
    
    /**
     * Send command to device via MQTT
     */
    public boolean sendCommand(String deviceId, DeviceCommand command) {
        try {
            String topic = "smartwatts/devices/" + deviceId + "/command";
            String payload = serializeCommand(command);
            
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);
            
            mqttClient.publish(topic, message);
            
            logger.info("Sent command to device {}: {}", deviceId, command.getCommand());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send command to device {}", deviceId, e);
            return false;
        }
    }
    
    /**
     * Register callback for device data
     */
    public void registerDeviceCallback(String deviceId, Consumer<DeviceReading> callback) {
        deviceCallbacks.put(deviceId, callback);
        logger.info("Registered callback for device: {}", deviceId);
    }
    
    /**
     * Unregister device callback
     */
    public void unregisterDeviceCallback(String deviceId) {
        deviceCallbacks.remove(deviceId);
        logger.info("Unregistered callback for device: {}", deviceId);
    }
    
    /**
     * Check MQTT connection status
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
    
    /**
     * Get connection statistics
     */
    public MqttConnectOptions getConnectionOptions() {
        try {
            // Return a new instance since getConnectionOptions() doesn't exist
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            return options;
        } catch (Exception e) {
            logger.error("Failed to get connection options", e);
            return null;
        }
    }
    
    /**
     * Parse device reading from JSON (simplified for demo)
     */
    private DeviceReading parseDeviceReading(String payload) {
        // In production, use Jackson ObjectMapper
        DeviceReading reading = new DeviceReading();
        reading.setTimestamp(java.time.LocalDateTime.now());
        reading.setEnergyConsumption(Math.random() * 100); // Demo value
        reading.setVoltage(240.0);
        reading.setCurrent(50.0);
        reading.setPowerFactor(0.95);
        return reading;
    }
    
    /**
     * Serialize command to JSON (simplified for demo)
     */
    private String serializeCommand(DeviceCommand command) {
        // In production, use Jackson ObjectMapper
        return String.format("{\"command\":\"%s\",\"timestamp\":\"%s\"}", 
                           command.getCommand(), command.getTimestamp());
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client disconnected and closed");
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup MQTT client", e);
        }
    }
}
