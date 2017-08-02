package com.ebay.opentracing.basic;

import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;

/**
 * {@link SpanInitiatorContext} implementation.
 *
 * @param <T> trace context type
 */
final class SpanInitiatorContextImpl<T> implements SpanInitiatorContext<T> {

    private ActiveSpanSource activeSpanSource;
    private SpanFinisher<T> spanFinisher;

    SpanInitiatorContextImpl(
            ActiveSpanSource activeSpanSource,
            SpanFinisher<T> spanFinisher) {
        this.activeSpanSource = activeSpanSource;
        this.spanFinisher = spanFinisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveSpanSource getActiveSpanSource() {
        return activeSpanSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span createSpan(MutableSpanData<T> spanData) {
        return new SpanImpl<>(spanData, spanFinisher);
    }

}
