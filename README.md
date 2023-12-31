# Nearest Car Parks

An API-only application that returns information on the nearest parking 
facilities to a user's location, including the parking lot's availability.

## Approach

Database:
- Used PostgreSQL with PostGIS to add support for geographic objects and 
   take advantage of its advanced query capabilities for spatial data. 
   Also, able to add spatial index to improve query performance since there 
   are around 2000 car park details stored in the database.

Loading of data:
- Car park information
  - At the start of the application, the `StartupRunner` class is designated to import all the 
    car park data from the CSV file into the database. This process is a one-time operation, 
    the data will not be reloaded upon subsequent application restarts unless the 
    `car_park` table is cleared.
  - The car park coordinates, originally in SVY21 format, are transformed to WGS84 format. 
    This conversion is essential as the data is stored as a geometry type in the database,
    utilizing an SRID (Spatial Reference ID) of 4326, which corresponds to the World Geodetic System.
  - Used https://epsg.io/transform to double-check transformation of coordinate values.
- Car park availability
  - When initializing the application, a scheduled thread pool is run every minute to update 
    the car park availability in the cache.
  - The external API provides a comprehensive response that encompasses the availability of all car parks, 
    with updates occurring every minute. To optimize efficiency and minimize redundant calls to the external API, 
    I have implemented caching for the processed response and also storing stale cache which will be used in case 
    of a cache miss.

Considerations:
- In terms of car park availability, I opted for a simplified approach by utilizing only the total number 
  of lots, rather than detailing the total and available lots for each lot type per car park number.

## Requirements
- Java JDK 17
- JAVA_HOME contains the location of the JDK home dir
- Docker

## Getting Started

To run the app, follow these steps:

1. Clone repository to your local machine.
    ```
   git clone https://github.com/tjlerias/wego-coding-exercise.git
    ```
2. Open the project using your preferred IDE.
3. Rename the `.env.example` file to `.env` and replace the value for `DB_USERNAME` and `DB_PASSWORD`.
4. In `application.properties` replace `<DB_USERNAME>` and `<DB_PASSWORD>` values to be the same with the `.env` file.
5. Open terminal to the project's root directory and run `docker-compose up` and wait for the process to complete. 
6. In a separate terminal window, run `./mvnw clean install` and wait for it to finish and run `./mvnw spring-boot:run` to start the application.
7. App should now be running on `http://localhost:8080`

## Endpoint

- `GET /v1/carparks/nearest` - retrieve the nearest car parks with parking lot's availability information.

    | Field     | Description                     | Type    |
    |-----------|---------------------------------|---------|
    | latitude  | Latitude of a given location    | double  |
    | longitude | Longitude of a given location   | double  |
    | distance  | Optional. Default is 500 meters | integer |
    | page      | Page number                     | integer |
    | per_page  | Page size                       | integer |
    
    Sample cURL:
    ```
    curl --location 'http://localhost:8080/v1/carparks/nearest?latitude=1.37326&longitude=103.897&distance=500&page=1&per_page=3'
    ```