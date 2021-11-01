# Micrometer Keycloak SPI

This is a fork from [micrometer-keycloak](https://github.com/micrometer-metrics/micrometer-keycloak)

This has been adapted to work with StatsD and DataDog.

---

A [Service Provider](https://www.keycloak.org/docs/4.8/server_development/index.html#_providers) that automatically publishes all metrics to StatsD using the DataDog flavor.

Defined providers:

* `MetricsEventListener` to record the internal Keycloak events

## Usage

Build the jar
    
    ./gradlew jar    

Deploy to JBoss and activate metric listener in Keycloak
