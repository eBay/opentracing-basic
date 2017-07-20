package com.ebay.opentracing.basic;

/**
 * {@link SampleController} implementation which always returns {@code true}.
 *
 * @param <T> trace context type
 */
final class SampleControllerAlways<T> implements SampleController<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSampled(SpanData<T> spanData) {
        return true;
    }

}
