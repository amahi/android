# Debugging

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
