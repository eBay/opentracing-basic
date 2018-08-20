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

import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * This interface exposes the requisite internals of the {@link BasicTracer} instance so that the {@link SpanInitiator}
 * has enough knowledge to intelligently integrate.
 *
 * @param <T> trace context type
 */
public interface SpanInitiatorContext<T> {

    /**
     * Get the {@link ScopeManager} being used by the tracer.
     *
     * @return source instance
     */
    ScopeManager getScopeManager();

    /**
     * Create a span instance from the {@link MutableSpanData} provided.
     *
     * @param spanData span data
     * @return span instance
     */
    Span createSpan(MutableSpanData<T> spanData);

}
