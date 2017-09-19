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
import java.util.concurrent.TimeUnit;

/**
 * Interface providing access to a span's data.
 *
 * @param <T> trace context type
 */
public interface SpanData<T> {

    /**
     * Get the span context.
     *
     * @return span context instance
     */
    InternalSpanContext<T> getSpanContext();

    /**
     * Get the span start time.
     *
     * @param timeUnit time unit that the result should be returned in
     * @return time stamp
     */
    long getStartTime(TimeUnit timeUnit);

    /**
     * Get the span finish time.
     *
     * @param timeUnit time unit that the result should be returned in
     * @return time stamp
     */
    long getFinishTime(TimeUnit timeUnit);

    /**
     * Get the span's operation name.
     *
     * @return operation name
     */
    String getOperationName();

    /**
     * Get the map of all tags which have been applied to the span.
     *
     * @return thread-safe map of tags
     */
    Map<String, String> getTags();

    /**
     * Get a list of all causal span references of the specified type.
     *
     * @param referenceType reference type
     * @return list of references
     * @see io.opentracing.References
     */
    List<? extends InternalSpanContext<T>> getReferences(String referenceType);

    /**
     * Get a list of all logged events.
     *
     * @return log events
     */
    List<LogEvent> getLogEvents();

}
