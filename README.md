## Logbook Bug Process

This project can be used to demonstrate a bug with Logbook when used with Micronaut via the netty module.

1. Start the application via ./gradlew run
2. Open a browser to http://localhost:8080/dummy
3. Look at the output of the logs, you should see content from logbook
4. Reload the browser
5. Most likely you will not see any more logging from logbook

It is possible that if you keep trying you will see additional logbook output. This is because
the request is being handled by a different thread. Each thread gets an instance of a LogbookServerHandler
and once that handler has processed a request/response, you will no longer see output from 
that handler. 

My impression is that bug is with the way Sequence is implemented. When a LogbookServerHandler is
created, a Sequence object is created with room for two tasks. The tasks are used to display the
request and response. The code in Sequence uses a variable called 'next' which controls whether a
task is executed.

```java
    synchronized void set(final int index, final Runnable task) {
        tasks.set(index, task);

        if (index == next) {
            runEagerly();
        }
    }
```
If a call is made to set with an index value ~= next, the task will not run. The 'runEagerly' method in
Sequence looks like this:

```java
    private void runEagerly() {
        final int end = tasks.size();

        for (@Nullable final Runnable task : tasks.subList(next, end)) {
            if (task == null) {
                return;
            }

            task.run();
            tasks.set(next, null);
            next++;
        }
    }
```

When a request and response are processed, both tasks will be processed. After the response 
task is completed, the value of 'next' will be 2. The value will remain at 2. When additional 
requests are made on the same thread, the value of 'next' is always 2 so the 'runEagerly' method 
is never called again.

I don't necessarily understand all the nuances involved, but I think the following change in
the 'runEagerly' method would solve the problem:

```java
    private void runEagerly() {
        final int end = tasks.size();

        for (@Nullable final Runnable task : tasks.subList(next, end)) {
            if (task == null) {
                return;
            }

            task.run();
            tasks.set(next, null);
            next++;
        }

        // Reset next to 0, we've exhausted the tasks and this instance will live on in the thread
        // that it was created in, so we need to reset next back to 0 if we want to see any more output

        next = 0;
    }
```

If my assumption is correct, the code will never normally exit the for loop without the value 
of 'next' being 2. In all other cases, the loop will short circuit and return when a null task
is encountered.

## Micronaut 3.1.4 Documentation

- [User Guide](https://docs.micronaut.io/3.1.4/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.1.4/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.1.4/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

