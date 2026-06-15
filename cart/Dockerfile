FROM eclipse-temurin:17-jre

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
