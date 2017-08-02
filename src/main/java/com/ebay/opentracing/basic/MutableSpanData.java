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
