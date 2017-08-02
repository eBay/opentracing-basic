package com.ebay.opentracing.basic;

import io.opentracing.Span;

/**
 * Default {@link SpanInitiator} implementation.
 *
 * @param <T> trace context type
 */
final class SpanInitiatorImpl<T> implements SpanInitiator<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Span initiateSpan(
            SpanInitiatorContext<T> initiatorContext,
            MutableSpanData<T> spanData) {
        return initiatorContext.createSpan(spanData);
    }

}
