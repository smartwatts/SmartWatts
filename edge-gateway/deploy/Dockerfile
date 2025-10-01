# SmartWatts Edge Gateway Dockerfile for R501 RK3588
# Multi-stage build for optimized production image

# Build stage
FROM python:3.11-slim as builder

# Set build arguments
ARG DEBIAN_FRONTEND=noninteractive

# Install build dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    pkg-config \
    libssl-dev \
    libffi-dev \
    libjpeg-dev \
    libopenblas-dev \
    liblapack-dev \
    libhdf5-dev \
    pkg-config \
    libhdf5-serial-dev \
    libatlas-base-dev \
    gfortran \
    libopenmpi-dev \
    libopenmpi3 \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Production stage
FROM python:3.11-slim

# Set environment variables
ENV PYTHONPATH=/opt/smartwatts
ENV PYTHONUNBUFFERED=1
ENV TZ=UTC

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    libssl3 \
    libffi8 \
    libjpeg62-turbo \
    libopenblas0 \
    liblapack3 \
    libhdf5-103 \
    libhdf5-serial-103 \
    libatlas3-base \
    libgfortran5 \
    libopenmpi3 \
    mosquitto-clients \
    sqlite3 \
    net-tools \
    nmap \
    i2c-tools \
    spi-tools \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r smartwatts && useradd -r -g smartwatts smartwatts

# Create directories
RUN mkdir -p /opt/smartwatts/{data,logs,models,config,backups} && \
    chown -R smartwatts:smartwatts /opt/smartwatts

# Set working directory
WORKDIR /opt/smartwatts

# Copy Python dependencies from builder stage
COPY --from=builder /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# Copy application code
COPY . .

# Set ownership
RUN chown -R smartwatts:smartwatts /opt/smartwatts

# Switch to non-root user
USER smartwatts

# Expose ports
EXPOSE 8080 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Default command
CMD ["python", "main.py"]
