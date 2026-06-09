# API Consistency & Rename Plan

## Context

Two services in this repo:

- `retail-data-services` (port 8080) — read-only product catalog serving items, prices, and availability from CSV-backed data. Consumed by `cart-service` at runtime.
- `cart-service` (port 8081) — shopping cart service. Calls `retail-data-services` for item/price enrichment on every cart read.

Both are mock APIs for retail case studies, run locally via Docker. No gateway, no production infrastructure.

---

## Decisions

### 1. Rename `retail-data-services` → `product-api`

The service is a product catalog — items, prices, availability. "Retail data services" communicates nothing about the domain. Everything is in scope: directory, packages, Docker service name, paths, docs.

| Thing                         | Before                                           | After                                                             |
| ----------------------------- | ------------------------------------------------ | ----------------------------------------------------------------- |
| Submodule directory           | `retail-data-services/`                          | `product-api/`                                                    |
| Gradle subproject             | `retail-data-services`                           | `product-api`                                                     |
| Java package root             | `com.target.retail.data.services`                | `com.target.retail.product`                                       |
| Java source tree              | `src/main/java/com/target/retail/data/services/` | `src/main/java/com/target/retail/product/`                        |
| JAR artifact                  | `retail-data-services.jar`                       | `product-api.jar`                                                 |
| Docker service name           | `data`                                           | `product-api`                                                     |
| Docker hostname (cart client) | `http://data:8080`                               | `http://product-api:8080`                                         |
| OpenAPI title                 | `"Retail Data Services API"`                     | `"Product API"`                                                   |
| OpenAPI description           | `"API for managing retail data services."`       | `"Read-only API for product catalog, pricing, and availability."` |
| API spec filename             | `retail_data_services-v1.yaml`                   | `product-api-v1.yaml`                                             |
| HTTP requests file            | `retail-data-services.http`                      | `product-api.http`                                                |

### 2. API path strategy

REST best practice: paths identify resources, not services. Service names belong in DNS/hostnames, not URL paths. Version belongs in the path, declared in `@RequestMapping` on the controller (visible in code and Swagger — not hidden in config).

| Before                                       | After                         |
| -------------------------------------------- | ----------------------------- |
| `/retail_data_services/v1/items/{id}`        | `/v1/items/{id}`              |
| `/retail_data_services/v1/items`             | `/v1/items`                   |
| `/retail_data_services/v1/prices/{id}`       | `/v1/prices/{id}`             |
| `/retail_data_services/v1/availability/{id}` | `/v1/availability/{id}`       |
| `/cart/v1/carts/{id}`                        | `/v1/carts/{id}`              |
| `/cart/v1/carts`                             | `/v1/carts`                   |
| `/cart/v1/carts/{id}/items`                  | `/v1/carts/{id}/items`        |
| `/cart/v1/carts/{id}/items/{tcin}`           | `/v1/carts/{id}/items/{tcin}` |

`context-path` removed from `product-api` `application.yml`. `@RequestMapping("/v1")` added to all controllers in both services.

### 3. Response field name alignment

Cart is the consumer-facing API. Upstream (`product-api`) adopts cart's shorter names where they differ. Cart's internal rename (when re-serializing consumed data) is removed.

#### Image fields

| Layer                                                   | Before                             | After                  |
| ------------------------------------------------------- | ---------------------------------- | ---------------------- |
| `product-api` `ItemResponse.ImageData`                  | `primary_image`, `alternate_image` | `primary`, `alternate` |
| `product-api` `Item` model (flat fields)                | `primaryImage`, `alternateImage`   | `primary`, `alternate` |
| `cart-service` `ItemApiResponse.ImageData` (client DTO) | `primaryImage`, `alternateImage`   | `primary`, `alternate` |
| `cart-service` `CartResponse.ImageResponse`             | `primary`, `alternate`             | no change              |

#### Price fields

| Layer                                          | Before                        | After             |
| ---------------------------------------------- | ----------------------------- | ----------------- |
| `product-api` `PriceResponse`                  | `regular_price`, `sale_price` | `regular`, `sale` |
| `cart-service` `PriceApiResponse` (client DTO) | `regularPrice`, `salePrice`   | `regular`, `sale` |
| `cart-service` `Price` model                   | `regularPrice`, `salePrice`   | `regular`, `sale` |
| `cart-service` `CartResponse.PriceResponse`    | `regular`, `sale`             | no change         |

### 4. ID field standardization — `product_id`, `tcin` → `item_id` everywhere

`tcin` (Target Corporation Item Number) and `item_id` are the same concept — the unique product identifier — but named differently across both services with no clear boundary rule. Standardize to `item_id` everywhere.

Three distinct problems rolled into one decision:

- `PriceResponse` and `AvailabilityResponse` in `product-api` expose `product_id` while `ItemResponse` exposes `item_id` — inconsistent within the same service
- `cart-service` uses `tcin` throughout — models, DTOs, path variables, CSV headers, service methods — while consuming `item_id` from `product-api`
- `cart-service` HTTP clients pass a `tcin` parameter into a `{item_id}` URI slot — the naming mismatch is explicit at the cross-service boundary

#### `product-api` changes

| File | Before | After |
| ---- | ------ | ----- |
| `PriceResponse` | `productId` / `product_id` | `itemId` / `item_id` |
| `AvailabilityResponse` | `productId` / `product_id` | `itemId` / `item_id` |
| `PriceIntegrationTest` (line 14) | `jsonPath("$.product_id")` | `jsonPath("$.item_id")` |
| `AvailabilityIntegrationTest` (line 14) | `jsonPath("$.product_id")` | `jsonPath("$.item_id")` |
| API spec `retail_data_services-v1.yaml` | `product_id` in PriceResponse + AvailabilityResponse schemas | `item_id` |

#### `cart-service` changes

**Models:**

| File | Before | After |
| ---- | ------ | ----- |
| `model/Item.java` | `String tcin` | `String itemId` |
| `model/Price.java` | `String tcin` | `String itemId` |
| `model/StoredCartLine.java` | `String tcin`, `@JsonPropertyOrder` literal `"tcin"` | `String itemId`, `"itemId"` |
| `model/Cart.java` | `findByTcin(String tcin)` | `findByItemId(String itemId)` |

**Controller + DTOs:**

| File | Before | After |
| ---- | ------ | ----- |
| `CartController.java` | `@PathVariable String tcin`, `{tcin}` in `@DeleteMapping`/`@PatchMapping` | `@PathVariable String itemId`, `{itemId}` |
| `CartController.java` line 42 | `"duplicate TCINs"` in `@ApiResponse` description | `"duplicate item IDs"` |
| `AddItemRequest.java` | `String tcin` | `String itemId` |
| `CartResponse.ItemResponse` | `String tcin` | `String itemId` |

**Service + clients:**

| File | Before | After |
| ---- | ------ | ----- |
| `CartService.java` — `removeItem`, `addItem`, `updateCartItem`, `getPriceForItem`, `getItem` params | `String tcin` | `String itemId` |
| `CartService.java` — exception message (line 141) | `"No cart line found for tcin " + tcin` | `"No cart line found for item id " + itemId` |
| `CartService.java` — local vars `storedCartLineForTcin` | `storedCartLineForTcin` | `storedCartLineForItemId` |
| `ItemApiClient.java` | `getItem(String tcin)`, `.uri("/items/{item_id}", tcin)` | `getItem(String itemId)`, `.uri("/items/{itemId}", itemId)` |
| `PriceApiClient.java` | `getPricing(String tcin)`, `.uri("/prices/{item_id}", tcin)` | `getPricing(String itemId)`, `.uri("/prices/{itemId}", itemId)` |
| `PriceApiResponse.java` | `productId` | `itemId` |

**CSV data files:**

| File | Before | After |
| ---- | ------ | ----- |
| `src/main/resources/data/100.csv` header | `lineId,cartId,tcin,...` | `lineId,cartId,itemId,...` |
| `src/main/resources/data/101.csv` header | `lineId,cartId,tcin,...` | `lineId,cartId,itemId,...` |

**Tests:**

| File | Change |
| ---- | ------ |
| `CartControllerTest.java` | All `tcin` local vars → `itemId`; test method `testUpdateCartItemWhenTcinNotFoundInCart` → `testUpdateCartItemWhenItemIdNotFoundInCart`; comment `// Duplicate tcin` → `// Duplicate item ID` |
| `CartResponseTest.java` | `cartLineItem.item().tcin()` → `cartLineItem.item().itemId()` |
| `CartServiceTest.java` | All `tcin`/`tcin1`/`tcin2`/`tcinToRemove`/`tcinToKeep` vars → `itemId`/`itemId1`/`itemId2`/`itemIdToRemove`/`itemIdToKeep`; test method names updated; exception message assertion updated |
| `CartDatabaseTest.java` | Inline CSV string literal header `tcin` → `itemId` |

**Docs / HTTP files:**

| File | Change |
| ---- | ------ |
| `README.md` lines 123, 129, 130, 141, 146, 156 | `tcin` → `item_id` in prose, request body examples, curl commands, path examples |
| `cart-service/cart-service.http` lines 14, 15, 25 | `"tcin"` key in JSON bodies → `"item_id"` |

### 5. `merch_class` type

`product-api` correctly exposes `merch_class` as `Integer`. `cart-service` casts it to `String` unnecessarily. Fix cart-service to use `Integer` end-to-end.

| File                                       | Before                                    | After                          |
| ------------------------------------------ | ----------------------------------------- | ------------------------------ |
| `cart-service` `Item` model                | `String merchClass`                       | `Integer merchClass`           |
| `cart-service` `CartResponse.ItemResponse` | `String merchClass`                       | `Integer merchClass`           |
| `cart-service` `CartService.getItem()`     | `itemApiResponse.merchClass().toString()` | `itemApiResponse.merchClass()` |

### 6. `next_page` sentinel

`PaginatedResponse.calculateNextPage` returns `0` when there is no next page. `0` is also a valid page number, making "no more pages" ambiguous. Return `null` instead.

| File                                                | Before                                     | After                                 |
| --------------------------------------------------- | ------------------------------------------ | ------------------------------------- |
| `product-api` `PaginatedResponse`                   | `return ... : 0`                           | `return ... : null`                   |
| `product-api` `ItemControllerTest` (lines 131, 227) | `assertEquals(0, responseBody.nextPage())` | `assertNull(responseBody.nextPage())` |

### 7. Typed exceptions and consistent error handling

Replace bare `RuntimeException` throws with typed domain exceptions. Each service gets its own `GlobalExceptionHandler` (`@ControllerAdvice`) and `ErrorResponse` record — not shared, but identical shape.

#### Error response shape (both services)

```json
{ "status": 404, "message": "No cart found with id 123" }
```

Record: `public record ErrorResponse(int status, String message) {}`

#### New exception types

**`product-api`:**

- `ItemNotFoundException extends RuntimeException` — thrown by `ItemService` when item not found
- `PriceNotFoundException extends RuntimeException` — thrown by `PriceService` when price not found
- `AvailabilityNotFoundException extends RuntimeException` — thrown by `AvailabilityService` when availability not found
- `InducedFailureException extends RuntimeException` — thrown by `Behaviors.callWithRandomFailures`

**`cart-service`:**

- `CartNotFoundException extends RuntimeException` — thrown by `CartService` when cart not found
- `CartLineItemNotFoundException extends RuntimeException` — thrown by `CartService` when line item not found
- `InducedFailureException extends RuntimeException` — thrown by `Behaviors.callWithRandomFailures`

#### Controller refactor

Resource controllers in `product-api` (`ItemController`, `PriceController`, `AvailabilityController`) currently handle 404 via `Optional.empty()` → `ResponseEntity.notFound().build()`. Refactor to throw typed exceptions from the service layer; let the advice handle the 404 response uniformly.

`CartController` pre-check guard pattern (`cartService.getCart(id).isEmpty()` before service calls) removed. Typed exceptions from `CartService` propagate to the advice. Exception: `removeItemFromCart` retains its post-removal `getCart` call for the 204 vs 200 logic.

#### `GlobalExceptionHandler` mappings

| Exception                                                                                          | HTTP Status | `message`                           |
| -------------------------------------------------------------------------------------------------- | ----------- | ----------------------------------- |
| `ItemNotFoundException` / `PriceNotFoundException` / `AvailabilityNotFoundException` (product-api) | 404         | exception message                   |
| `CartNotFoundException` / `CartLineItemNotFoundException` (cart-service)                           | 404         | exception message                   |
| `InducedFailureException` (both)                                                                   | 503         | exception message                   |
| `DataException` (both)                                                                             | 500         | `"An internal data error occurred"` |
| `RuntimeException` catch-all (both)                                                                | 500         | `"An unexpected error occurred"`    |

#### Delete

`CustomErrorController.java` in `product-api` — replaced entirely by `GlobalExceptionHandler`. Its `/error` path entries removed from the API spec.

### 8. `AddItemRequest` missing `@JsonNaming`

`UpdateItemRequest` has `@JsonNaming(SnakeCaseStrategy)`. `AddItemRequest` does not. Add it for consistency.

### 9. `Behaviors` map key type

`product-api` `Behaviors` uses `Map<String, InducedBehavior>` with `.name()` lookups. `cart-service` uses the type-safe `Map<BehaviorType, InducedBehavior>`. Align `product-api` to use enum keys directly.

---

## Commit sequence

Commits 1–8 execute while the directory is still named `retail-data-services` so each change is focused and reviewable. Commit 9 is the atomic rename that touches all structural identifiers at once.

| #   | Type       | Subject                                                                           |
| --- | ---------- | --------------------------------------------------------------------------------- |
| 1   | `fix`      | align image field names to primary/alternate across both services                 |
| 2   | `fix`      | align price field names to regular/sale across both services                      |
| 3   | `fix`      | standardize item identifier to item_id across both services               |
| 4   | `fix`      | correct merch_class type from String to Integer in cart-service                   |
| 5   | `fix`      | return null for next_page when no further pages exist                             |
| 6   | `refactor` | introduce typed exceptions and consistent GlobalExceptionHandler in both services |
| 7   | `style`    | add @JsonNaming to AddItemRequest                                                 |
| 8   | `refactor` | use type-safe Map<BehaviorType, InducedBehavior> in product-api Behaviors         |
| 9   | `refactor` | rename retail-data-services to product-api with REST-idiomatic paths              |

All commits use gitzy with `--signoff`, no scopes.

---

## Commit 9 full touch-point inventory

Every file that changes in the rename commit:

### Java source files (38 files — package + import statements)

All files under `retail-data-services/src/main/java/com/target/retail/data/services/` and `retail-data-services/src/test/java/com/target/retail/data/services/`.

### Build files

| File                           | Change                                                       |
| ------------------------------ | ------------------------------------------------------------ |
| `settings.gradle.kts`          | `include("retail-data-services")` → `include("product-api")` |
| `product-api/build.gradle.kts` | `mainClass` package + `archiveFileName`                      |
| `product-api/Dockerfile`       | JAR filename ×2                                              |

### Docker / CI

| File                        | Change                                                                 |
| --------------------------- | ---------------------------------------------------------------------- |
| `docker-compose.yml`        | Service name `data:` → `product-api:`, build context, `depends_on` key |
| `.github/workflows/cd.yaml` | Matrix entries ×2                                                      |

### Spring config

| File                                              | Change                                                                       |
| ------------------------------------------------- | ---------------------------------------------------------------------------- |
| `product-api/src/main/resources/application.yml`  | Remove `context-path`, remove Swagger URL overrides                          |
| `cart-service/src/main/resources/application.yml` | `http://data:8080/retail_data_services/v1` → `http://product-api:8080/v1` ×2 |
| `cart-service/src/test/resources/application.yml` | Same ×2                                                                      |

### Controllers

| File                                                                              | Change                                                   |
| --------------------------------------------------------------------------------- | -------------------------------------------------------- |
| `ItemController`, `PriceController`, `AvailabilityController`, `HealthController` | Add `@RequestMapping("/v1")`                             |
| `CartController`                                                                  | `@RequestMapping("/cart/v1")` → `@RequestMapping("/v1")` |

### Docs / specs / scripts

| File                                       | Change                                                                 |
| ------------------------------------------ | ---------------------------------------------------------------------- |
| `retail_data_services-v1.yaml`             | Rename to `product-api-v1.yaml`; update server URL, title, description |
| `retail-data-services.http`                | Rename to `product-api.http`; update all URLs (already updated by commit 3) |
| `README.md`                                | ~25 occurrences                                                        |
| `product-api/scripts/benchmark-startup.sh` | IMAGE_NAME, HEALTH_ENDPOINT ×2, JAR path ×2                            |
| `product-api/scripts/README.md`            | 1 mention                                                              |
| `product-api/induced_behaviors.md`         | ~18 occurrences                                                        |

### CI/CD

| File                        | Line(s) | Change                                                                       |
| --------------------------- | ------- | ---------------------------------------------------------------------------- |
| `.github/workflows/cd.yaml` | 24      | Matrix entry `retail-data-services` → `product-api` (`docker-dry-run` job)  |
| `.github/workflows/cd.yaml` | 59      | Matrix entry `retail-data-services` → `product-api` (`docker-build-push` job) |

The matrix value `${{ matrix.service }}` is used as-is for the Gradle task (`:retail-data-services:build` → `:product-api:build`), Docker build context, GHCR image name, and GHA cache scope — so changing the matrix entry fixes all four in one place.

#### GHCR package rename

The published image path changes from:

```
ghcr.io/<org>/tech-case-studies-retail-data-services/retail-data-services
```

to:

```
ghcr.io/<org>/tech-case-studies-retail-data-services/product-api
```

Existing images published under the old name are not automatically redirected. Anyone pulling the old image name will need to update their reference. All tags (`latest`, branch, SHA, semver) will be published fresh under the new name on the next push to `main` after the rename commit lands.

`ci.yaml`, `zizmor.yml`, and `dependabot.yml` have no service-name references and require no changes.
