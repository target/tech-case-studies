# Induced Behaviors

This service supports **induced behaviors** that let you simulate latency and random failures in its API responses without changing any business logic. By setting environment variables when you run the container, you can run the same APIs in different modes:

- Normal responses
- Slow responses
- Randomly failing responses

This is useful for resilience testing, and exploring how the clients (candidate's application) behave when API responses are slow or flaky.

## Behavior selection

The application chooses a single default behavior per process using the `DEFAULT_BEHAVIOR` environment variable:

- Name: `DEFAULT_BEHAVIOR`
- Type: enum-like string
- Default: `NORMAL` (if not explicitly set)
- Valid values:
  - `NORMAL`
  - `SLOW_RESPONSE`
  - `RANDOM_FAILURES`

If `DEFAULT_BEHAVIOR` is set to anything else, the service will fail when it tries to resolve the behavior (it throws a "default behaviour not configured" exception).

Assumptions in the examples:

- Container image name: `product-api`
- Container port: `8080` (mapped to different host ports as needed)

## NORMAL behavior

### What it does

- Executes the usual API-response generation logic directly.
- No artificial delay before building the response.
- No induced failures instead of building a response.

Conceptually, controllers/services call into a function that builds the API response object; in NORMAL mode, the induced behavior simply calls that function and returns the response unchanged.

### Configuration and Docker usage

**Environment variable**

- You can omit `DEFAULT_BEHAVIOR` entirely (it defaults to `NORMAL`), or
- Set it explicitly:
  - `DEFAULT_BEHAVIOR=NORMAL`

There are no additional tuning environment variables for this behavior.

**Run with `docker run`**

Implicit default (no env var):

```bash
docker run --rm \
  -p 8080:8080 \
  product-api
```

Explicitly set `NORMAL`:

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=NORMAL \
  product-api
```

**Run with `docker-compose`**

Example service definition:

```yaml
services:
  product-api-normal:
    image: product-api
    ports:
      - "8080:8080"
    environment:
      DEFAULT_BEHAVIOR: NORMAL
```

You can also omit `DEFAULT_BEHAVIOR` here; it will still default to `NORMAL`.

### Typical use cases

- Baseline demos and benchmarks of API response times.
- Comparing behavior with and without induced latency/failures.

---

## SLOW_RESPONSE behavior

### What it does

- Adds an artificial delay to the **API response generation**.
- Before executing the normal logic that builds the API response, this behavior sleeps for a randomized amount of time, then calls that logic and returns its result as the HTTP response payload.
- The delay is computed using two delay bounds:
  - A minimum delay in milliseconds.
  - A maximum-like bound that controls how long the delay can be.

In the implementation, the behavior:

1. Starts with a configured minimum delay.
2. Uses `Random.nextInt(...)` to add a random component up to a bound derived from the configured maximum and minimum.
3. Sleeps for that total delay.
4. Calls the underlying function that generates the API response and returns whatever that function produces.

Every wrapped API call experiences a delay somewhere between the minimum and an upper bound derived from the max before the response is generated and sent.

### Configuration and Docker usage

**Environment variables**

To enable slow responses:

- `DEFAULT_BEHAVIOR=SLOW_RESPONSE`

Tune how slow API responses are:

- `BEHAVIORS_SLOW_RESPONSE_MIN_DELAY_MS`
  - Default if unset: `1000` (1 second)
  - Meaning: minimum delay in milliseconds before starting to generate the API response.
- `BEHAVIORS_SLOW_RESPONSE_MAX_DELAY_MS`
  - Default if unset: `10000` (10 seconds)
  - Meaning: upper bound used when computing the random delay.
  - Should be greater than or equal to the minimum delay.

**Run with `docker run`**

Use default delay settings (around 1 to 10 seconds before each API response is generated):

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=SLOW_RESPONSE \
  product-api
```

Override delays for a milder slowdown (roughly 0.5 to 2 seconds added before generating each response):

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=SLOW_RESPONSE \
  -e BEHAVIORS_SLOW_RESPONSE_MIN_DELAY_MS=500 \
  -e BEHAVIORS_SLOW_RESPONSE_MAX_DELAY_MS=2000 \
  product-api
```

Override for a heavier slowdown (roughly 3 to 8 seconds before generating each response):

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=SLOW_RESPONSE \
  -e BEHAVIORS_SLOW_RESPONSE_MIN_DELAY_MS=3000 \
  -e BEHAVIORS_SLOW_RESPONSE_MAX_DELAY_MS=8000 \
  product-api
```

**Run with `docker-compose`**

Mild slowdown example (host port 8081):

```yaml
services:
  product-api-slow:
    image: product-api
    ports:
      - "8081:8080"
    environment:
      DEFAULT_BEHAVIOR: SLOW_RESPONSE
      BEHAVIORS_SLOW_RESPONSE_MIN_DELAY_MS: 500
      BEHAVIORS_SLOW_RESPONSE_MAX_DELAY_MS: 3000
```

Heavier slowdown variant:

```yaml
services:
  product-api-slow-heavy:
    image: product-api
    ports:
      - "8083:8080"
    environment:
      DEFAULT_BEHAVIOR: SLOW_RESPONSE
      BEHAVIORS_SLOW_RESPONSE_MIN_DELAY_MS: 3000
      BEHAVIORS_SLOW_RESPONSE_MAX_DELAY_MS: 8000
```

### Typical use cases

- Simulating slow downstream data sources affecting API responses.
- Testing UI loading indicators and request timeouts against slow APIs.
- Validating metrics and alerting for elevated API response times.

---

## RANDOM_FAILURES behavior

### What it does

- Wraps the **API response generation** in a probabilistic failure.
- For each wrapped API call, this behavior:
  1. Draws a random number between 0.0 and 1.0.
  2. Compares it to a configured failure rate.
  3. If the random number is less than the failure rate, it throws an `InducedFailureException` instead of generating the API response.
  4. Otherwise, it runs the normal logic that builds the API response and returns that response.

This causes a configurable fraction of requests to fail before a response is generated. The `GlobalExceptionHandler` maps the resulting `InducedFailureException` to HTTP 503 Service Unavailable instead of returning the normal API payload.

### Configuration and Docker usage

**Environment variables**

To enable randomly failing API responses:

- `DEFAULT_BEHAVIOR=RANDOM_FAILURES`

Tune how flaky the API responses are:

- `BEHAVIORS_RANDOM_FAILING_FAILURE_RATE`
  - Default if unset: `0.05` (about 5% of wrapped API calls fail before a response is generated)
  - Range: any double between `0.0` and `1.0`
  - Meaning: probability that a given API call will fail instead of producing a response.

**Run with `docker run`**

Use the default failure rate (~5% of API responses fail):

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=RANDOM_FAILURES \
  product-api
```

Increase the failure rate to about 25% of API calls:

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=RANDOM_FAILURES \
  -e BEHAVIORS_RANDOM_FAILING_FAILURE_RATE=0.25 \
  product-api
```

Use a very aggressive 50% failure rate (half of the API calls fail):

```bash
docker run --rm \
  -p 8080:8080 \
  -e DEFAULT_BEHAVIOR=RANDOM_FAILURES \
  -e BEHAVIORS_RANDOM_FAILING_FAILURE_RATE=0.5 \
  product-api
```

**Run with `docker-compose`**

Light flakiness (~10 to 20% of API calls) on host port 8082:

```yaml
services:
  product-api-flaky:
    image: product-api
    ports:
      - "8082:8080"
    environment:
      DEFAULT_BEHAVIOR: RANDOM_FAILURES
      BEHAVIORS_RANDOM_FAILING_FAILURE_RATE: 0.2
```

More aggressive flakiness (~50% of API calls):

```yaml
services:
  product-api-flaky-heavy:
    image: product-api
    ports:
      - "8084:8080"
    environment:
      DEFAULT_BEHAVIOR: RANDOM_FAILURES
      BEHAVIORS_RANDOM_FAILING_FAILURE_RATE: 0.5
```

### Typical use cases

- Testing client retry/backoff logic for failing APIs.
- Exercising circuit breakers and resilience libraries against flaky endpoints.
- Demonstrating user-facing error handling when APIs intermittently fail to produce responses.

---

## How behaviors are applied conceptually

- For each endpoint that opts in, the code wraps its **API-response generation** logic (for example, "build the `ItemResponse` object from data") inside an induced behavior.
- That logic is usually represented as a lambda or method reference returning the response, and passed to the configured behavior.
- The behavior then decides whether to:
  - Generate the response immediately (NORMAL),
  - Wait, then generate and return the response (SLOW_RESPONSE), or
  - Fail early without generating a response at all (RANDOM_FAILURES).

By setting only environment variables when you start the container, you can run the same API code in any of these modes. Endpoints that don't use the behavior wrapper will continue to generate responses normally.

## Use cases and tips

- **UI latency testing**
  Point your UI or client at a `SLOW_RESPONSE` instance to observe loading indicators, spinners, and timeouts when API responses are delayed.

- **Resilience and retry testing**
  Use a `RANDOM_FAILURES` instance with a moderate failure rate (e.g., 0.1 to 0.3) to exercise retries, backoff strategies, and error handling when API responses sometimes fail outright.

- **Baseline vs. stressed comparison**
  Run `NORMAL` and one induced-behavior instance in parallel (on different ports) to compare metrics, logs, and user experience between normal and stressed API behavior.

- **Safety tips**
  - Very high failure rates or very long delays can make the API appear "down." Start with conservative values and ramp up.
  - The chosen `DEFAULT_BEHAVIOR` applies process-wide. To run different modes simultaneously, start multiple containers with different environment settings.

## References

For deeper technical details, see:

- `src/main/java/com/target/retail/product/service/behavior/InducedBehavior.java`
  Functional interface that wraps a `Supplier<T>` representing the API-response generation logic and lets behaviors inject cross-cutting effects.

- `src/main/java/com/target/retail/product/service/behavior/BehaviorType.java`
  Enum declaring the supported behavior types (`NORMAL`, `SLOW_RESPONSE`, `RANDOM_FAILURES`).

- `src/main/java/com/target/retail/product/service/behavior/Behaviors.java`
  Component that reads configuration from environment variables (including `DEFAULT_BEHAVIOR`, failure rate, and delay bounds), wires behavior lambdas into a map, and exposes the configured behavior used to wrap API-response generation.

