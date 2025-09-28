-- Feature Flag Service Database Migration
-- Creates tables for feature flags and subscription management

-- Feature Flags Table
CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_key VARCHAR(100) UNIQUE NOT NULL,
    feature_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_globally_enabled BOOLEAN DEFAULT FALSE,
    is_paid_feature BOOLEAN DEFAULT FALSE,
    feature_category VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Subscription Plans Table
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_key VARCHAR(50) UNIQUE NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    description TEXT,
    monthly_price DECIMAL(10,2),
    yearly_price DECIMAL(10,2),
    max_users INTEGER,
    max_devices INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Subscription Features Table (Many-to-Many relationship)
CREATE TABLE subscription_features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    feature_flag_id UUID NOT NULL REFERENCES feature_flags(id),
    is_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subscription_plan_id, feature_flag_id)
);

-- User subscriptions table
CREATE TABLE user_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    subscription_plan_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE
);

-- Insert default subscription plans
INSERT INTO subscription_plans (plan_key, plan_name, description, monthly_price, yearly_price, max_users, max_devices) VALUES
('FREEMIUM', 'Freemium', 'Basic energy monitoring with limited features', 0.00, 0.00, 1, 2),
('PREMIUM', 'Premium', 'Advanced monitoring with premium features', 29.99, 299.99, 5, 10),
('ENTERPRISE', 'Enterprise', 'Full-featured solution for businesses', 99.99, 999.99, 100, 500);

-- Insert default feature flags
INSERT INTO feature_flags (feature_key, feature_name, description, is_globally_enabled, is_paid_feature, feature_category) VALUES
('FACILITY360', 'Facility360 Management', 'Advanced facility management with asset tracking, fleet management, and work orders', FALSE, TRUE, 'MANAGEMENT'),
('BILLING_DASHBOARD', 'Billing Dashboard', 'Advanced billing analytics and cost optimization features', FALSE, TRUE, 'ANALYTICS'),
('ADVANCED_ANALYTICS', 'Advanced Analytics', 'Enhanced analytics and reporting capabilities', FALSE, TRUE, 'ANALYTICS'),
('DEVICE_MANAGEMENT', 'Device Management', 'Advanced device management and IoT integration', FALSE, TRUE, 'DEVICES'),
('API_ACCESS', 'API Access', 'REST API access for integrations', FALSE, TRUE, 'INTEGRATION'),
('PARTNER_SERVICES', 'Partner Services', 'Access to partner ecosystem, integrations, and third-party services', FALSE, TRUE, 'PARTNERSHIPS'),
('APPLIANCE_MONITORING', 'Appliance Monitoring', 'Individual appliance power consumption tracking and efficiency analysis', FALSE, TRUE, 'MONITORING');

-- Insert subscription-feature mappings
INSERT INTO subscription_features (subscription_plan_id, feature_flag_id, is_enabled)
SELECT sp.id, ff.id,
    CASE
        WHEN sp.plan_key = 'FREEMIUM' THEN FALSE
        WHEN sp.plan_key = 'PREMIUM' THEN
            CASE WHEN ff.feature_key IN ('FACILITY360', 'BILLING_DASHBOARD', 'PARTNER_SERVICES', 'APPLIANCE_MONITORING') THEN TRUE ELSE FALSE END
        WHEN sp.plan_key = 'ENTERPRISE' THEN TRUE
        ELSE FALSE
    END
FROM subscription_plans sp
CROSS JOIN feature_flags ff;

-- Indexes for better performance
CREATE INDEX idx_feature_flags_key ON feature_flags(feature_key);
CREATE INDEX idx_subscription_plans_key ON subscription_plans(plan_key);
CREATE INDEX idx_user_subscriptions_user_id ON user_subscriptions(user_id);
CREATE INDEX idx_subscription_features_plan_id ON subscription_features(subscription_plan_id);
