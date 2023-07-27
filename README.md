# ECE 531 Introducion to IOT
## Smart Thermostat REST API


### About
This API is an HTTP API deployed to an AWS EC2 instance. The API uses the HttpServer with an SQLite JDBC integration and Maven build. 

### API Specification
The API has two paths, `/userTempProg` and `/deviceTempProg`, usage of `/userTempProg` will be covered 
and `/deviceTempProg` excluded as its for internal use only. 
- `/userTempProg/{deviceId}`
  - This path is used to create a device programming record containing the provided `deviceId`, `temperature`, and `datetime`. 
  - POST `/userTempProg/{deviceId}`
  - Post body: 
    ```
      {
        "temp": "<String parsed as float in degrees celsius>" 
        "datetime": "<ISO8601 timestamp>"
      }
      ```
    Example: 
    ```
    {
       "temp": "22.1", 
       "datetime": "2023-07-25T23:10:09.920382Z"
    }
    ```
- POST `/userTempProg/{deviceId}`
    - This path is used to create a device programming record containing the provided `deviceId`, `temperature`, and `datetime` with a unique ID on the server.
    - Returns: `recordId` (integer) to allow fetching and deleting of the created record
      - return example: 
        ```
        {"recordId":19}
        ```
    - Post body:
      ```
        {
          "temp": "<String parsed as float in degrees celsius>" 
          "datetime": "<ISO8601 timestamp>"
        }
        ```
      Example:
      ```
      {
         "temp": "22.1", 
         "datetime": "2023-07-25T23:10:09.920382Z"
      }
      ```
- GET `/userTempProg/{recordId}`
    - This path is used to fetch a temperature programming record with the given `recordId`.
    - Any request body provided will not be used
    - Returns: `temperatureProgrammingRecord` or an error message with 400/404 error code
        - `temperatureProgrammingRecord` example:
          ```
          {
             "deviceId": 1000,
             "temp": "22.1",
             "datetime": "2023-07-25T23:10:09.920382Z"
          }
          ```
        - error example 
            ```
            404 - Not Found
            {"message":"Unable to locate record"}
            ```
- DELETE `/userTempProg/{recordId}`
  - This path is the same as the GET path and will not use a request body
  - 404 or 400 errors and an error message are returned if deletion was unsuccessful as in the GET description. 


