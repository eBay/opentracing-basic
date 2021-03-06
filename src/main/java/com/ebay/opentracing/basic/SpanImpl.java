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

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Base class for the span implementation which implements the methods in the corresponding
 * {@link Span} interface.
 *
 * @param <S> span class type
 * @param <T> trace context type
 */
class SpanImpl<S extends Span, T> implements Span {
    private static final String DEFAULT_EVENT_NAME = "event";

    private final MutableSpanData<T> spanState;
    private final SpanFinisher<T> spanFinisher;

    SpanImpl(MutableSpanData<T> spanState, SpanFinisher<T> spanFinisher) {
        this.spanState = spanState;
        this.spanFinisher = spanFinisher;
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
        return log(TimeUnit.MICROSECONDS, timestampMicroseconds, DEFAULT_EVENT_NAME, event);
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
    public void finish() {
        spanFinisher.finish(spanState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish(long finishMicros) {
        spanFinisher.finish(spanState, TimeUnit.MICROSECONDS, finishMicros);
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
