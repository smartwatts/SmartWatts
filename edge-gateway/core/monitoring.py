"""
SmartWatts Edge Gateway Monitoring Service
Prometheus metrics collection and system monitoring
"""

import asyncio
import logging
import psutil
from typing import Dict, Any
from datetime import datetime
from prometheus_client import Counter, Histogram, Gauge, start_http_server, generate_latest
from core.config import EdgeConfig

class MetricsCollector:
    """Prometheus metrics collector for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig):
        self.config = config
        self.logger = logging.getLogger(__name__)
        
        # Prometheus metrics
        self.metrics = {
            # Energy data metrics
            "energy_readings_total": Counter(
                "smartwatts_energy_readings_total",
                "Total number of energy readings processed",
                ["device_id", "device_type"]
            ),
            "energy_power_watts": Gauge(
                "smartwatts_energy_power_watts",
                "Current power consumption in watts",
                ["device_id"]
            ),
            "energy_voltage_volts": Gauge(
                "smartwatts_energy_voltage_volts",
                "Current voltage in volts",
                ["device_id"]
            ),
            "energy_current_amps": Gauge(
                "smartwatts_energy_current_amps",
                "Current current in amperes",
                ["device_id"]
            ),
            
            # Device metrics
            "devices_total": Gauge(
                "smartwatts_devices_total",
                "Total number of devices",
                ["status", "type"]
            ),
            "device_uptime_seconds": Gauge(
                "smartwatts_device_uptime_seconds",
                "Device uptime in seconds",
                ["device_id"]
            ),
            
            # MQTT metrics
            "mqtt_messages_received_total": Counter(
                "smartwatts_mqtt_messages_received_total",
                "Total number of MQTT messages received",
                ["topic"]
            ),
            "mqtt_messages_sent_total": Counter(
                "smartwatts_mqtt_messages_sent_total",
                "Total number of MQTT messages sent",
                ["topic"]
            ),
            "mqtt_connection_status": Gauge(
                "smartwatts_mqtt_connection_status",
                "MQTT connection status (1=connected, 0=disconnected)"
            ),
            
            # Modbus metrics
            "modbus_reads_total": Counter(
                "smartwatts_modbus_reads_total",
                "Total number of Modbus reads",
                ["device_id", "register"]
            ),
            "modbus_read_errors_total": Counter(
                "smartwatts_modbus_read_errors_total",
                "Total number of Modbus read errors",
                ["device_id"]
            ),
            "modbus_connection_status": Gauge(
                "smartwatts_modbus_connection_status",
                "Modbus connection status (1=connected, 0=disconnected)",
                ["device_id"]
            ),
            
            # AI inference metrics
            "ai_inferences_total": Counter(
                "smartwatts_ai_inferences_total",
                "Total number of AI inferences",
                ["model_name", "prediction_type"]
            ),
            "ai_inference_duration_seconds": Histogram(
                "smartwatts_ai_inference_duration_seconds",
                "AI inference duration in seconds",
                ["model_name"],
                buckets=[0.1, 0.5, 1.0, 2.0, 5.0, 10.0]
            ),
            "ai_model_loaded": Gauge(
                "smartwatts_ai_model_loaded",
                "AI model loaded status (1=loaded, 0=not loaded)",
                ["model_name"]
            ),
            
            # Data sync metrics
            "sync_records_total": Counter(
                "smartwatts_sync_records_total",
                "Total number of records synchronized",
                ["table_name", "status"]
            ),
            "sync_duration_seconds": Histogram(
                "smartwatts_sync_duration_seconds",
                "Data synchronization duration in seconds",
                ["table_name"],
                buckets=[1.0, 5.0, 10.0, 30.0, 60.0, 300.0]
            ),
            "sync_queue_size": Gauge(
                "smartwatts_sync_queue_size",
                "Number of records pending synchronization"
            ),
            
            # Alert metrics
            "alerts_total": Counter(
                "smartwatts_alerts_total",
                "Total number of alerts",
                ["device_id", "severity", "type"]
            ),
            "alerts_active": Gauge(
                "smartwatts_alerts_active",
                "Number of active alerts",
                ["severity"]
            ),
            
            # System metrics
            "system_cpu_usage_percent": Gauge(
                "smartwatts_system_cpu_usage_percent",
                "System CPU usage percentage"
            ),
            "system_memory_usage_bytes": Gauge(
                "smartwatts_system_memory_usage_bytes",
                "System memory usage in bytes"
            ),
            "system_disk_usage_bytes": Gauge(
                "smartwatts_system_disk_usage_bytes",
                "System disk usage in bytes",
                ["device", "mountpoint"]
            ),
            "system_uptime_seconds": Gauge(
                "smartwatts_system_uptime_seconds",
                "System uptime in seconds"
            ),
            
            # Database metrics
            "database_size_bytes": Gauge(
                "smartwatts_database_size_bytes",
                "Database size in bytes"
            ),
            "database_connections_active": Gauge(
                "smartwatts_database_connections_active",
                "Number of active database connections"
            ),
        }
        
        # Monitoring state
        self.monitoring = False
        self.monitor_task: asyncio.Task = None
        
        # System info
        self.boot_time = datetime.fromtimestamp(psutil.boot_time())
    
    async def start(self):
        """Start the metrics collector."""
        self.logger.info("🚀 Starting metrics collector...")
        
        try:
            # Start Prometheus HTTP server
            start_http_server(9090)
            self.logger.info("📊 Prometheus metrics server started on port 9090")
            
            # Start system monitoring
            self.monitoring = True
            self.monitor_task = asyncio.create_task(self._monitor_loop())
            
            self.logger.info("✅ Metrics collector started successfully")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start metrics collector: {e}")
            raise
    
    async def stop(self):
        """Stop the metrics collector."""
        self.logger.info("🛑 Stopping metrics collector...")
        
        try:
            # Stop monitoring
            self.monitoring = False
            if self.monitor_task:
                self.monitor_task.cancel()
                try:
                    await self.monitor_task
                except asyncio.CancelledError:
                    pass
            
            self.logger.info("✅ Metrics collector stopped")
            
        except Exception as e:
            self.logger.error(f"❌ Error stopping metrics collector: {e}")
    
    async def _monitor_loop(self):
        """Main monitoring loop."""
        while self.monitoring:
            try:
                # Update system metrics
                await self._update_system_metrics()
                
                # Wait before next update
                await asyncio.sleep(30)  # Update every 30 seconds
                
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.logger.error(f"❌ Error in monitoring loop: {e}")
                await asyncio.sleep(60)  # Wait before retry
    
    async def _update_system_metrics(self):
        """Update system metrics."""
        try:
            # CPU usage
            cpu_percent = psutil.cpu_percent(interval=1)
            self.metrics["system_cpu_usage_percent"].set(cpu_percent)
            
            # Memory usage
            memory = psutil.virtual_memory()
            self.metrics["system_memory_usage_bytes"].set(memory.used)
            
            # Disk usage
            for partition in psutil.disk_partitions():
                try:
                    usage = psutil.disk_usage(partition.mountpoint)
                    self.metrics["system_disk_usage_bytes"].labels(
                        device=partition.device,
                        mountpoint=partition.mountpoint
                    ).set(usage.used)
                except PermissionError:
                    pass  # Skip partitions we can't access
            
            # System uptime
            uptime = (datetime.now() - self.boot_time).total_seconds()
            self.metrics["system_uptime_seconds"].set(uptime)
            
        except Exception as e:
            self.logger.error(f"❌ Error updating system metrics: {e}")
    
    def record_energy_reading(self, device_id: str, device_type: str, power: float = None, voltage: float = None, current: float = None):
        """Record energy reading metrics."""
        try:
            self.metrics["energy_readings_total"].labels(
                device_id=device_id,
                device_type=device_type
            ).inc()
            
            if power is not None:
                self.metrics["energy_power_watts"].labels(device_id=device_id).set(power)
            
            if voltage is not None:
                self.metrics["energy_voltage_volts"].labels(device_id=device_id).set(voltage)
            
            if current is not None:
                self.metrics["energy_current_amps"].labels(device_id=device_id).set(current)
                
        except Exception as e:
            self.logger.error(f"❌ Error recording energy reading metrics: {e}")
    
    def record_device_status(self, device_id: str, device_type: str, status: str):
        """Record device status metrics."""
        try:
            # Update device count by status and type
            self.metrics["devices_total"].labels(status=status, type=device_type).inc()
            
            # Set device uptime (simplified)
            if status == "online":
                self.metrics["device_uptime_seconds"].labels(device_id=device_id).set(
                    (datetime.now() - self.boot_time).total_seconds()
                )
                
        except Exception as e:
            self.logger.error(f"❌ Error recording device status metrics: {e}")
    
    def record_mqtt_message(self, topic: str, direction: str):
        """Record MQTT message metrics."""
        try:
            if direction == "received":
                self.metrics["mqtt_messages_received_total"].labels(topic=topic).inc()
            elif direction == "sent":
                self.metrics["mqtt_messages_sent_total"].labels(topic=topic).inc()
                
        except Exception as e:
            self.logger.error(f"❌ Error recording MQTT message metrics: {e}")
    
    def record_mqtt_connection(self, connected: bool):
        """Record MQTT connection status."""
        try:
            self.metrics["mqtt_connection_status"].set(1 if connected else 0)
        except Exception as e:
            self.logger.error(f"❌ Error recording MQTT connection metrics: {e}")
    
    def record_modbus_read(self, device_id: str, register: int, success: bool = True):
        """Record Modbus read metrics."""
        try:
            if success:
                self.metrics["modbus_reads_total"].labels(
                    device_id=device_id,
                    register=str(register)
                ).inc()
            else:
                self.metrics["modbus_read_errors_total"].labels(device_id=device_id).inc()
                
        except Exception as e:
            self.logger.error(f"❌ Error recording Modbus read metrics: {e}")
    
    def record_modbus_connection(self, device_id: str, connected: bool):
        """Record Modbus connection status."""
        try:
            self.metrics["modbus_connection_status"].labels(device_id=device_id).set(1 if connected else 0)
        except Exception as e:
            self.logger.error(f"❌ Error recording Modbus connection metrics: {e}")
    
    def record_ai_inference(self, model_name: str, prediction_type: str, duration: float):
        """Record AI inference metrics."""
        try:
            self.metrics["ai_inferences_total"].labels(
                model_name=model_name,
                prediction_type=prediction_type
            ).inc()
            
            self.metrics["ai_inference_duration_seconds"].labels(
                model_name=model_name
            ).observe(duration)
                
        except Exception as e:
            self.logger.error(f"❌ Error recording AI inference metrics: {e}")
    
    def record_ai_model_loaded(self, model_name: str, loaded: bool):
        """Record AI model loaded status."""
        try:
            self.metrics["ai_model_loaded"].labels(model_name=model_name).set(1 if loaded else 0)
        except Exception as e:
            self.logger.error(f"❌ Error recording AI model loaded metrics: {e}")
    
    def record_sync_record(self, table_name: str, status: str):
        """Record data sync metrics."""
        try:
            self.metrics["sync_records_total"].labels(
                table_name=table_name,
                status=status
            ).inc()
        except Exception as e:
            self.logger.error(f"❌ Error recording sync metrics: {e}")
    
    def record_sync_duration(self, table_name: str, duration: float):
        """Record data sync duration."""
        try:
            self.metrics["sync_duration_seconds"].labels(table_name=table_name).observe(duration)
        except Exception as e:
            self.logger.error(f"❌ Error recording sync duration metrics: {e}")
    
    def record_sync_queue_size(self, size: int):
        """Record sync queue size."""
        try:
            self.metrics["sync_queue_size"].set(size)
        except Exception as e:
            self.logger.error(f"❌ Error recording sync queue size metrics: {e}")
    
    def record_alert(self, device_id: str, severity: str, alert_type: str):
        """Record alert metrics."""
        try:
            self.metrics["alerts_total"].labels(
                device_id=device_id,
                severity=severity,
                type=alert_type
            ).inc()
        except Exception as e:
            self.logger.error(f"❌ Error recording alert metrics: {e}")
    
    def record_active_alerts(self, severity: str, count: int):
        """Record active alerts count."""
        try:
            self.metrics["alerts_active"].labels(severity=severity).set(count)
        except Exception as e:
            self.logger.error(f"❌ Error recording active alerts metrics: {e}")
    
    def record_database_size(self, size_bytes: int):
        """Record database size."""
        try:
            self.metrics["database_size_bytes"].set(size_bytes)
        except Exception as e:
            self.logger.error(f"❌ Error recording database size metrics: {e}")
    
    def record_database_connections(self, count: int):
        """Record active database connections."""
        try:
            self.metrics["database_connections_active"].set(count)
        except Exception as e:
            self.logger.error(f"❌ Error recording database connections metrics: {e}")
    
    def get_metrics(self) -> str:
        """Get metrics in Prometheus format."""
        try:
            return generate_latest()
        except Exception as e:
            self.logger.error(f"❌ Error generating metrics: {e}")
            return ""
    
    def get_metrics_summary(self) -> Dict[str, Any]:
        """Get metrics summary for API endpoints."""
        try:
            return {
                "timestamp": datetime.now().isoformat(),
                "system": {
                    "cpu_usage_percent": psutil.cpu_percent(),
                    "memory_usage_bytes": psutil.virtual_memory().used,
                    "uptime_seconds": (datetime.now() - self.boot_time).total_seconds()
                },
                "monitoring_active": self.monitoring,
                "metrics_count": len(self.metrics)
            }
        except Exception as e:
            self.logger.error(f"❌ Error getting metrics summary: {e}")
            return {"error": str(e)}
