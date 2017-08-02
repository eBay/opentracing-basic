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
    private SampleController<T> sampleController;
    private SpanFinisher<T> spanFinisher;

    SpanInitiatorContextImpl(
            ActiveSpanSource activeSpanSource,
            SampleController<T> sampleController,
            SpanFinisher<T> spanFinisher) {
        this.activeSpanSource = activeSpanSource;
        this.sampleController = sampleController;
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
    public SampleController<T> getSampleController() {
        return sampleController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span createSpan(MutableSpanData<T> spanData) {
        return new SpanImpl<>(spanData, spanFinisher);
    }

}
