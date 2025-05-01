FROM azul/zulu-openjdk:17-latest

WORKDIR /app

# Copy the JAR file
COPY server/build/libs/delhi-metro-api-all.jar app.jar

# Copy asset files if needed
COPY app/src/main/assets assets/

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 