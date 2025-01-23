# Benchmark Scripts

## benchmark-startup.sh

Measures Docker container startup time for retail-data-services.

### Usage

```bash
# Run with default 3 iterations
./scripts/benchmark-startup.sh

# Run with 5 iterations
./scripts/benchmark-startup.sh 5
```

### Prerequisites

- Application JAR built: `./gradlew clean build`
- Port 8080 available (or script will use 8081)

### Output

Results saved to `benchmark-results-{timestamp}.txt`

### Metrics

- **Spring Boot startup**: Time from JVM start to application ready
- **Health check ready**: Time from container start to first successful health check
- **Image size**: Docker image size
