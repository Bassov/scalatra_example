# simple Scalatra Web App #

## Build & Run ##

```sh
$ cd simpleScalatraWebApp
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

Also here is export from Postman

This repo demonstrate the approach how to use `flatMap` for errors handling, flatMap
is a straight way to for-comprehensions. Also it is shown how
how you can introduce modularity for scalatra-based server.

There is a package `old_server` where first version is implemented, it was partially refactored. The refactored version
is not fully implemented, but it is enough to extend it further. 

There is also slides.
