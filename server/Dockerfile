FROM azul/zulu-openjdk:17-latest

WORKDIR /app

# Copy the JAR file
COPY build/libs/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 