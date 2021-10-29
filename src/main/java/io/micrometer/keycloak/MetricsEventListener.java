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
import io.micrometer.core.instrument.Tags;
import io.micrometer.keycloak.provider.StatsdMeterRegistryProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

public class MetricsEventListener implements EventListenerProvider {

    public final static String ID = "metrics-listener";
    private final static String PROVIDER_KEYCLOAK_OPENID = "keycloak";

    private final MeterRegistryFacade meterRegistryFacade;

    public MetricsEventListener() {
        // We use StatsD, but could be interchanged here
        final MeterRegistry statsdMeterRegistry = new StatsdMeterRegistryProvider().getMeterRegistry();

        this.meterRegistryFacade = new MeterRegistryFacade(statsdMeterRegistry);
    }

    @Override
    public void onEvent(Event event) {
        switch (event.getType()) {
            case LOGIN:
                meterRegistryFacade.getMeterRegistry().counter("keycloak.logins", Tags.of(
                        "realm", nullToUnknown(event.getRealmId()),
                        "client.id", nullToUnknown(event.getClientId()),
                        "status", "success"
                ).and(getIdentityProviderAsTag(event))).increment();
                break;
            case LOGIN_ERROR:
                meterRegistryFacade.getMeterRegistry().counter("keycloak.logins", Tags.of(
                        "realm", nullToUnknown(event.getRealmId()),
                        "client.id", nullToUnknown(event.getClientId()),
                        "error", event.getError(),
                        "status", "error"
                ).and(getIdentityProviderAsTag(event))).increment();
                break;
            case REGISTER:
                meterRegistryFacade.getMeterRegistry().counter("keycloak.registrations", Tags.of(
                        "realm", nullToUnknown(event.getRealmId()),
                        "client.id", nullToUnknown(event.getClientId()),
                        "status", "success"
                ).and(getIdentityProviderAsTag(event))).increment();
                break;
            case REGISTER_ERROR:
                meterRegistryFacade.getMeterRegistry().counter("keycloak.registrations", Tags.of(
                        "realm", nullToUnknown(event.getRealmId()),
                        "client.id", nullToUnknown(event.getClientId()),
                        "error", event.getError(),
                        "status", "error"
                ).and(getIdentityProviderAsTag(event))).increment();
                break;
            default:
                meterRegistryFacade.getMeterRegistry().counter("keycloak.events", Tags.of(
                        Tag.of("realm", nullToUnknown(event.getRealmId())),
                        Tag.of("type", event.getType().toString()),
                        getIdentityProviderAsTag(event)
                )).increment();
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        meterRegistryFacade.getMeterRegistry().counter("keycloak.admin.events", Tags.of(
                "realm", nullToUnknown(event.getRealmId()),
                "operation.type", event.getOperationType().toString(),
                "resource.type", nullToUnknown(event.getResourceTypeAsString())
        )).increment();
    }

    private String nullToUnknown(String value) {
        return value == null ? "unknown" : value;
    }

    /**
     * Retrieve the identity prodiver name from event details or
     * default to {@value #PROVIDER_KEYCLOAK_OPENID}.
     *
     * @param event User event
     * @return Identity provider tag
     */
    private Tag getIdentityProviderAsTag(Event event) {
        String identityProvider = null;
        if (event.getDetails() != null) {
            identityProvider = event.getDetails().get("identity_provider");
        }
        if (identityProvider == null) {
            identityProvider = PROVIDER_KEYCLOAK_OPENID;
        }
        return Tag.of("provider", identityProvider);
    }


    @Override
    public void close() {
        // unused
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistryFacade.getMeterRegistry();
    }

}
