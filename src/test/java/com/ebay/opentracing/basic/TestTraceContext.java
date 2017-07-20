package com.ebay.opentracing.basic;

class TestTraceContext
{

	private final String traceId;

	private final String spanId;

	TestTraceContext(String traceId, String spanId)
	{
		this.traceId = traceId;
		this.spanId = spanId;
	}

	String getTraceId()
	{
		return traceId;
	}

	public String getSpanId()
	{
		return spanId;
	}

	@Override
	public String toString()
	{
		return getSpanId();
	}

}
