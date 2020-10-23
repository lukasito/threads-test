# Threads test

This is simple, pure java application without any dependency management.
Open it as java project in your favourite IDE or simply compile using `javac` and run it with `java`.

There are 3 main components:

- HttpServer
- HttpClient
- `compute()` method

### HttpServer
Is simple java server from `com.sun.net` package running on `serverExecutor`

### HttpClient
Is java client from `java.net` package running on `clientExecutor`

### compute()
This method has 2 parts
- computation (very inefficient - probably not ideal) of euler number
  it takes about 1 second (on quad core) to compute euler with 1500 iterations
  formula is: `0 + 1/1! + 1/2! + 1/3! + ... + 1/n!` where `n` = number of iterations.
  The point here is to keep processor busy.
  
- io (calling http request) 
  locally I run `wiremock` with `fixedDelay` to ~500ms. For simplicity you can also simulate IO with `Thread.sleep(500)`  
  ideally download wiremock here: https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/2.27.2/wiremock-jre8-standalone-2.27.2.jar  
  copy `resources/mappings` and `resources/__files` folders next to wiremock jar and
  start wiremock with `java -jar wiremock-jre8-standalone-2.27.2.jar --port 9090`
  
