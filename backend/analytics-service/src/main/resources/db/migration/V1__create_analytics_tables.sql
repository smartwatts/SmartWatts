-- Analytics Service Database Migration
-- Creates tables for energy analytics, usage patterns, reports, insights, predictions, and energy reports

-- Energy Analytics Table
CREATE TABLE energy_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID,
    analytics_date TIMESTAMP NOT NULL,
    period_type VARCHAR(20) NOT NULL CHECK (period_type IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM')),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_consumption_kwh DECIMAL(10,4),
    peak_consumption_kw DECIMAL(8,4),
    average_consumption_kw DECIMAL(8,4),
    total_cost DECIMAL(10,2),
    cost_per_kwh DECIMAL(6,4),
    efficiency_score DECIMAL(5,2),
    carbon_footprint_kg DECIMAL(8,2),
    peak_hours_count INTEGER,
    off_peak_hours_count INTEGER,
    night_hours_count INTEGER,
    peak_consumption_kwh DECIMAL(10,4),
    off_peak_consumption_kwh DECIMAL(10,4),
    night_consumption_kwh DECIMAL(10,4),
    peak_cost DECIMAL(10,2),
    off_peak_cost DECIMAL(10,2),
    night_cost DECIMAL(10,2),
    savings_potential DECIMAL(10,2),
    optimization_recommendations TEXT,
    anomaly_count INTEGER,
    quality_score DECIMAL(5,2),
    data_points_count INTEGER,
    completeness_percentage DECIMAL(5,2),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Usage Patterns Table
CREATE TABLE usage_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID,
    pattern_date TIMESTAMP NOT NULL,
    pattern_type VARCHAR(30) NOT NULL CHECK (pattern_type IN ('DAILY_RHYTHM', 'WEEKLY_PATTERN', 'MONTHLY_TREND', 'SEASONAL_VARIATION', 'PEAK_USAGE', 'OFF_PEAK_USAGE', 'NIGHT_USAGE', 'WEEKEND_PATTERN', 'HOLIDAY_PATTERN', 'ANOMALOUS_USAGE', 'EFFICIENT_USAGE', 'INEFFICIENT_USAGE', 'CUSTOM_PATTERN')),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_hours DECIMAL(5,2),
    total_consumption_kwh DECIMAL(10,4),
    average_power_kw DECIMAL(8,4),
    peak_power_kw DECIMAL(8,4),
    total_cost DECIMAL(10,2),
    frequency_count INTEGER,
    frequency_percentage DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    is_anomaly BOOLEAN DEFAULT FALSE,
    anomaly_score DECIMAL(5,2),
    pattern_description TEXT,
    category VARCHAR(50),
    subcategory VARCHAR(50),
    tags TEXT,
    seasonal_factor DECIMAL(5,2),
    weather_correlation DECIMAL(5,2),
    occupancy_correlation DECIMAL(5,2),
    efficiency_rating DECIMAL(5,2),
    optimization_potential DECIMAL(5,2),
    recommendations TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reports Table
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    report_title VARCHAR(500),
    report_type VARCHAR(30) NOT NULL CHECK (report_type IN ('CONSUMPTION_SUMMARY', 'COST_ANALYSIS', 'EFFICIENCY_REPORT', 'COMPARISON_REPORT', 'TREND_ANALYSIS', 'PATTERN_ANALYSIS', 'ANOMALY_REPORT', 'OPTIMIZATION_REPORT', 'CARBON_FOOTPRINT', 'CUSTOM_REPORT')),
    format VARCHAR(10) NOT NULL DEFAULT 'PDF' CHECK (format IN ('PDF', 'EXCEL', 'CSV', 'JSON', 'HTML')),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    generated_by UUID,
    file_path VARCHAR(1000),
    file_size_bytes BIGINT,
    download_count INTEGER DEFAULT 0,
    last_downloaded_at TIMESTAMP,
    is_scheduled BOOLEAN DEFAULT FALSE,
    schedule_frequency VARCHAR(50),
    next_scheduled_at TIMESTAMP,
    parameters TEXT,
    summary TEXT,
    key_findings TEXT,
    recommendations TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    archived_at TIMESTAMP,
    archived_by UUID,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Energy Reports Table
CREATE TABLE energy_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    report_period VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    total_consumption_kwh DECIMAL(12,3),
    total_cost_ngn DECIMAL(15,2),
    average_daily_consumption_kwh DECIMAL(10,3),
    peak_consumption_kwh DECIMAL(10,3),
    carbon_footprint_kg DECIMAL(10,2),
    efficiency_score DECIMAL(3,2),
    cost_per_kwh DECIMAL(8,4),
    savings_potential_ngn DECIMAL(12,2),
    comparison_previous_period TEXT,
    trends_analysis TEXT,
    recommendations TEXT,
    charts_data TEXT,
    insights_summary TEXT,
    report_url VARCHAR(500),
    is_generated BOOLEAN NOT NULL DEFAULT FALSE,
    generation_date TIMESTAMP,
    file_size_bytes BIGINT,
    format VARCHAR(20),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Energy Predictions Table
CREATE TABLE energy_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    prediction_type VARCHAR(30) NOT NULL,
    prediction_horizon VARCHAR(20) NOT NULL,
    predicted_consumption_kwh DECIMAL(10,3) NOT NULL,
    predicted_cost_ngn DECIMAL(12,2),
    confidence_interval_lower DECIMAL(10,3),
    confidence_interval_upper DECIMAL(10,3),
    confidence_level DECIMAL(3,2),
    model_version VARCHAR(50),
    model_accuracy DECIMAL(3,2),
    prediction_date TIMESTAMP NOT NULL,
    target_date TIMESTAMP NOT NULL,
    weather_conditions TEXT,
    seasonal_factors TEXT,
    behavioral_factors TEXT,
    external_factors TEXT,
    is_accurate BOOLEAN,
    actual_consumption_kwh DECIMAL(10,3),
    actual_cost_ngn DECIMAL(12,2),
    prediction_error DECIMAL(10,3),
    error_percentage DECIMAL(5,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Energy Insights Table
CREATE TABLE energy_insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    insight_type VARCHAR(30) NOT NULL,
    insight_category VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(3,2),
    energy_savings_kwh DECIMAL(10,3),
    cost_savings_ngn DECIMAL(12,2),
    carbon_reduction_kg DECIMAL(10,2),
    recommendation TEXT,
    action_items TEXT,
    data_sources TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Energy Analytics
CREATE INDEX idx_energy_analytics_user_id ON energy_analytics(user_id);
CREATE INDEX idx_energy_analytics_device_id ON energy_analytics(device_id);
CREATE INDEX idx_energy_analytics_analytics_date ON energy_analytics(analytics_date);
CREATE INDEX idx_energy_analytics_period_type ON energy_analytics(period_type);
CREATE INDEX idx_energy_analytics_start_time ON energy_analytics(start_time);
CREATE INDEX idx_energy_analytics_end_time ON energy_analytics(end_time);
CREATE INDEX idx_energy_analytics_efficiency_score ON energy_analytics(efficiency_score);
CREATE INDEX idx_energy_analytics_quality_score ON energy_analytics(quality_score);
CREATE INDEX idx_energy_analytics_anomaly_count ON energy_analytics(anomaly_count);

-- Indexes for Usage Patterns
CREATE INDEX idx_usage_patterns_user_id ON usage_patterns(user_id);
CREATE INDEX idx_usage_patterns_device_id ON usage_patterns(device_id);
CREATE INDEX idx_usage_patterns_pattern_date ON usage_patterns(pattern_date);
CREATE INDEX idx_usage_patterns_pattern_type ON usage_patterns(pattern_type);
CREATE INDEX idx_usage_patterns_start_time ON usage_patterns(start_time);
CREATE INDEX idx_usage_patterns_end_time ON usage_patterns(end_time);
CREATE INDEX idx_usage_patterns_is_anomaly ON usage_patterns(is_anomaly);
CREATE INDEX idx_usage_patterns_anomaly_score ON usage_patterns(anomaly_score);
CREATE INDEX idx_usage_patterns_efficiency_rating ON usage_patterns(efficiency_rating);
CREATE INDEX idx_usage_patterns_confidence_score ON usage_patterns(confidence_score);
CREATE INDEX idx_usage_patterns_category ON usage_patterns(category);
CREATE INDEX idx_usage_patterns_subcategory ON usage_patterns(subcategory);

-- Indexes for Reports
CREATE INDEX idx_reports_user_id ON reports(user_id);
CREATE INDEX idx_reports_report_type ON reports(report_type);
CREATE INDEX idx_reports_format ON reports(format);
CREATE INDEX idx_reports_generated_at ON reports(generated_at);
CREATE INDEX idx_reports_is_scheduled ON reports(is_scheduled);
CREATE INDEX idx_reports_is_archived ON reports(is_archived);
CREATE INDEX idx_reports_is_public ON reports(is_public);
CREATE INDEX idx_reports_download_count ON reports(download_count);
CREATE INDEX idx_reports_next_scheduled_at ON reports(next_scheduled_at);

-- Indexes for Energy Reports
CREATE INDEX idx_energy_reports_user_id ON energy_reports(user_id);
CREATE INDEX idx_energy_reports_report_type ON energy_reports(report_type);
CREATE INDEX idx_energy_reports_report_period ON energy_reports(report_period);
CREATE INDEX idx_energy_reports_period_start ON energy_reports(period_start);
CREATE INDEX idx_energy_reports_period_end ON energy_reports(period_end);
CREATE INDEX idx_energy_reports_is_generated ON energy_reports(is_generated);
CREATE INDEX idx_energy_reports_generation_date ON energy_reports(generation_date);

-- Indexes for Energy Predictions
CREATE INDEX idx_energy_predictions_user_id ON energy_predictions(user_id);
CREATE INDEX idx_energy_predictions_prediction_type ON energy_predictions(prediction_type);
CREATE INDEX idx_energy_predictions_prediction_horizon ON energy_predictions(prediction_horizon);
CREATE INDEX idx_energy_predictions_prediction_date ON energy_predictions(prediction_date);
CREATE INDEX idx_energy_predictions_target_date ON energy_predictions(target_date);
CREATE INDEX idx_energy_predictions_is_accurate ON energy_predictions(is_accurate);

-- Indexes for Energy Insights
CREATE INDEX idx_energy_insights_user_id ON energy_insights(user_id);
CREATE INDEX idx_energy_insights_insight_type ON energy_insights(insight_type);
CREATE INDEX idx_energy_insights_insight_category ON energy_insights(insight_category);
CREATE INDEX idx_energy_insights_severity ON energy_insights(severity);
CREATE INDEX idx_energy_insights_confidence_score ON energy_insights(confidence_score);

-- Composite indexes for common queries
CREATE INDEX idx_energy_analytics_user_date_range ON energy_analytics(user_id, start_time, end_time);
CREATE INDEX idx_energy_analytics_user_period ON energy_analytics(user_id, period_type);
CREATE INDEX idx_usage_patterns_user_date_range ON usage_patterns(user_id, start_time, end_time);
CREATE INDEX idx_usage_patterns_user_type ON usage_patterns(user_id, pattern_type);
CREATE INDEX idx_reports_user_type ON reports(user_id, report_type);
CREATE INDEX idx_reports_user_generated ON reports(user_id, generated_at);
CREATE INDEX idx_energy_reports_user_type ON energy_reports(user_id, report_type);
CREATE INDEX idx_energy_predictions_user_type ON energy_predictions(user_id, prediction_type);
CREATE INDEX idx_energy_insights_user_type ON energy_insights(user_id, insight_type);

-- Unique constraints
CREATE UNIQUE INDEX uk_energy_analytics_user_device_date_period ON energy_analytics(user_id, device_id, analytics_date, period_type) WHERE device_id IS NOT NULL;
CREATE UNIQUE INDEX uk_energy_analytics_user_date_period ON energy_analytics(user_id, analytics_date, period_type) WHERE device_id IS NULL;

-- Foreign key constraints (placeholder - will be added when user service is integrated)
-- ALTER TABLE energy_analytics ADD CONSTRAINT fk_energy_analytics_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE energy_analytics ADD CONSTRAINT fk_energy_analytics_device_id FOREIGN KEY (device_id) REFERENCES devices(id);
-- ALTER TABLE usage_patterns ADD CONSTRAINT fk_usage_patterns_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE usage_patterns ADD CONSTRAINT fk_usage_patterns_device_id FOREIGN KEY (device_id) REFERENCES devices(id);
-- ALTER TABLE reports ADD CONSTRAINT fk_reports_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE reports ADD CONSTRAINT fk_reports_generated_by FOREIGN KEY (generated_by) REFERENCES users(id);
-- ALTER TABLE reports ADD CONSTRAINT fk_reports_archived_by FOREIGN KEY (archived_by) REFERENCES users(id);

-- Comments for documentation
COMMENT ON TABLE energy_analytics IS 'Stores computed energy analytics data for users and devices';
COMMENT ON TABLE usage_patterns IS 'Stores energy usage patterns and insights';
COMMENT ON TABLE reports IS 'Stores generated energy reports and analytics';
COMMENT ON TABLE energy_reports IS 'Stores energy-specific reports with detailed metrics';
COMMENT ON TABLE energy_predictions IS 'Stores energy consumption and cost predictions';
COMMENT ON TABLE energy_insights IS 'Stores AI-generated energy insights and recommendations';

COMMENT ON COLUMN energy_analytics.period_type IS 'Type of analytics period (HOURLY, DAILY, etc.)';
COMMENT ON COLUMN energy_analytics.efficiency_score IS 'Energy efficiency score (0-100)';
COMMENT ON COLUMN energy_analytics.quality_score IS 'Data quality score (0-100)';
COMMENT ON COLUMN energy_analytics.carbon_footprint_kg IS 'Carbon footprint in kg CO2';

COMMENT ON COLUMN usage_patterns.pattern_type IS 'Type of usage pattern detected';
COMMENT ON COLUMN usage_patterns.is_anomaly IS 'Whether this pattern is anomalous';
COMMENT ON COLUMN usage_patterns.anomaly_score IS 'Anomaly detection score (0-100)';
COMMENT ON COLUMN usage_patterns.efficiency_rating IS 'Efficiency rating for this pattern (0-100)';

COMMENT ON COLUMN reports.report_type IS 'Type of energy report generated';
COMMENT ON COLUMN reports.format IS 'Output format of the report';
COMMENT ON COLUMN reports.is_scheduled IS 'Whether this report is scheduled for automatic generation';
COMMENT ON COLUMN reports.is_archived IS 'Whether this report has been archived';

COMMENT ON COLUMN energy_predictions.prediction_type IS 'Type of prediction (CONSUMPTION, COST, etc.)';
COMMENT ON COLUMN energy_predictions.prediction_horizon IS 'Time horizon for prediction (HOURLY, DAILY, etc.)';
COMMENT ON COLUMN energy_predictions.confidence_level IS 'Confidence level of the prediction (0-1)';

COMMENT ON COLUMN energy_insights.insight_type IS 'Type of insight generated';
COMMENT ON COLUMN energy_insights.severity IS 'Severity level of the insight (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN energy_insights.confidence_score IS 'Confidence score of the insight (0-1)'; 