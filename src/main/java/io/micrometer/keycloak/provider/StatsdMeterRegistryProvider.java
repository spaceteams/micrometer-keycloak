package io.micrometer.keycloak.provider;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;

public class StatsdMeterRegistryProvider implements MeterRegistryProvider {

    private final StatsdConfig statsdConfig = new StatsdConfig() {
        @Override
        public boolean enabled() {
            return Boolean.parseBoolean(System.getenv().getOrDefault("MONITORING_ENABLED", "false"));
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public String host() {
            return System.getenv().getOrDefault("STATSD_HOST", "localhost");
        }

        @Override
        public int port() {
            return Integer.parseInt(System.getenv().getOrDefault("STATSD_PORT", "8125"));
        }

        @Override
        public StatsdFlavor flavor() {
            return StatsdFlavor.DATADOG;
        }
    };

    private final StatsdMeterRegistry statsdMeterRegistry = new StatsdMeterRegistry(statsdConfig, Clock.SYSTEM);

    public MeterRegistry getMeterRegistry() {
        return statsdMeterRegistry;
    }

}
