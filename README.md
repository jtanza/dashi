# Dashi

Dashi is a small, embedded HTTP server running on the JVM. It is single-threaded, asynchronous and designed to be super simple to use.  It is not for production use (yet!) but lends itself quite nicely to rapid prototyping and usage in test environments.

Getting a server up and running is as simple as:

```
RequestHandler handler = new RequestHandler("/ping", r -> Response.ok("pong"));
HttpServer.builder(new RequestDispatcher().addHandler(handler)).build().serve();
```

### Installation

Dashi requires Java 8 or greater. It is packaged with Maven and is built as an uber-jar. Dependencies in Dashi are pretty minimal. After the following you're ready to go:

```
$ git clone https://github.com/jtanza/dashi.git && cd dashi
$ mvn clean install
```

### API

The Dashi API is built around the `RequestDispatcher`. It's design allows for users to quickly generate an HTTP server from a collection of self contained handlers without having to spend time on configurations. Below are a few examples of the features offered.  Please note that this project is still a work in progress, and as such the API may be subject to change. Any and all feedback is welcomed. Be sure to refer to the [wiki](https://github.com/jtanza/dashi/wiki) for the full project documentation.  

### Examples

Instantiating `RequestDispather`s is pretty straight forward and concise with chained method invocation:

```
String usersResource = "/your-path";
RequestDispatcher requestDispatcher = new RequestDispatcher()
	.addHandler(new RequestHandler(GET,    userResource, r -> Response.ok().build()))
	.addHandler(new RequestHandler(DELETE, userResource, r -> Response.from(NOT_CONTENT).build()));
```

Path variables are easily configurable:

```
RequestDispatcher dispatcher = new RequestDispatcher()
    .addHandler(new RequestHandler(Method.PUT, "/users/{userId}", r -> {
        String userId = r.getPathVariable("userId");
        // do work
        return Response.ok().build();
    }));
```

Serving static files is as simple as providing a path to resources:

```
HttpServer.builder(new RequestDispatcher().addResourcePath("/web")).build().serve();
```

Dashi delegates the servicing of client requests to a stand alone worker pool, by default a `cachedThreadPool` is used, but this resource is easily overridable to suit client needs:

```
HttpServer.builder(dispatcher).workerPool(Executors.newSingleThreadExecutor()).build().serve();
```

### Design
 
Dashi is built heavily around `java.nio` and strives to be as resource efficient as possible. The thread-per-socket-connection model is abandoned in favor of readiness selection to multiplex requests from all client connections within a single thread. The servicing of ready requests however is assigned to a separate pool of worker threads, decoupling network I/O from the processing of received data. 

