/*
 * Copyright (c) 2017-2018 eBay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebay.opentracing.basic;

import io.opentracing.SpanContext;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the baggage portion of a {@link SpanContext}.  Each {@link Baggage} instance is immutable
 * once created, as per the recommendations of the Open Tracing specification.
 */
public final class Baggage implements SpanContext {
    private final Map<String, String> local;

    /**
     * Creates a new {@link Baggage} instance.
     *
     * @param local key/value definitions.  Note that this map must be specifically for this baggage instance and not
     *              shared/used elsewhere
     */
    Baggage(Map<String, String> local) {
        this.local = Objects.requireNonNull(local);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return local.entrySet();
    }

    /**
     * Get the {@link Map} backing the {@link Baggage} instance.
     *
     * @return map instance
     */
    Map<String, String> getAsMap() {
        return local;
    }

    /**
     * Get an individual item from within the {@link Baggage} collection.
     *
     * @param key item key
     * @return item value or {@code null}
     */
    @Nullable
    String getItem(String key) {
        return local.get(key);
    }

}
