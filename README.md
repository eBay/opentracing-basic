# Basic OpenTracing API Implementation for Java

## Introduction

 The [OpenTracing API for Java](https://github.com/opentracing/opentracing-java)
 is defines the interfaces for how a user interacts with a compliant
 OpenTracing back-end but leaves it up to the OpenTracing backend
 implementation to fulfill the API requirements. When creating simple
 bridge implementations, tracing data ETL operations, or integrating
 OpenTracing support in constrained environments this can amount to
 re-inventing the wheel.

 The goal of this library is to attempt to implement the generic
 semantics of the OpenTracing API, allowing for the main implementation
 points to be provided into the library by the consumer.

## Status

 This project is currently in a pre-release state.  The API should be
 considered to be unstable and the implementation is not yet optimized.

 No artifacts are currently being published to any repositories.

## Obtaining the Library

 For the time being, obtain the artifacts by cloning this repository
 and then running the following command:

```
$ ./gradlew clean publishToMavenLocal

```

 This will perform the build and install the artifacts in your local
 maven repository.  Once installed you can access the artifacts as
 follows:

### Gradle:

```
dependencies {
    compile 'com.ebay.opentracing:opentracing-basic:0.0.0'
}
```

### Maven:

```
<dependencies>
    <dependency>
        <groupId>com.ebay.opentracing</groupId>
        <artifactId>opentracing-basic</artifactId>
        <version>0.0.0</version>
    </dependency>
</dependencies>
```

## Usage

 At a high level, the use of this library requires the developer to
 do the following:

1. Define a "trace context" object which encapsulates the data which
  is specific to the tracing implementation back-end (e.g., Trace ID,
  Span ID, sampling information, etc.).  This "trace context" object
  may be a plain old Java object (POJO).  An example implementation
  is provided in the test code:
  [TestTraceContext](src/test/java/com/ebay/opentracing/basic/TestTraceContext.java)
1. Create an implementation of the
  [TraceContextHandler](src/main/java/com/ebay/opentracing/basic/TraceContextHandler.java)
  interface which creates instances of
  [InternalSpanContext<?>](src/main/java/com/ebay/opentracing/basic/InternalSpanContext.java)
  which integrate the custom trace context data.  An example
  implementation is provided in the test code:
  [TestTraceContextHandler](src/test/java/com/ebay/opentracing/basic/TestTraceContextHandler.java)
1. Create an implementation of the
  [FinishedSpanReceiver](src/main/java/com/ebay/opentracing/basic/FinishedSpanReceiver.java)
  interface to take finished spans' data and use the data as appropriate
  for the consumer's use case.  An example implementation is provided
  in the test code:

 Once the above have been setup, usage of the library may be as simple
 as the following:

```
TraceContextHandler traceContextHandler = ...
FinishedSpanReceiver receiver = ...
Tracer tracer = new BasicTracerBuilder<>(traceContextHandler, receiver)
    .build();

try (ActiveSpan span = tracer.buildSpan("operation").startActive()) {
    // Measured work
}
```

 To achieve cross-process operation, the developer will also need to
 create an implementation of the
 [Formatter](https://github.corp.ebay.com/mcumings/opentracing-basic/blob/master/src/main/java/com/ebay/opentracing/basic/Formatter.java)
 interface in order to define how the trace/span context is applied to
 a data carrier of a particular type.  Once defined, the formatter
 should be registered at Tracer- creation time as in the following
 example, replacing the Format with the appropriate type.

```
Tracer tracer = new BasicTracerBuilder<>(traceContextHandler, receiver)
    .registerFormatter(Format.Builtin.TEXT_MAP, formatter)
    .build();
```

The Basic Tracer implementation will then leverage the provided
formatter for inject and extract operations.

## References

* [OpenTracing - A vendor-neutral open standard for distributed tracing](http://opentracing.io/)
* [OpenTracing Specification](https://github.com/opentracing/specification)
* [OpenTracing Semantic Conventions](https://github.com/opentracing/specification/blob/master/semantic_conventions.md)
