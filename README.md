# tech-case-studies

## Overview

A collection of containerized microservices used for technical interviews. Candidates pull the images (or build from source), run them locally, and build client applications against their APIs. All data is synthetic.

## Services

| Service              | Port | Description                                                                                                       |
| -------------------- | ---- | ----------------------------------------------------------------------------------------------------------------- |
| retail-data-services | 8080 | Read-only REST API serving synthetic product catalog, pricing, and inventory availability                         |
| cart-service         | 8081 | Shopping cart REST API with CRUD operations. Calls retail-data-services at runtime for item and price enrichment. |

**Note:** All data returned by these services is mocked/sample data intended for interviewing purposes only. It does not represent real or production retail data.

## Running the application

### Option 1: docker compose (recommended)

Build and run both services together:

```sh
./gradlew clean build
docker compose build
docker compose up
```

The services will be available at:

- retail-data-services: <http://localhost:8080/retail_data_services/v1/>
- cart-service: <http://localhost:8081/cart/v1/>

To stop the services:

```sh
docker compose down
```

### Option 2: docker run (individual services)

Build and run each service separately. Note that cart-service depends on retail-data-services, so retail-data-services must be running first.

```sh
# Build both JARs
./gradlew clean build

# Build and run retail-data-services
docker build -t retail-data-services retail-data-services/
docker run -d -p 8080:8080 --name data retail-data-services

# Build and run cart-service
docker build -t cart-service cart-service/
docker run -p 8081:8081 --name cart --link data:data cart-service
```

### OpenAPI specs

Both services expose Swagger UI and OpenAPI docs:

| Service              | Swagger UI                                                            | API docs                                                 |
| -------------------- | --------------------------------------------------------------------- | -------------------------------------------------------- |
| retail-data-services | <http://localhost:8080/retail_data_services/v1/swagger-ui/index.html> | <http://localhost:8080/retail_data_services/v1/api-docs> |
| cart-service         | <http://localhost:8081/swagger-ui/index.html>                         | <http://localhost:8081/api-docs>                         |

HTTP request files for use with IntelliJ or VS Code are available at:

- `retail-data-services/retail-data-services.http`
- `cart-service/cart-service.http`

## retail-data-services endpoints

### Get price

**`GET /retail_data_services/v1/prices/{id}`**

```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/prices/123456"
```

### Get item

**`GET /retail_data_services/v1/items/{id}`**

```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/items/123456"
```

### List items

**`GET /retail_data_services/v1/items`**

Supports filtering by `small_description` query parameter.

```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/items"
curl -X GET "http://localhost:8080/retail_data_services/v1/items?small_description=jersey"
```

### Get availability

**`GET /retail_data_services/v1/availability/{id}`**

```sh
curl -X GET "http://localhost:8080/retail_data_services/v1/availability/123456"
```

## cart-service endpoints

cart-service depends on retail-data-services at runtime. When a cart is read, the service calls retail-data-services over HTTP to enrich each line item with product details and pricing. It then calculates taxes (by product category) and delivery charges.

### Get cart

**`GET /cart/v1/carts/{id}`**

```sh
curl 'http://localhost:8081/cart/v1/carts/100' -i -X GET
```

### Create cart

**`POST /cart/v1/carts`**

Request body: array of objects with `tcin` (string) and `quantity` (integer).

```sh
curl 'http://localhost:8081/cart/v1/carts' -i -X POST \
  -H 'Content-Type: application/json' \
  -d '[
    {"tcin" : "123456", "quantity": 1},
    {"tcin" : "789123", "quantity": 2}
  ]'
```

### Add item to cart

**`POST /cart/v1/carts/{id}/items`**

```sh
curl 'http://localhost:8081/cart/v1/carts/100/items' -i -X POST \
  -H 'Content-Type: application/json' \
  -d '{"tcin" : "456788", "quantity": 2}'
```

### Update item quantity

**`PATCH /cart/v1/carts/{id}/items/{tcin}`**

```sh
curl 'http://localhost:8081/cart/v1/carts/100/items/456788' -i -X PATCH \
  -H 'Content-Type: application/json' \
  -d '{"quantity": 3}'
```

### Remove item from cart

**`DELETE /cart/v1/carts/{id}/items/{tcin}`**

Removing the last item from a cart also removes the cart.

```sh
curl 'http://localhost:8081/cart/v1/carts/100/items/456788' -i -X DELETE
```

## Customizing data

You can customize the data returned by retail-data-services by creating your own CSV files and mounting them into the container. See [retail-data-services/data-formats.md](retail-data-services/data-formats.md) for details.

## Induced behaviors (latency and failure simulation)

Both services support configurable induced behaviors that simulate latency and failures. By setting the `DEFAULT_BEHAVIOR` environment variable, you can run the same APIs in different modes (normal, slow, or randomly failing) without changing any code.

See [retail-data-services/induced_behaviors.md](retail-data-services/induced_behaviors.md) for available modes, environment variables, and usage examples.

## Performance benchmarking

A startup time benchmarking script is available at `retail-data-services/scripts/benchmark-startup.sh`. See `retail-data-services/scripts/README.md` for usage details.

## Project structure

```txt
tech-case-studies/
  retail-data-services/       # Read-only data API (port 8080)
    src/
    Dockerfile
    build.gradle.kts
  cart-service/                # Shopping cart API (port 8081)
    src/
    Dockerfile
    build.gradle.kts
  docker-compose.yml           # Orchestrates both services
  build.gradle.kts             # Root Gradle build (shared config)
  settings.gradle.kts          # Multi-project includes
  gradle/libs.versions.toml    # Shared dependency versions
```
