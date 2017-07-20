package com.ebay.opentracing.basic;

import io.opentracing.propagation.Format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps {@link Format}s to a {@link Formatter} instance that has been registered to apply the format.
 *
 * @param <T> trace context type
 */
final class Formatters<T> {
    private final Map<Format<?>, Formatter<T, ?>> formatters = new ConcurrentHashMap<>();

    /**
     * Register a formatter for a specified {@link Format}.
     *
     * @param format    format instance
     * @param formatter formatter instance
     * @param <C>       carrier data type
     */
    <C> void register(Format<C> format, Formatter<T, C> formatter) {
        TracerPreconditions.checkNotNull(format);
        TracerPreconditions.checkNotNull(formatter);
        formatters.put(format, formatter);
    }

    /**
     * Get the formatter for a specified {@link Format}.
     *
     * @param format format instance
     * @param <C>    carrier data type
     * @return formatter instance or {@code null} if no registration was found
     */
    <C> Formatter<T, C> get(Format<C> format) {
        TracerPreconditions.checkNotNull(format);

        @SuppressWarnings("unchecked") // Protected via strong typing at registration point
                Formatter<T, C> formatter = (Formatter<T, C>) formatters.get(format);
        if (formatter == null)
            throw new UnsupportedOperationException("Format not supported: " + format);
        return formatter;
    }

}
