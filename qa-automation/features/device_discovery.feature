Feature: Device Discovery and Pairing
  As a SmartWatts Edge user
  I want to discover and pair smart devices
  So that I can monitor their energy consumption

  Background:
    Given I am logged into the SmartWatts Edge system
    And I am on the device management page
    And there are simulated smart devices available

  Scenario: Device discovery page loads correctly
    When I navigate to the devices page
    Then I should see "Device Management" header
    And I should see "Discover Devices" section
    And I should see a "Scan for Devices" button
    And I should see device status indicators

  Scenario: Discover simulated smart plugs
    Given there are smart plugs available on the network
    When I click "Scan for Devices"
    Then I should see discovered devices appear
    And I should see device information including:
      | Field | Description |
      | Device ID | Unique identifier |
      | Device Type | SMART_PLUG |
      | Location | Room location |
      | Status | ONLINE/OFFLINE |
      | Power Consumption | Current power usage |

  Scenario: Device pairing process
    Given I have discovered a smart plug with ID "smart_plug_001"
    When I click "Pair Device" for that device
    Then I should see a pairing dialog
    And I should be able to enter a custom device name
    And I should be able to select the device location
    And I should be able to choose the communication protocol
    When I click "Confirm Pairing"
    Then I should see "Device paired successfully"
    And the device should appear in my paired devices list
    And the device should be associated with my account

  Scenario: Device status monitoring
    Given I have paired a smart plug
    When the device is online
    Then I should see the device status as "ONLINE"
    And I should see real-time power consumption data
    And I should see the last update timestamp
    When the device goes offline
    Then I should see the device status as "OFFLINE"
    And the power consumption should show as 0W
    And I should see an offline indicator

  Scenario: Device configuration
    Given I have paired a smart plug
    When I click on the device settings
    Then I should see configuration options including:
      | Option | Description |
      | Device Name | Custom name for the device |
      | Location | Physical location |
      | Protocol | Communication protocol |
      | Power Threshold | Alert threshold |
    When I update the device name to "Living Room TV"
    And I update the location to "Updated Living Room"
    And I click "Save Configuration"
    Then I should see "Configuration saved successfully"
    And the device should display the updated information

  Scenario: Device removal
    Given I have paired a smart plug
    When I click "Remove Device"
    Then I should see a confirmation dialog
    And I should see "Are you sure you want to remove this device?"
    When I click "Confirm Removal"
    Then I should see "Device removed successfully"
    And the device should no longer appear in my paired devices list
    And the device data should be removed from the database

  Scenario: Multiple protocol support
    Given there are devices with different protocols available:
      | Device ID | Protocol | Type |
      | mqtt_plug_001 | MQTT | SMART_PLUG |
      | modbus_inverter_001 | MODBUS | INVERTER |
      | wifi_meter_001 | WIFI | SMART_METER |
    When I scan for devices
    Then I should see all devices regardless of protocol
    And each device should show its protocol type
    And I should be able to pair devices with different protocols
    And all devices should work together in the system

  Scenario: Device discovery timeout
    Given there are no devices available on the network
    When I click "Scan for Devices"
    Then I should see a scanning indicator
    And after 15 seconds I should see "No devices found"
    And I should see "Discovery completed"
    And I should be able to scan again

  Scenario: Device discovery with offline devices
    Given there are both online and offline devices on the network
    When I click "Scan for Devices"
    Then I should only see online devices
    And offline devices should not appear in the discovery results
    And I should see a count of discovered devices
    And I should be able to pair the online devices

  Scenario: Device data synchronization
    Given I have paired multiple devices
    When I generate data from the devices
    Then the data should be synchronized across all devices
    And I should see real-time updates on the dashboard
    And the data should be stored in the local database
    And I should be able to view historical data

  Scenario: Device error handling
    Given I have paired a device
    When the device encounters an error
    Then I should see an error indicator on the device
    And I should see an error message describing the issue
    And the system should continue to function with other devices
    And I should be able to retry the operation
    When the device recovers from the error
    Then the error indicator should disappear
    And normal operation should resume

  Scenario: Device firmware update simulation
    Given I have paired a device
    When a firmware update is available
    Then I should see a firmware update notification
    And I should be able to initiate the update
    When I click "Update Firmware"
    Then the update should be downloaded and installed
    And I should see "Firmware updated successfully"
    And the device should restart with the new firmware
    And the device should continue to function normally
