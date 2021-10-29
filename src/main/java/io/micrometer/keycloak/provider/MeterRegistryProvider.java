package io.micrometer.keycloak.provider;

import io.micrometer.core.instrument.MeterRegistry;

interface MeterRegistryProvider {
    MeterRegistry getMeterRegistry();
}
