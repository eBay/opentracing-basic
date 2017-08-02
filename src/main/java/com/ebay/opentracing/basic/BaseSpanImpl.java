package com.ebay.opentracing.basic;

import io.opentracing.BaseSpan;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Base class for the span implementation which implements the methods in the corresponding
 * {@link BaseSpan} interface.
 *
 * @param <S> span class type
 * @param <T> trace context type
 */
abstract class BaseSpanImpl<S extends BaseSpan<S>, T> implements BaseSpan<S> {
    private static final String DEFAULT_EVENT_NAME = "event";

    private final MutableSpanData<T> spanState;

    BaseSpanImpl(MutableSpanData<T> spanState) {
        this.spanState = TracerPreconditions.checkNotNull(spanState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SpanContext context() {
        return spanState.getSpanContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final S setTag(String key, String value) {
        spanState.putTag(key, value);
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final S setTag(String key, boolean value) {
        spanState.putTag(key, Boolean.toString(value));
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final S setTag(String key, Number value) {
        spanState.putTag(key, value.toString());
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(String event) {
        return log(TimeUnit.MILLISECONDS, System.currentTimeMillis(), DEFAULT_EVENT_NAME, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(long timestampMicroseconds, String event) {
        return log(timestampMicroseconds, DEFAULT_EVENT_NAME, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final S setBaggageItem(String key, String value) {
        InternalSpanContext<T> spanContext = spanState.getSpanContext();
        spanContext.setBaggageItem(key, value);
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getBaggageItem(String key) {
        InternalSpanContext<T> spanContext = spanState.getSpanContext();
        Baggage baggage = spanContext.getBaggage();
        return baggage.getItem(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final S setOperationName(String operationName) {
        spanState.setOperationName(operationName);
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(String eventName, Object payload) {
        return log(TimeUnit.MILLISECONDS, System.currentTimeMillis(), eventName, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(long timestampMicroseconds, String eventName, Object payload) {
        return log(TimeUnit.MICROSECONDS, timestampMicroseconds, eventName, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(long timestampMicroseconds, Map<String, ?> fields) {
        return logAll(TimeUnit.MICROSECONDS, timestampMicroseconds, fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final S log(Map<String, ?> fields) {
        return logAll(TimeUnit.MILLISECONDS, System.currentTimeMillis(), fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return spanState.toString();
    }

    @SuppressWarnings("unchecked")
    private S log(TimeUnit timeUnit, long timeStamp, String eventName, Object payload) {
        LogEvent logEvent = new LogEvent(timeUnit, timeStamp, eventName, payload);
        spanState.addLogEvent(logEvent);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    private S logAll(TimeUnit timeUnit, long timeStamp, Map<String, ?> fields) {
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            log(timeUnit, timeStamp, entry.getKey(), entry.getValue());
        }
        return (S) this;
    }

}
