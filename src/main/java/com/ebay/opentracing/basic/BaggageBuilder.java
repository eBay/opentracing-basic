package com.ebay.opentracing.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder used to create new {@link Baggage} instances.
 */
@SuppressWarnings("WeakerAccess") // API class
public final class BaggageBuilder {
    private int totalEntries;
    private List<Map<String, String>> inherited;
    private Map<String, String> local;

    /**
     * Create a new builder instance which inherits baggage items from the list of ancestors provided.
     * Ancestors later in the list take precedence over those earlier in the list when the same key is
     * present in both.
     *
     * @param baggages ancestor baggage instances
     * @return builder instance
     */
    public BaggageBuilder inheritAll(List<Baggage> baggages) {
        TracerPreconditions.checkNotNull(baggages, "baggages instance may not be null");

        if (inherited == null)
            inherited = new ArrayList<>(baggages.size() + 4);
        for (Baggage baggage : baggages)
            inherited.add(baggage.getAsMap());
        return this;
    }

    /**
     * Create a new builder instance which inherits baggage items from the ancestor provided.
     *
     * @param baggage ancestor baggage instance
     * @return builder instance
     */
    public BaggageBuilder inherit(Baggage baggage) {
        TracerPreconditions.checkNotNull(baggage, "baggage instance may not be null");

        if (inherited == null)
            inherited = new ArrayList<>(4);

        Map<String, String> map = baggage.getAsMap();
        inherited.add(map);
        totalEntries += map.size();
        return this;
    }

    /**
     * Define a new key/value pair for the baggage.
     *
     * @param key   item key
     * @param value item value
     * @return builder instance
     */
    public BaggageBuilder put(String key, String value) {
        TracerPreconditions.checkNotNull(key, "key may not be null");

        // TODO - This one is questionable as it could be used to remove existing baggage
        TracerPreconditions.checkNotNull(value, "value may not be null");

        if (local == null)
            local = new HashMap<>(4);
        local.put(key, value);
        totalEntries++;

        return this;
    }

    /**
     * Builds the {@link Baggage} instance.
     *
     * @return new baggage instance
     */
    public Baggage build() {
        HashMap<String, String> map = new HashMap<>(totalEntries);
        if (inherited != null) {
            for (Map<String, String> inherit : inherited)
                map.putAll(inherit);
        }
        if (local != null)
            map.putAll(local);
        return new Baggage(Collections.unmodifiableMap(map));
    }
}
