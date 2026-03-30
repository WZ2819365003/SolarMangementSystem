DROP TABLE IF EXISTS dashboard_alarm_snapshot;
DROP TABLE IF EXISTS dashboard_station_status_snapshot;
DROP TABLE IF EXISTS dashboard_vpp_node_snapshot;
DROP TABLE IF EXISTS dashboard_station;

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
