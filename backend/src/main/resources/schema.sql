-- ============================================================
-- PVMS MySQL Schema (v2.0.0)
-- Compatible with both MySQL 8.x and H2 (MODE=MySQL)
-- ============================================================

-- Dashboard
DROP TABLE IF EXISTS dashboard_alarm_snapshot;
DROP TABLE IF EXISTS dashboard_station_status_snapshot;
DROP TABLE IF EXISTS dashboard_vpp_node_snapshot;
DROP TABLE IF EXISTS dashboard_station;

-- Strategy
DROP TABLE IF EXISTS sg_strategy_snapshot;
DROP TABLE IF EXISTS sg_revenue_daily;
DROP TABLE IF EXISTS sg_price_period;
DROP TABLE IF EXISTS sg_execution_log;
DROP TABLE IF EXISTS sg_strategy_period;
DROP TABLE IF EXISTS sg_strategy;

-- Forecast
DROP TABLE IF EXISTS fc_adjustable_window;
DROP TABLE IF EXISTS fc_monthly_accuracy_snapshot;
DROP TABLE IF EXISTS fc_error_sample;
DROP TABLE IF EXISTS fc_prediction_series_15m;
DROP TABLE IF EXISTS fc_station_model_binding;
DROP TABLE IF EXISTS fc_model;

-- Station Archive
DROP TABLE IF EXISTS sa_inverter_alarm;
DROP TABLE IF EXISTS sa_station_strategy;
DROP TABLE IF EXISTS sa_inverter;
DROP TABLE IF EXISTS sa_station_curve_15m;
DROP TABLE IF EXISTS sa_station;
DROP TABLE IF EXISTS sa_company;

-- Production Monitor
DROP TABLE IF EXISTS pm_station_curve_15m;
DROP TABLE IF EXISTS pm_dispatch_record;
DROP TABLE IF EXISTS pm_weather_snapshot;
DROP TABLE IF EXISTS pm_station_realtime_snapshot;
DROP TABLE IF EXISTS pm_station;
DROP TABLE IF EXISTS pm_resource_unit;

-- ======================== Dashboard ========================

CREATE TABLE dashboard_station (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    resource_unit_id VARCHAR(32) NOT NULL,
    resource_unit_name VARCHAR(128) NOT NULL,
    region VARCHAR(64) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    address VARCHAR(255) NOT NULL,
    capacity_kwp DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dashboard_station_status_snapshot (
    station_id VARCHAR(32) NOT NULL PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    realtime_power_kw DECIMAL(12,2) NOT NULL,
    today_energy_kwh DECIMAL(12,2) NOT NULL,
    today_revenue_cny DECIMAL(12,2) NOT NULL,
    availability DECIMAL(5,2) NOT NULL,
    health_grade VARCHAR(16) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_dashboard_station_status_station
        FOREIGN KEY (station_id) REFERENCES dashboard_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dashboard_alarm_snapshot (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    station_id VARCHAR(32) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    level VARCHAR(32) NOT NULL,
    level_label VARCHAR(32) NOT NULL,
    device_name VARCHAR(64) NOT NULL,
    alarm_type VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    owner VARCHAR(64) NOT NULL,
    suggestion VARCHAR(255) NOT NULL,
    CONSTRAINT fk_dashboard_alarm_station
        FOREIGN KEY (station_id) REFERENCES dashboard_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dashboard_vpp_node_snapshot (
    node_id VARCHAR(32) NOT NULL PRIMARY KEY,
    total_capacity_mw DECIMAL(12,2) NOT NULL,
    adjustable_min_mw DECIMAL(12,2) NOT NULL,
    adjustable_max_mw DECIMAL(12,2) NOT NULL,
    last_heartbeat TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =================== Production Monitor ====================

CREATE TABLE pm_resource_unit (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    region VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    cluster_radius_km INT NOT NULL,
    dispatch_mode VARCHAR(128) NOT NULL,
    strategy_label VARCHAR(255) NOT NULL,
    latest_alarm_title VARCHAR(255) NOT NULL,
    latest_alarm_time TIMESTAMP NOT NULL,
    alarm_total INT NOT NULL,
    alarm_critical INT NOT NULL,
    alarm_major INT NOT NULL,
    alarm_minor INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pm_station (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    resource_unit_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    capacity_mw DECIMAL(10,3) NOT NULL,
    weight DECIMAL(8,4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    online_rate DECIMAL(5,2) NOT NULL,
    alarm_count INT NOT NULL,
    sort_index INT NOT NULL,
    load_visible BOOLEAN NOT NULL,
    load_base_kw DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_pm_station_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pm_station_realtime_snapshot (
    station_id VARCHAR(32) NOT NULL PRIMARY KEY,
    snapshot_time TIMESTAMP NOT NULL,
    realtime_power_kw DECIMAL(12,2) NOT NULL,
    load_kw DECIMAL(12,2) NOT NULL,
    availability DECIMAL(5,2) NOT NULL,
    health_grade VARCHAR(16) NOT NULL,
    CONSTRAINT fk_pm_snapshot_station
        FOREIGN KEY (station_id) REFERENCES pm_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pm_weather_snapshot (
    resource_unit_id VARCHAR(32) NOT NULL PRIMARY KEY,
    snapshot_time TIMESTAMP NOT NULL,
    weather VARCHAR(32) NOT NULL,
    cloudiness VARCHAR(32) NOT NULL,
    temperature INT NOT NULL,
    irradiance INT NOT NULL,
    humidity INT NOT NULL,
    wind_speed DECIMAL(5,2) NOT NULL,
    conclusion VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pm_weather_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pm_station_curve_15m (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    time_slot INT NOT NULL,
    load_kw DECIMAL(12,2) NOT NULL,
    pv_power_kw DECIMAL(12,2) NOT NULL,
    forecast_power_kw DECIMAL(12,2) NOT NULL,
    baseline_power_kw DECIMAL(12,2) NOT NULL,
    irradiance INT NOT NULL,
    temperature DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (station_id, biz_date, time_slot),
    CONSTRAINT fk_pm_curve_station
        FOREIGN KEY (station_id) REFERENCES pm_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pm_dispatch_record (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    resource_unit_id VARCHAR(32) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    command_type VARCHAR(32) NOT NULL,
    target_power_mw DECIMAL(10,2) NOT NULL,
    actual_power_mw DECIMAL(10,2) NOT NULL,
    response_seconds INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    deviation_reason VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pm_dispatch_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== Station Archive ======================

CREATE TABLE sa_company (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    region VARCHAR(64) NOT NULL,
    sort_index INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sa_station (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    company_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    capacity_kwp DECIMAL(12,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    data_quality VARCHAR(32) NOT NULL,
    grid_status VARCHAR(32) NOT NULL,
    grid_status_label VARCHAR(32) NOT NULL,
    commission_date DATE NOT NULL,
    address VARCHAR(255) NOT NULL,
    load_base_kw DECIMAL(12,2) NOT NULL,
    sort_index INT NOT NULL,
    CONSTRAINT fk_sa_station_company
        FOREIGN KEY (company_id) REFERENCES sa_company(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sa_station_curve_15m (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    time_slot INT NOT NULL,
    load_kw DECIMAL(12,2) NOT NULL,
    pv_output_kw DECIMAL(12,2) NOT NULL,
    forecast_day_ahead_kw DECIMAL(12,2) NOT NULL,
    forecast_ultra_short_kw DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (station_id, biz_date, time_slot),
    CONSTRAINT fk_sa_curve_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sa_inverter (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    station_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    sort_index INT NOT NULL,
    rated_power_kw DECIMAL(12,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    model VARCHAR(64) NOT NULL,
    manufacturer VARCHAR(64) NOT NULL,
    serial_no VARCHAR(64) NOT NULL,
    firmware_version VARCHAR(64) NOT NULL,
    install_date DATE NOT NULL,
    mppt_channels INT NOT NULL,
    dc_input_voltage_v DECIMAL(12,2) NOT NULL,
    ac_output_voltage_v DECIMAL(12,2) NOT NULL,
    grid_frequency_hz DECIMAL(5,2) NOT NULL,
    module_temperature_c DECIMAL(5,2) NOT NULL,
    ambient_temperature_c DECIMAL(5,2) NOT NULL,
    string_count INT NOT NULL,
    panels_per_string INT NOT NULL,
    CONSTRAINT fk_sa_inverter_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sa_station_strategy (
    station_id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    target_power_kw DECIMAL(12,2) NOT NULL,
    estimated_revenue_cny DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sa_strategy_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sa_inverter_alarm (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    inverter_id VARCHAR(32) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    type VARCHAR(64) NOT NULL,
    level VARCHAR(32) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT fk_sa_alarm_inverter
        FOREIGN KEY (inverter_id) REFERENCES sa_inverter(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================== Forecast =========================

CREATE TABLE fc_model (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    version VARCHAR(32) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fc_station_model_binding (
    station_id VARCHAR(32) NOT NULL PRIMARY KEY,
    day_ahead_model_id VARCHAR(32) NOT NULL,
    ultra_short_model_id VARCHAR(32) NOT NULL,
    confidence_level DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_fc_binding_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id),
    CONSTRAINT fk_fc_binding_day_ahead
        FOREIGN KEY (day_ahead_model_id) REFERENCES fc_model(id),
    CONSTRAINT fk_fc_binding_ultra_short
        FOREIGN KEY (ultra_short_model_id) REFERENCES fc_model(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fc_prediction_series_15m (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    time_slot INT NOT NULL,
    forecast_type VARCHAR(32) NOT NULL,
    predicted_power_kw DECIMAL(12,2) NOT NULL,
    upper_bound_kw DECIMAL(12,2) NOT NULL,
    lower_bound_kw DECIMAL(12,2) NOT NULL,
    scenario_tag VARCHAR(32) NOT NULL,
    PRIMARY KEY (station_id, biz_date, time_slot, forecast_type),
    CONSTRAINT fk_fc_prediction_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fc_error_sample (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    hour_slot INT NOT NULL,
    forecast_type VARCHAR(32) NOT NULL,
    error_kw DECIMAL(12,2) NOT NULL,
    qualified BOOLEAN NOT NULL,
    PRIMARY KEY (station_id, biz_date, hour_slot, forecast_type),
    CONSTRAINT fk_fc_error_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fc_monthly_accuracy_snapshot (
    station_id VARCHAR(32) NOT NULL,
    month_key VARCHAR(8) NOT NULL,
    forecast_type VARCHAR(32) NOT NULL,
    mae_kw DECIMAL(12,2) NOT NULL,
    rmse_kw DECIMAL(12,2) NOT NULL,
    accuracy_pct DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (station_id, month_key, forecast_type),
    CONSTRAINT fk_fc_monthly_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fc_adjustable_window (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    window_order INT NOT NULL,
    start_slot INT NOT NULL,
    end_slot INT NOT NULL,
    window_status VARCHAR(32) NOT NULL,
    PRIMARY KEY (station_id, biz_date, window_order),
    CONSTRAINT fk_fc_window_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================== Strategy =========================

CREATE TABLE sg_strategy (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    station_id VARCHAR(32) NOT NULL,
    company_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    mode VARCHAR(32) NOT NULL,
    target_power_kw DECIMAL(12,2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    version_no INT NOT NULL,
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sg_strategy_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id),
    CONSTRAINT fk_sg_strategy_company
        FOREIGN KEY (company_id) REFERENCES sa_company(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sg_strategy_period (
    strategy_id VARCHAR(32) NOT NULL,
    period_order INT NOT NULL,
    start_slot INT NOT NULL,
    end_slot INT NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    target_ratio DECIMAL(6,3) NOT NULL,
    PRIMARY KEY (strategy_id, period_order),
    CONSTRAINT fk_sg_period_strategy
        FOREIGN KEY (strategy_id) REFERENCES sg_strategy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sg_execution_log (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    strategy_id VARCHAR(32) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    action VARCHAR(64) NOT NULL,
    result VARCHAR(32) NOT NULL,
    deviation_rate_pct DECIMAL(6,2) NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    CONSTRAINT fk_sg_log_strategy
        FOREIGN KEY (strategy_id) REFERENCES sg_strategy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sg_price_period (
    station_id VARCHAR(32) NOT NULL,
    period_order INT NOT NULL,
    start_slot INT NOT NULL,
    end_slot INT NOT NULL,
    price_type VARCHAR(16) NOT NULL,
    price_cny_per_kwh DECIMAL(8,3) NOT NULL,
    PRIMARY KEY (station_id, period_order),
    CONSTRAINT fk_sg_price_station
        FOREIGN KEY (station_id) REFERENCES sa_station(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sg_revenue_daily (
    strategy_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    estimated_revenue_cny DECIMAL(12,2) NOT NULL,
    actual_revenue_cny DECIMAL(12,2) NOT NULL,
    peak_saving_cny DECIMAL(12,2) NOT NULL,
    response_reward_cny DECIMAL(12,2) NOT NULL,
    penalty_cny DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (strategy_id, biz_date),
    CONSTRAINT fk_sg_revenue_strategy
        FOREIGN KEY (strategy_id) REFERENCES sg_strategy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sg_strategy_snapshot (
    strategy_id VARCHAR(32) NOT NULL PRIMARY KEY,
    last_simulated_revenue_cny DECIMAL(12,2) NOT NULL,
    confidence_low_cny DECIMAL(12,2) NOT NULL,
    confidence_high_cny DECIMAL(12,2) NOT NULL,
    success_probability_pct DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_sg_snapshot_strategy
        FOREIGN KEY (strategy_id) REFERENCES sg_strategy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
