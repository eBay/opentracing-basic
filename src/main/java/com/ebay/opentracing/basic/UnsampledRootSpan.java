package com.ebay.opentracing.basic;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;

/**
 * {@link Span} implementation that performs no work but exposes access to and passes along existing {@link SpanContext}
 * data.  This allows for the collection of baggage context prior to the first sampled span.
 */
final class UnsampledRootSpan<T> implements Span {

    private InternalSpanContext<T> spanContext;

    UnsampledRootSpan(InternalSpanContext<T> spanContext) {
        this.spanContext = spanContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanContext context() {
        return spanContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setTag(String key, String value) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setTag(String key, boolean value) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setTag(String key, Number value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(Map<String, ?> fields) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(String event) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(long timestampMicroseconds, String event) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setBaggageItem(String key, String value) {
        /*
         * According to the OpenTracing folks baggage should continue to be gathered in the un-sampled case
		 * since down-stream spans may be sampled.
		 */
        spanContext.setBaggageItem(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaggageItem(String key) {
        return spanContext.getBaggage().getItem(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setOperationName(String operationName) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(String eventName, Object payload) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        // No-op
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish(long finishMicros) {
        // No-op
    }

}
