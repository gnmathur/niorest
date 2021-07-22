# :herb: NIOREST

A simple framework for REST and HTTP API testing built on Java NIO. NIOREST is for those environments where we would like
to test tens of thousands of APIs periodically using minimal resources. The core NIO reactor will be a single thread that
will be used to dispatch all the registered HTTP and REST APIs. NIO is suitable for this task because it can support 
a very high order of concurrent long-lived connections, where each of these connection have infrequent data transfers 
(tests are only required to run every time period).

# Feature List
- [x] Basic framework
- [ ] HTTP headers including to headers to support token auth 
- [ ] HTTP POST request
- [ ] HTTP response parsing
- [ ] Rich config DSL to specify a sequence of REST API test suites, where tests within a suite can extract
    information from the previous test in the suite. For example, CREATE REST API call, followed by
    a READ REST API call to verify that the create succeded
- [ ] Add framework for testing

# Build and Run
```
$ mvn compile && mvn exec:java -Dexec.mainClass="com.gmathur.niorest.NioRest"
```

__Last Update__: 07/21/2021
