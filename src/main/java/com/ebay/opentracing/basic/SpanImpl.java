package com.ebay.opentracing.basic;

import io.opentracing.Span;

import java.util.concurrent.TimeUnit;

final class SpanImpl<T> extends BaseSpanImpl<Span, T> implements Span {
    private final MutableSpanData<T> spanState;
    private final SpanFinisher<T> spanFinisher;

    SpanImpl(MutableSpanData<T> spanState, SpanFinisher<T> spanFinisher) {
        super(spanState);
        this.spanState = spanState;
        this.spanFinisher = spanFinisher;
    }

    @Override
    public void finish() {
        spanFinisher.finish(spanState);
    }

    @Override
    public void finish(long finishMicros) {
        spanFinisher.finish(spanState, TimeUnit.MICROSECONDS, finishMicros);
    }

}
