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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

class MicrometerKeycloakTest {

    private static final String DEFAULT_REALM = "myrealm";
    private static final String CLIENT_ID = "THE_CLIENT_ID";
    private static final String IDENTITY_PROVIDER = "THE_ID_PROVIDER";

    private final MetricsEventListener listener = new MetricsEventListener();

    @BeforeEach
    void beforeEach() {
        this.listener.getMeterRegistry().clear();
    }

    @Test
    void shouldCorrectlyCountLoginWhenIdentityProviderIsDefined() {
        updateEnv("OWNER", "TEST");
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.LOGIN, IDENTITY_PROVIDER));
        listener.getMeterRegistry().get("keycloak.logins")
                .tag("Owner", "TEST")
                .tag("provider", IDENTITY_PROVIDER)
                .tag("realm", DEFAULT_REALM)
                .tag("client.id", CLIENT_ID)
                .tag("status", "success")
                .counter();
    }

    @Test
    void shouldCorrectlyCountLoginWhenIdentityProviderIsNotDefined() {
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.LOGIN));
        listener.getMeterRegistry().get("keycloak.logins")
                .tag("provider", "keycloak")
                .tag("realm", DEFAULT_REALM)
                .tag("client.id", CLIENT_ID)
                .tag("status", "success")
                .counter();
    }

    @Test
    void shouldCorrectlyCountLoginError() {
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.LOGIN_ERROR, IDENTITY_PROVIDER, "user_not_found"));
        listener.getMeterRegistry().get("keycloak.logins")
                .tag("provider", IDENTITY_PROVIDER)
                .tag("realm", DEFAULT_REALM)
                .tag("client.id", CLIENT_ID)
                .tag("error", "user_not_found")
                .tag("status", "error")
                .counter();
    }

    @Test
    void shouldCorrectlyCountRegister() {
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.REGISTER, IDENTITY_PROVIDER));
        listener.getMeterRegistry().get("keycloak.registrations")
                .tag("provider", IDENTITY_PROVIDER)
                .tag("realm", DEFAULT_REALM)
                .tag("client.id", CLIENT_ID)
                .tag("status", "success")
                .counter();
    }

    @Test
    void shouldCorrectlyRecordGenericEvents() {
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.UPDATE_EMAIL));
        listener.getMeterRegistry().get("keycloak.events")
                .tag("realm", DEFAULT_REALM)
                .tag("type", EventType.UPDATE_EMAIL.toString())
                .tag("provider", "keycloak")
                .counter();
    }

    @Test
    void shouldCorrectlyRecordJvmMetrics() {
        updateEnv("OWNER", "TEST");
        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(createEvent(EventType.UPDATE_EMAIL));
        listener.getMeterRegistry().get("jvm.memory.max")
                .tag("Owner", "TEST")
                .gauge();
    }

    @Test
    void shouldCorrectlyRecordGenericAdminEvents() {
        AdminEvent event = new AdminEvent();
        event.setOperationType(OperationType.ACTION);
        event.setResourceType(ResourceType.AUTHORIZATION_SCOPE);
        event.setRealmId(DEFAULT_REALM);

        MetricsEventListener listener = new MetricsEventListener();
        listener.onEvent(event, true);
        listener.getMeterRegistry().get("keycloak.admin.events")
                .tag("realm", DEFAULT_REALM)
                .tag("operation.type", OperationType.ACTION.toString())
                .tag("resource.type", ResourceType.AUTHORIZATION_SCOPE.toString())
                .counter();
    }

    @Test
    void shouldTolerateNullLabels() {
        Event event = new Event();
        event.setType(EventType.CLIENT_DELETE);
        event.setRealmId(null);

        listener.onEvent(event);
        listener.getMeterRegistry().get("keycloak.events")
                .tag("type", EventType.CLIENT_DELETE.toString())
                .tag("realm", "unknown")
                .counter();
    }

    private Event createEvent(EventType type, String provider, String error) {
        Event event = new Event();
        event.setType(type);
        event.setRealmId(DEFAULT_REALM);
        event.setClientId(CLIENT_ID);
        event.setDetails(Collections.emptyMap());

        if (provider != null) {
           event.setDetails(Map.of("identity_provider", provider));
        }

        if (error != null) {
            event.setError(error);
        }

        return event;
    }

    private Event createEvent(EventType type, String provider) {
        return createEvent(type, provider, null);
    }

    private Event createEvent(EventType type) {
        return createEvent(type, null, null);
    }

    @SuppressWarnings({ "unchecked" })
    public static void updateEnv(String name, String val){
        Map<String, String> env = System.getenv();
        Field field = null;
        try {
            field = env.getClass().getDeclaredField("m");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            ((Map<String, String>) field.get(env)).put(name, val);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
