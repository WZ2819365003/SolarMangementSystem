DROP TABLE IF EXISTS dashboard_alarm_snapshot;
DROP TABLE IF EXISTS dashboard_station_status_snapshot;
DROP TABLE IF EXISTS dashboard_vpp_node_snapshot;
DROP TABLE IF EXISTS dashboard_station;
DROP TABLE IF EXISTS pm_station_curve_15m;
DROP TABLE IF EXISTS pm_dispatch_record;
DROP TABLE IF EXISTS pm_weather_snapshot;
DROP TABLE IF EXISTS pm_station_realtime_snapshot;
DROP TABLE IF EXISTS pm_station;
DROP TABLE IF EXISTS pm_resource_unit;

CREATE TABLE dashboard_station (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    resource_unit_id VARCHAR(32) NOT NULL,
    resource_unit_name VARCHAR(128) NOT NULL,
    region VARCHAR(64) NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    address VARCHAR(255) NOT NULL,
    capacity_kwp DECIMAL(12, 2) NOT NULL
);

CREATE TABLE dashboard_station_status_snapshot (
    station_id VARCHAR(32) PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    realtime_power_kw DECIMAL(12, 2) NOT NULL,
    today_energy_kwh DECIMAL(12, 2) NOT NULL,
    today_revenue_cny DECIMAL(12, 2) NOT NULL,
    availability DECIMAL(5, 2) NOT NULL,
    health_grade VARCHAR(16) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_dashboard_station_status_station
        FOREIGN KEY (station_id) REFERENCES dashboard_station(id)
);

CREATE TABLE dashboard_alarm_snapshot (
    id VARCHAR(64) PRIMARY KEY,
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
);

CREATE TABLE dashboard_vpp_node_snapshot (
    node_id VARCHAR(32) PRIMARY KEY,
    total_capacity_mw DECIMAL(12, 2) NOT NULL,
    adjustable_min_mw DECIMAL(12, 2) NOT NULL,
    adjustable_max_mw DECIMAL(12, 2) NOT NULL,
    last_heartbeat TIMESTAMP NOT NULL
);

CREATE TABLE pm_resource_unit (
    id VARCHAR(32) PRIMARY KEY,
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
);

CREATE TABLE pm_station (
    id VARCHAR(32) PRIMARY KEY,
    resource_unit_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    capacity_mw DECIMAL(10, 3) NOT NULL,
    weight DECIMAL(8, 4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    online_rate DECIMAL(5, 2) NOT NULL,
    alarm_count INT NOT NULL,
    sort_index INT NOT NULL,
    load_visible BOOLEAN NOT NULL,
    load_base_kw DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_pm_station_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
);

CREATE TABLE pm_station_realtime_snapshot (
    station_id VARCHAR(32) PRIMARY KEY,
    snapshot_time TIMESTAMP NOT NULL,
    realtime_power_kw DECIMAL(12, 2) NOT NULL,
    load_kw DECIMAL(12, 2) NOT NULL,
    availability DECIMAL(5, 2) NOT NULL,
    health_grade VARCHAR(16) NOT NULL,
    CONSTRAINT fk_pm_station_realtime_snapshot_station
        FOREIGN KEY (station_id) REFERENCES pm_station(id)
);

CREATE TABLE pm_weather_snapshot (
    resource_unit_id VARCHAR(32) PRIMARY KEY,
    snapshot_time TIMESTAMP NOT NULL,
    weather VARCHAR(32) NOT NULL,
    cloudiness VARCHAR(32) NOT NULL,
    temperature INT NOT NULL,
    irradiance INT NOT NULL,
    humidity INT NOT NULL,
    wind_speed DECIMAL(5, 2) NOT NULL,
    conclusion VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pm_weather_snapshot_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
);

CREATE TABLE pm_station_curve_15m (
    station_id VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    time_slot INT NOT NULL,
    load_kw DECIMAL(12, 2) NOT NULL,
    pv_power_kw DECIMAL(12, 2) NOT NULL,
    forecast_power_kw DECIMAL(12, 2) NOT NULL,
    baseline_power_kw DECIMAL(12, 2) NOT NULL,
    irradiance INT NOT NULL,
    temperature DECIMAL(5, 2) NOT NULL,
    PRIMARY KEY (station_id, biz_date, time_slot),
    CONSTRAINT fk_pm_station_curve_station
        FOREIGN KEY (station_id) REFERENCES pm_station(id)
);

CREATE TABLE pm_dispatch_record (
    id VARCHAR(64) PRIMARY KEY,
    resource_unit_id VARCHAR(32) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    command_type VARCHAR(32) NOT NULL,
    target_power_mw DECIMAL(10, 2) NOT NULL,
    actual_power_mw DECIMAL(10, 2) NOT NULL,
    response_seconds INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    deviation_reason VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pm_dispatch_record_resource_unit
        FOREIGN KEY (resource_unit_id) REFERENCES pm_resource_unit(id)
);
