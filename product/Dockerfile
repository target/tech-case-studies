FROM eclipse-temurin:17-jre@sha256:3443191793fb27f7df8bab42821d149eec9e280ab28e816e713387548c4dfcc6

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY build/libs/product.jar /app/product.jar

# Create the /data directory
RUN mkdir /data

# Add a volume to mount the CSV file
VOLUME ["/data"]

COPY src/main/resources/prices.csv /data/prices.csv
COPY src/main/resources/availability.csv /data/availability.csv
COPY src/main/resources/items.csv /data/items.csv

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", \
  "-XX:+TieredCompilation", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseSerialGC", \
  "-Xss256k", \
  "-jar", "/app/product.jar"]
