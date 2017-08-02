package com.ebay.opentracing.basic;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
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
        ActiveSpanSource activeSpanSource = initiatorContext.getActiveSpanSource();
        SampleController<T> sampleController = initiatorContext.getSampleController();

        ActiveSpan activeSpan = activeSpanSource.activeSpan();
        if (activeSpan != null || sampleController.isSampled(spanData)) {
            return initiatorContext.createSpan(spanData);
        } else {
            return new UnsampledRootSpan<>(spanData.getSpanContext());
        }
    }

}
