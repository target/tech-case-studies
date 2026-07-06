FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.description="Shopping cart REST API with CRUD operations. Calls product at runtime for item and price enrichment."

WORKDIR /app

COPY build/libs/cart.jar /app/cart.jar

RUN mkdir -p /data/carts/

VOLUME ["/data"]

COPY src/main/resources/data/* /data/carts/

EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+TieredCompilation", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseSerialGC", \
  "-Xss256k", \
  "-jar", "/app/cart.jar"]
