# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the application jar file to the container
COPY target/cafedb-0.0.1-SNAPSHOT.jar /app/cafedb-0.0.1-SNAPSHOT.jar

# Specify the command to run your application
ENTRYPOINT ["java", "-jar", "/app/cafedb-0.0.1-SNAPSHOT.jar"]