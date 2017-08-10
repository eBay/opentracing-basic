/*
 * Copyright (c) 2017 eBay Inc.
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
