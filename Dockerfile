
# Amazon Corretto 25 on Alpine
FROM amazoncorretto:25-alpine

# Install curl
RUN apk update && apk add --no-cache curl

# Copy the jar file into the image
COPY target/app.jar /app.jar

# Expose Javalin port
EXPOSE 7070

# Start application
CMD ["java", "-jar", "/app.jar"]
