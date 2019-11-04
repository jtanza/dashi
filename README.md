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

The Dashi API is built around the `RequestDispatcher`. It's design allows for users to quickly generate an HTTP server from a collection of self contained handlers without having to spend time on configurations. Below are a few examples of the features offered.  

Please note that this project is still **a work in progress**, and as such the API may be subject to change. Any and all feedback is welcomed! Be sure to refer to the [wiki](https://github.com/jtanza/dashi/wiki) for the full project documentation.  

### Examples

Instantiating `RequestDispather`s is pretty straight forward and concise with chained method invocation:

```
String resourcePath = "/your-path";
RequestDispatcher requestDispatcher = new RequestDispatcher()
	.addHandler(new RequestHandler(GET,    resourcePath, r -> Response.ok().build()))
	.addHandler(new RequestHandler(DELETE, resourcePath, r -> Response.from(GONE).build()));
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

### Benchmarking 

A few `ab` tests have been run against Dashi with the output provided below. All tests were run on a ~2015 3.1 GHz Intel Core i7 MacBook Pro. Benchmarking on memory footprint for Dashi will be soon to follow.


4000 requets over 4000 concurrent connections

```
jtanza @ ~/dev/dashi (master) $ ab -n 4000 -c 4000 localhost/ping
[...]
Server Software:        Dashi/0.0.1
Server Hostname:        localhost
Server Port:            80

Document Path:          /ping
Document Length:        5 bytes

Concurrency Level:      4000
Time taken for tests:   0.541 seconds
Complete requests:      4000
Failed requests:        0
Total transferred:      404000 bytes
HTML transferred:       20000 bytes
Requests per second:    7393.74 [#/sec] (mean)
Time per request:       540.998 [ms] (mean)
Time per request:       0.135 [ms] (mean, across all concurrent requests)
Transfer rate:          729.27 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0  149  38.2    146     223
Processing:   133  218  18.1    224     240
Waiting:        0  185  45.4    204     234
Total:        222  367  24.0    367     426

Percentage of the requests served within a certain time (ms)
  50%    367
  66%    375
  75%    382
  80%    387
  90%    400
  95%    413
  98%    419
  99%    421
 100%    426 (longest request)

```

15000 requests over 50 concurrent connections

```
jtanza @ ~/dev/dashi (master) $ ab  -n 15000 -c 50 localhost/ping
[...]
Server Software:        Dashi/0.0.1
Server Hostname:        localhost
Server Port:            80

Document Path:          /ping
Document Length:        5 bytes

Concurrency Level:      50
Time taken for tests:   1.561 seconds
Complete requests:      15000
Failed requests:        0
Total transferred:      1515000 bytes
HTML transferred:       75000 bytes
Requests per second:    9608.28 [#/sec] (mean)
Time per request:       5.204 [ms] (mean)
Time per request:       0.104 [ms] (mean, across all concurrent requests)
Transfer rate:          947.69 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   1.2      1      12
Processing:     0    4   2.5      3      24
Waiting:        0    3   2.5      3      24
Total:          2    5   2.5      4      24

Percentage of the requests served within a certain time (ms)
  50%      4
  66%      5
  75%      5
  80%      6
  90%      7
  95%     10
  98%     14
  99%     17
 100%     24 (longest request)

```
