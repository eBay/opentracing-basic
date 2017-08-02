package com.ebay.opentracing.basic;

import java.util.concurrent.TimeUnit;

final class SpanFinisher<T> {
    private final FinishedSpanReceiver<T> receiver;

    SpanFinisher(FinishedSpanReceiver<T> receiver) {
        this.receiver = receiver;
    }

    void finish(MutableSpanData<T> spanState) {
        finish(spanState, TimeUnit.MILLISECONDS, System.currentTimeMillis());
    }

    void finish(MutableSpanData<T> spanState, TimeUnit finishTimeUnit, long finishTime) {
        spanState.setFinishTime(finishTimeUnit, finishTime);
        receiver.spanFinished(spanState);
    }

}
