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

import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;

/**
 * Interface used to define the ability to marshal span context data to and from a particular
 * data type.  This works around the funky OpenTracing API (i.e., {@link Format} and its uses in
 * {@link io.opentracing.Tracer#inject(SpanContext, Format, Object)} /
 * {@link io.opentracing.Tracer#extract(Format, Object)}) that are difficult to define and use.
 *
 * @param <T> trace context type
 * @param <C> carrier data object type
 */
public interface Formatter<T, C> {

    /**
     * Performs the work of {@link io.opentracing.Tracer#inject(SpanContext, Format, Object)} to apply
     * span context to a carrier data instance.
     *
     * @param spanContext span context
     * @param carrier     carrier data container
     */
    void inject(InternalSpanContext<T> spanContext, C carrier);

    /**
     * Performs the work of {@link io.opentracing.Tracer#extract(Format, Object)} to extract span context data
     * from a carrier data instance.
     *
     * @param carrier carrier data container
     * @return reconstituted span context
     */
    InternalSpanContext<T> extract(C carrier);

}
