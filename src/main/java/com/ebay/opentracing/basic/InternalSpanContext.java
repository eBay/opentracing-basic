package com.ebay.opentracing.basic;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link SpanContext} API defined by OpenTracing identifies the fact that {@link Span} data encompasses
 * two classes of data:  Data that is internal to the tracing implementation and user-level baggage.  This
 * class provides an implementation of the {@link SpanContext} interface and maintains that separation.  The
 * tracing implementation- specific portion is encapsulated within an arbitrary "trace context" object instance.
 * The user-level baggage is managed separately in a {@link Baggage} instance.
 *
 * @param <T> trace context type
 */
@SuppressWarnings("WeakerAccess") // API class
public final class InternalSpanContext<T> implements SpanContext {
    private final T traceContext;
    private AtomicReference<Baggage> baggageRef;

    public InternalSpanContext(T traceContext, Baggage baggage) {
        this.traceContext = traceContext;
        this.baggageRef = new AtomicReference<>(baggage);
    }

    public T getTraceContext() {
        return traceContext;
    }

    public Baggage getBaggage() {
        return baggageRef.get();
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return getBaggage().baggageItems();
    }

    @Override
    public String toString() {
        return "SpanContext{" + traceContext + "}";
    }

    void setBaggageItem(String key, String value) {
        boolean success;
        do {
            Baggage original = baggageRef.get();
            Baggage replacement = new BaggageBuilder()
                    .inherit(original)
                    .put(key, value)
                    .build();
            success = baggageRef.compareAndSet(original, replacement);
        } while (!success);
    }

}
