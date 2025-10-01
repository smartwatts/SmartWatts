"""
SmartWatts Edge Gateway AI Inference Service
TensorFlow Lite-based AI inference for energy forecasting, anomaly detection, and optimization
"""

import asyncio
import logging
import numpy as np
from typing import Dict, List, Optional, Any, Tuple
from datetime import datetime, timedelta
from dataclasses import dataclass
from enum import Enum
import json
from pathlib import Path
import tensorflow as tf
from core.config import EdgeConfig, AIConfig

class ModelType(Enum):
    """AI model types."""
    ENERGY_FORECAST = "energy_forecast"
    ANOMALY_DETECTION = "anomaly_detection"
    LOAD_PREDICTION = "load_prediction"
    EFFICIENCY_OPTIMIZATION = "efficiency_optimization"

@dataclass
class ModelInfo:
    """AI model information."""
    name: str
    model_type: ModelType
    path: str
    input_shape: Tuple[int, ...]
    output_shape: Tuple[int, ...]
    version: str
    accuracy: float
    last_updated: datetime
    loaded: bool = False

@dataclass
class InferenceResult:
    """AI inference result."""
    model_name: str
    prediction_type: str
    input_data: Dict[str, Any]
    prediction_result: Dict[str, Any]
    confidence: float
    timestamp: datetime
    processing_time_ms: float

class AIInferenceService:
    """AI inference service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig):
        self.config = config
        self.ai_config = config.ai
        self.logger = logging.getLogger(__name__)
        
        # Model management
        self.models: Dict[str, ModelInfo] = {}
        self.loaded_models: Dict[str, tf.lite.Interpreter] = {}
        
        # Inference queue
        self.inference_queue: asyncio.Queue = asyncio.Queue()
        self.inference_task: Optional[asyncio.Task] = None
        
        # Statistics
        self.stats = {
            "total_inferences": 0,
            "successful_inferences": 0,
            "failed_inferences": 0,
            "average_processing_time_ms": 0,
            "models_loaded": 0,
            "last_inference": None
        }
        
        # Processing times for averaging
        self.processing_times: List[float] = []
    
    async def initialize(self):
        """Initialize the AI inference service."""
        self.logger.info("🚀 Initializing AI inference service...")
        
        try:
            if not self.ai_config.enabled:
                self.logger.info("⚠️ AI inference is disabled")
                return
            
            # Create model directory if it doesn't exist
            model_dir = Path(self.ai_config.model_path)
            model_dir.mkdir(parents=True, exist_ok=True)
            
            # Initialize models
            await self._initialize_models()
            
            # Load available models
            await self._load_models()
            
            # Start inference processing loop
            if self.loaded_models:
                self.inference_task = asyncio.create_task(self._inference_loop())
                self.logger.info("✅ AI inference service initialized successfully")
            else:
                self.logger.warning("⚠️ No AI models loaded")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to initialize AI inference service: {e}")
            raise
    
    async def stop(self):
        """Stop the AI inference service."""
        self.logger.info("🛑 Stopping AI inference service...")
        
        try:
            # Stop inference loop
            if self.inference_task:
                self.inference_task.cancel()
                try:
                    await self.inference_task
                except asyncio.CancelledError:
                    pass
            
            # Clear loaded models
            self.loaded_models.clear()
            
            self.logger.info("✅ AI inference service stopped")
            
        except Exception as e:
            self.logger.error(f"❌ Error stopping AI inference service: {e}")
    
    async def _initialize_models(self):
        """Initialize model definitions."""
        for model_name, model_file in self.ai_config.models.items():
            try:
                model_type = ModelType(model_name)
                model_path = Path(self.ai_config.model_path) / model_file
                
                # Get model info (this would typically come from model metadata)
                model_info = ModelInfo(
                    name=model_name,
                    model_type=model_type,
                    path=str(model_path),
                    input_shape=self._get_default_input_shape(model_type),
                    output_shape=self._get_default_output_shape(model_type),
                    version="1.0.0",
                    accuracy=0.85,  # Default accuracy
                    last_updated=datetime.now()
                )
                
                self.models[model_name] = model_info
                self.logger.info(f"📋 Initialized model: {model_name}")
                
            except Exception as e:
                self.logger.error(f"❌ Error initializing model {model_name}: {e}")
    
    def _get_default_input_shape(self, model_type: ModelType) -> Tuple[int, ...]:
        """Get default input shape for model type."""
        shapes = {
            ModelType.ENERGY_FORECAST: (24, 6),  # 24 hours, 6 features
            ModelType.ANOMALY_DETECTION: (1, 10),  # 1 sample, 10 features
            ModelType.LOAD_PREDICTION: (168, 4),  # 1 week, 4 features
            ModelType.EFFICIENCY_OPTIMIZATION: (1, 8)  # 1 sample, 8 features
        }
        return shapes.get(model_type, (1, 1))
    
    def _get_default_output_shape(self, model_type: ModelType) -> Tuple[int, ...]:
        """Get default output shape for model type."""
        shapes = {
            ModelType.ENERGY_FORECAST: (24,),  # 24-hour forecast
            ModelType.ANOMALY_DETECTION: (1,),  # Anomaly score
            ModelType.LOAD_PREDICTION: (24,),  # 24-hour load prediction
            ModelType.EFFICIENCY_OPTIMIZATION: (1,)  # Optimization score
        }
        return shapes.get(model_type, (1,))
    
    async def _load_models(self):
        """Load available TensorFlow Lite models."""
        for model_name, model_info in self.models.items():
            try:
                if Path(model_info.path).exists():
                    # Load TensorFlow Lite model
                    interpreter = tf.lite.Interpreter(model_path=model_info.path)
                    interpreter.allocate_tensors()
                    
                    self.loaded_models[model_name] = interpreter
                    model_info.loaded = True
                    self.stats["models_loaded"] += 1
                    
                    self.logger.info(f"✅ Loaded model: {model_name}")
                else:
                    self.logger.warning(f"⚠️ Model file not found: {model_info.path}")
                    
            except Exception as e:
                self.logger.error(f"❌ Error loading model {model_name}: {e}")
    
    async def _inference_loop(self):
        """Main inference processing loop."""
        while True:
            try:
                # Get inference request from queue
                request = await self.inference_queue.get()
                
                # Process inference
                result = await self._process_inference(request)
                
                # Store result if storage service is available
                if result and hasattr(self, 'storage_service'):
                    await self._store_inference_result(result)
                
                # Mark task as done
                self.inference_queue.task_done()
                
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.logger.error(f"❌ Error in inference loop: {e}")
                self.stats["failed_inferences"] += 1
    
    async def _process_inference(self, request: Dict[str, Any]) -> Optional[InferenceResult]:
        """Process a single inference request."""
        start_time = datetime.now()
        
        try:
            model_name = request.get("model_name")
            prediction_type = request.get("prediction_type")
            input_data = request.get("input_data", {})
            
            if model_name not in self.loaded_models:
                self.logger.error(f"❌ Model not loaded: {model_name}")
                return None
            
            # Get model and prepare input
            interpreter = self.loaded_models[model_name]
            model_info = self.models[model_name]
            
            # Prepare input tensor
            input_tensor = self._prepare_input_tensor(input_data, model_info)
            
            # Run inference
            prediction_result = self._run_inference(interpreter, input_tensor, model_info)
            
            # Calculate confidence (simplified)
            confidence = self._calculate_confidence(prediction_result, model_info)
            
            # Calculate processing time
            processing_time = (datetime.now() - start_time).total_seconds() * 1000
            
            # Create result
            result = InferenceResult(
                model_name=model_name,
                prediction_type=prediction_type,
                input_data=input_data,
                prediction_result=prediction_result,
                confidence=confidence,
                timestamp=datetime.now(),
                processing_time_ms=processing_time
            )
            
            # Update statistics
            self.stats["total_inferences"] += 1
            self.stats["successful_inferences"] += 1
            self.stats["last_inference"] = datetime.now()
            
            # Update average processing time
            self.processing_times.append(processing_time)
            if len(self.processing_times) > 100:  # Keep only last 100 times
                self.processing_times.pop(0)
            self.stats["average_processing_time_ms"] = np.mean(self.processing_times)
            
            self.logger.debug(f"🤖 Inference completed: {model_name} in {processing_time:.2f}ms")
            return result
            
        except Exception as e:
            self.logger.error(f"❌ Error processing inference: {e}")
            self.stats["failed_inferences"] += 1
            return None
    
    def _prepare_input_tensor(self, input_data: Dict[str, Any], model_info: ModelInfo) -> np.ndarray:
        """Prepare input tensor for model inference."""
        # This is a simplified implementation
        # In practice, you'd have specific preprocessing for each model type
        
        if model_info.model_type == ModelType.ENERGY_FORECAST:
            # Prepare energy forecast input
            return self._prepare_energy_forecast_input(input_data, model_info)
        elif model_info.model_type == ModelType.ANOMALY_DETECTION:
            # Prepare anomaly detection input
            return self._prepare_anomaly_detection_input(input_data, model_info)
        elif model_info.model_type == ModelType.LOAD_PREDICTION:
            # Prepare load prediction input
            return self._prepare_load_prediction_input(input_data, model_info)
        elif model_info.model_type == ModelType.EFFICIENCY_OPTIMIZATION:
            # Prepare efficiency optimization input
            return self._prepare_efficiency_optimization_input(input_data, model_info)
        else:
            # Default input preparation
            return np.zeros(model_info.input_shape, dtype=np.float32)
    
    def _prepare_energy_forecast_input(self, input_data: Dict[str, Any], model_info: ModelInfo) -> np.ndarray:
        """Prepare input for energy forecasting model."""
        # Extract features: power, voltage, current, temperature, time_of_day, day_of_week
        features = []
        
        # Historical power data (last 24 hours)
        power_data = input_data.get("power_history", [0] * 24)
        features.extend(power_data[:24])
        
        # Current conditions
        features.append(input_data.get("voltage", 220.0))
        features.append(input_data.get("current", 1.0))
        features.append(input_data.get("temperature", 25.0))
        
        # Time features
        now = datetime.now()
        features.append(now.hour / 24.0)  # Normalized hour
        features.append(now.weekday() / 7.0)  # Normalized day of week
        
        # Pad or truncate to match input shape
        while len(features) < np.prod(model_info.input_shape):
            features.append(0.0)
        features = features[:np.prod(model_info.input_shape)]
        
        return np.array(features, dtype=np.float32).reshape(model_info.input_shape)
    
    def _prepare_anomaly_detection_input(self, input_data: Dict[str, Any], model_info: ModelInfo) -> np.ndarray:
        """Prepare input for anomaly detection model."""
        features = [
            input_data.get("power", 0.0),
            input_data.get("voltage", 220.0),
            input_data.get("current", 1.0),
            input_data.get("frequency", 50.0),
            input_data.get("power_factor", 1.0),
            input_data.get("temperature", 25.0),
            input_data.get("humidity", 50.0),
            input_data.get("time_of_day", 12.0),
            input_data.get("day_of_week", 1.0),
            input_data.get("load_factor", 0.5)
        ]
        
        return np.array(features, dtype=np.float32).reshape(model_info.input_shape)
    
    def _prepare_load_prediction_input(self, input_data: Dict[str, Any], model_info: ModelInfo) -> np.ndarray:
        """Prepare input for load prediction model."""
        # Similar to energy forecast but with different features
        features = []
        
        # Historical load data (last week)
        load_data = input_data.get("load_history", [0] * 168)
        features.extend(load_data[:168])
        
        # Weather data
        features.append(input_data.get("temperature", 25.0))
        features.append(input_data.get("humidity", 50.0))
        features.append(input_data.get("weather_condition", 1.0))
        
        # Time features
        now = datetime.now()
        features.append(now.hour / 24.0)
        
        # Pad or truncate
        while len(features) < np.prod(model_info.input_shape):
            features.append(0.0)
        features = features[:np.prod(model_info.input_shape)]
        
        return np.array(features, dtype=np.float32).reshape(model_info.input_shape)
    
    def _prepare_efficiency_optimization_input(self, input_data: Dict[str, Any], model_info: ModelInfo) -> np.ndarray:
        """Prepare input for efficiency optimization model."""
        features = [
            input_data.get("current_efficiency", 0.8),
            input_data.get("power_consumption", 1000.0),
            input_data.get("voltage", 220.0),
            input_data.get("current", 5.0),
            input_data.get("frequency", 50.0),
            input_data.get("power_factor", 0.9),
            input_data.get("temperature", 25.0),
            input_data.get("load_factor", 0.7)
        ]
        
        return np.array(features, dtype=np.float32).reshape(model_info.input_shape)
    
    def _run_inference(self, interpreter: tf.lite.Interpreter, input_tensor: np.ndarray, model_info: ModelInfo) -> Dict[str, Any]:
        """Run inference on the loaded model."""
        # Get input and output details
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        
        # Set input tensor
        interpreter.set_tensor(input_details[0]['index'], input_tensor)
        
        # Run inference
        interpreter.invoke()
        
        # Get output
        output_data = interpreter.get_tensor(output_details[0]['index'])
        
        # Format output based on model type
        if model_info.model_type == ModelType.ENERGY_FORECAST:
            return {
                "forecast": output_data.tolist(),
                "forecast_hours": len(output_data),
                "total_forecast_energy": float(np.sum(output_data))
            }
        elif model_info.model_type == ModelType.ANOMALY_DETECTION:
            return {
                "anomaly_score": float(output_data[0]),
                "is_anomaly": float(output_data[0]) > 0.5,
                "severity": "high" if float(output_data[0]) > 0.8 else "medium" if float(output_data[0]) > 0.5 else "low"
            }
        elif model_info.model_type == ModelType.LOAD_PREDICTION:
            return {
                "predicted_load": output_data.tolist(),
                "peak_load": float(np.max(output_data)),
                "average_load": float(np.mean(output_data))
            }
        elif model_info.model_type == ModelType.EFFICIENCY_OPTIMIZATION:
            return {
                "optimization_score": float(output_data[0]),
                "recommended_actions": self._get_optimization_recommendations(float(output_data[0])),
                "potential_savings": float(output_data[0]) * 100  # Percentage
            }
        else:
            return {"raw_output": output_data.tolist()}
    
    def _get_optimization_recommendations(self, score: float) -> List[str]:
        """Get optimization recommendations based on score."""
        recommendations = []
        
        if score < 0.3:
            recommendations.extend([
                "Check power factor correction",
                "Optimize load scheduling",
                "Consider energy storage"
            ])
        elif score < 0.6:
            recommendations.extend([
                "Fine-tune existing systems",
                "Monitor peak demand"
            ])
        else:
            recommendations.append("System is well optimized")
        
        return recommendations
    
    def _calculate_confidence(self, prediction_result: Dict[str, Any], model_info: ModelInfo) -> float:
        """Calculate confidence score for prediction."""
        # Simplified confidence calculation
        # In practice, this would be more sophisticated
        
        if model_info.model_type == ModelType.ANOMALY_DETECTION:
            anomaly_score = prediction_result.get("anomaly_score", 0.5)
            return abs(anomaly_score - 0.5) * 2  # Higher confidence for extreme scores
        else:
            return 0.85  # Default confidence
    
    async def _store_inference_result(self, result: InferenceResult):
        """Store inference result in database."""
        try:
            # This would integrate with the storage service
            # For now, just log the result
            self.logger.debug(f"📊 Stored inference result: {result.model_name}")
        except Exception as e:
            self.logger.error(f"❌ Error storing inference result: {e}")
    
    async def predict_energy_forecast(self, input_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Predict energy consumption forecast."""
        if "energy_forecast" not in self.loaded_models:
            self.logger.error("❌ Energy forecast model not loaded")
            return None
        
        request = {
            "model_name": "energy_forecast",
            "prediction_type": "energy_forecast",
            "input_data": input_data
        }
        
        await self.inference_queue.put(request)
        
        # Wait for result (simplified - in practice, you'd use a callback or future)
        await asyncio.sleep(0.1)
        return {"status": "queued"}
    
    async def detect_anomaly(self, input_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Detect energy consumption anomalies."""
        if "anomaly_detection" not in self.loaded_models:
            self.logger.error("❌ Anomaly detection model not loaded")
            return None
        
        request = {
            "model_name": "anomaly_detection",
            "prediction_type": "anomaly_detection",
            "input_data": input_data
        }
        
        await self.inference_queue.put(request)
        return {"status": "queued"}
    
    async def predict_load(self, input_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Predict load consumption."""
        if "load_prediction" not in self.loaded_models:
            self.logger.error("❌ Load prediction model not loaded")
            return None
        
        request = {
            "model_name": "load_prediction",
            "prediction_type": "load_prediction",
            "input_data": input_data
        }
        
        await self.inference_queue.put(request)
        return {"status": "queued"}
    
    async def optimize_efficiency(self, input_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """Optimize energy efficiency."""
        if "efficiency_optimization" not in self.loaded_models:
            self.logger.error("❌ Efficiency optimization model not loaded")
            return None
        
        request = {
            "model_name": "efficiency_optimization",
            "prediction_type": "efficiency_optimization",
            "input_data": input_data
        }
        
        await self.inference_queue.put(request)
        return {"status": "queued"}
    
    def is_ready(self) -> bool:
        """Check if AI inference service is ready."""
        return len(self.loaded_models) > 0
    
    def get_loaded_models(self) -> List[str]:
        """Get list of loaded model names."""
        return list(self.loaded_models.keys())
    
    def get_model_info(self, model_name: str) -> Optional[Dict[str, Any]]:
        """Get information about a specific model."""
        if model_name not in self.models:
            return None
        
        model = self.models[model_name]
        return {
            "name": model.name,
            "type": model.model_type.value,
            "version": model.version,
            "accuracy": model.accuracy,
            "loaded": model.loaded,
            "input_shape": model.input_shape,
            "output_shape": model.output_shape,
            "last_updated": model.last_updated.isoformat()
        }
    
    def get_stats(self) -> Dict[str, Any]:
        """Get AI inference service statistics."""
        return self.stats.copy()
