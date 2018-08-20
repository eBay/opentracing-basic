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

import java.util.concurrent.TimeUnit;

/**
 * {@link SpanData} implementation which is mutable.
 *
 * @param <T> trace context type
 */
public interface MutableSpanData<T> extends SpanData<T> {

    /**
     * Set the span's operation name.
     *
     * @see io.opentracing.Span#setOperationName(String)
     * @param operationName new operation name
     */
    void setOperationName(String operationName);

    /**
     * Set the span's finish time.
     *
     * @param finishTimeUnit time unit
     * @param finishTimeStamp time stamp
     */
    void setFinishTime(TimeUnit finishTimeUnit, long finishTimeStamp);

    /**
     * Add the specified tag to the span.
     *
     * @param key tag name
     * @param value tag value
     */
    void putTag(String key, String value);

    /**
     * Add a timestamped event to the span.
     *
     * @param logEvent event instance
     */
    void addLogEvent(LogEvent logEvent);

}
