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

import java.util.List;
import java.util.Map;

/**
 * Interface used to encapsulate required interactions with a provider of trace context objects.
 */
public interface TraceContextHandler<T> {

    /**
     * Create a new span context which is not associated with any other spans.  i.e., a new root/initial span.
     *
     * @return span context
     */
    InternalSpanContext<T> createNew();

    /**
     * Create a new span context given the span's configured references.  This would typically look for
     * child-of or follows-from relationships and create a new span context configured with the same
     * trace ID but a new/unique span ID.
     *
     * @param references reference map
     * @return span context
     */
    InternalSpanContext<T> createForContext(Map<String, List<InternalSpanContext<T>>> references);

}
