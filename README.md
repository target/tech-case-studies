# retail_data_services

## Overview
The `retail_data_service` is a simple RESTful service providing endpoints to access a variety of data entities typical in a retail business. These data entities include item, price and availability.

**Note:** All data returned by this service, including any examples shown below, is **mocked/sample data** intended for interviewing purposes only. It does not represent real or production retail data.

## Endpoints

Note: There is a retail-data-services.http file that can be used as well.

### Open API Specification
**Swagger UI:** http://localhost:8080/retail_data_services/v1/swagger-ui/index.html

**API Docs:** http://localhost:8080/retail_data_services/v1/api-docs

The committed yaml version can be re-generated using the following command:
```sh
curl http://localhost:8080/retail_data_services/v1/api-docs.yaml > api-spec/retail_data_services-v1.yaml
```
---
### Get Price
Retrieve the price of a specific product by its ID.

**URI:** `/retail_data_services/v1/prices/{id}`

**Method:** `GET`

**Path Parameters:**
- `id` (string): The ID of the product.

**Response:**
- `200 OK`: Returns the price details of the product.
- `404 Not Found`: If the price is not found.

**Sample cURL Command:**
```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/prices/123456"
```
---
### Get Item
**URI:** `/retail_data_services/v1/items/{id}`

**Method:** `GET`

**Path Parameters:**
- `id` (string): The ID of the item.

**Response:**
- `200 OK`: Returns the item details.
- `404 Not Found`: If the item is not found.

**Sample cURL Command:**
```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/items/123456"
```
---
### List Items
**URI:** `/retail_data_services/v1/items`

**Method:** `GET`

**Response:**
- `200 OK`: Returns the item details.

**Sample cURL Command:**
```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/items"
```
---
### Filter Item List by Small Description
**URI:** `/retail_data_services/v1/items`

**Method:** `GET`

**Query Parameters:**
- `small_description` (string): The small description to filter items by.

**Response:**
- `200 OK`: Returns the filtered item details.

**Sample cURL Command:**
```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/items?small_description=jersey"
```
---
### Get Availability
**URI:** `/retail_data_services/v1/availability/{id}`

**Method:** `GET`

**Path Parameters:**
- `id` (string): The ID of the item.

**Response:**
- `200 OK`: Returns the units of the item available for ordering .
- `404 Not Found`: If the item availability is not found.

**Sample cURL Command:**
```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/availability/123456"
```
---

### Performance Benchmarking

A startup time benchmarking script is available in `scripts/benchmark-startup.sh`. 
This script measures Docker container startup time and generates performance reports.

See `scripts/README.md` for usage details.

---

### Running the Application

#### Option 1: Using docker run (Recommended)

Build and run the Docker container directly:

```sh
# 1. Build the application JAR
./gradlew clean build

# 2. Build the Docker image
docker build -t retail-data-services .

# 3. Run the container
docker run -p 8080:8080 retail-data-services
```

The application will be available at http://localhost:8080/retail_data_services/v1/

To run in the background (detached mode):

```sh
docker run -d -p 8080:8080 --name retail-data-services retail-data-services
```

To stop the container:

```sh
docker stop retail-data-services
docker rm retail-data-services
```

#### Option 2: Using docker-compose

Alternatively, use docker-compose:

```sh
./gradlew buildAndRunDockerCompose
```
### Customizing the data
You can customize the data returned from teh services by creating your own CSV files for the various services.
See the documentation at [data-formats.md](data-formats.md) for details.

### Induced Behaviors (Latency and Failure Simulation)

This service can run with configurable **induced behaviors** that wrap the logic used to generate API responses. By setting environment variables when you start the container, you can run the same APIs in different modes (normal, slow, or randomly failing) without changing any code. This is intended to help candidates test how their applications handle latency and failures.

See `induced_behaviors.md` for:

- An overview of the behavior mechanism and behavior selection (`DEFAULT_BEHAVIOR`).
- The available modes: `NORMAL`, `SLOW_RESPONSE`, and `RANDOM_FAILURES`.
- Environment variables that control behavior tuning.
- `docker run` and `docker-compose` examples for starting the service in each mode.


