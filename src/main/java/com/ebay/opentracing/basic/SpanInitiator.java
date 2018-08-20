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

import io.opentracing.Span;

/**
 * Interface used to define the API for obtaining knowledge of span creation when using the {@link BasicTracer}.
 * The {@link io.opentracing.Tracer.SpanBuilder} implementation will use this to create new {@link Span} instances.
 * This hook allows for complex interactions, such as sampling, trace span aggregation, and more to be implemented
 * and integrated with the {@link BasicTracer}.
 */
public interface SpanInitiator<T> {

    /**
     * Initiate a {@link Span} given the current initiator context and the provided span data.
     *
     * There is no need to worry about activating the span when implementing an initiator, though the initiator
     * context does provide access to the {@link io.opentracing.ScopeManager} to expose any potential currently
     * active span.
     *
     * @param initiatorContext context in which the span is being created, providing access to the necessary internals
     * @param spanData the span data being turned into a {@link Span}
     * @return new span instance
     * @see SpanInitiatorContext#createSpan(MutableSpanData)
     */
    Span initiateSpan(
            SpanInitiatorContext<T> initiatorContext,
            MutableSpanData<T> spanData);

}
