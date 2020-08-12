# Debugging

## Using a Custom Server

Sometimes you may need to debug with some special purpose server. To do that, add a file like this

 src/main/assets/customServers.json

with details of the custom server(s) you need, like this:

```
[
  {
    "name": "Test Server 1",
    "session_token": "12345678901234567",
    "local_address": "http://192.168.0.11:4563",
    "remote_address": "http://192.168.12.22:4563"
  },
  {
    "name": "Test Server 2",
    "session_token": "12345678901234567",
    "local_address": "http://192.168.0.11:4563",
    "remote_address": "http://192.168.12.22:4563"
  }
]
```


## Enabling Chuck Interceptor

Some relevant information about Chuck Interceptor can be found [here](https://github.com/jgilfelt/chuck).

We have disabled Chuck Interceptor by default in Amahi App. But if you want to use it for debugging, you can enable it by following these steps :

1. Enable the dependencies in `build.gradle` by uncommenting the following code : 

```
debugImplementation 'com.readystatesoftware.chuck:library:1.1.0'
releaseImplementation 'com.readystatesoftware.chuck:library-no-op:1.1.0'
betaReleaseImplementation 'com.readystatesoftware.chuck:library-no-op:1.1.0'
```

2. Now modify the `ApIModule.java` file to add the Chuck Interceptor :
  
  * Creating an instance for Chuck Interceptor, by uncommenting following block of code:
  ```
  @Provides
  @Singleton
  ChuckInterceptor provideChuckInterceptor(Context context) {
    return new ChuckInterceptor(context);
  }
  ```
  
  * Modify the function definition to pass Chuck Interceptor :
    ```
    provideHttpClient(ApiHeaders headers, HttpLoggingInterceptor logging, ChuckInterceptor chuck)
    ```

    * Add the Chuck interceptor when building OkHttpClient, using:
  ```
  clientBuilder.addInterceptor(chuck);
  ```
     
