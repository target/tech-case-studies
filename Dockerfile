FROM eclipse-temurin:25-jre@sha256:04262e8782d6b034ee5d7c1c5d4e8938fcf2063a76b4bfcd84e5d994d09c27bc

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY build/libs/retail-data-services.jar /app/retail-data-services.jar

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
  "-jar", "/app/retail-data-services.jar"]
