/**
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.keycloak;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;

import java.util.Arrays;
import java.util.List;

public class MeterRegistryFacade {

    private final MeterRegistry meterRegistry;

    public MeterRegistryFacade(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        new ProcessorMetrics().bindTo(this.meterRegistry);
        new JvmGcMetrics().bindTo(this.meterRegistry);
        new JvmMemoryMetrics().bindTo(this.meterRegistry);
        new JvmThreadMetrics().bindTo(this.meterRegistry);
        new FileDescriptorMetrics().bindTo(this.meterRegistry);
        new ClassLoaderMetrics().bindTo(this.meterRegistry);
        new UptimeMetrics().bindTo(this.meterRegistry);
        List<Tag> commonTags = Arrays.asList(
                Tag.of("Owner", System.getenv().getOrDefault("OWNER", "UNKNOWN_OWNER")),
                Tag.of("Environment", System.getenv().getOrDefault("METRIC_ENVIRONMENT", "UNKNOWN_ENV")),
                Tag.of("Service", System.getenv().getOrDefault("SERVICE_NAME", "UNKNOWN_SERVICE_NAME"))
        );
        this.meterRegistry.config().commonTags(commonTags);
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

}
