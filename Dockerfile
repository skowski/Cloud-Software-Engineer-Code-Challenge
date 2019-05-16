# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine

# Add Ktor user
ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

# Add application dir
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

# Set user
USER $APPLICATION_USER

# Copy application JAR (Fat JAR)
COPY ./build/libs/otto-code-challenge.jar /app/app.jar
WORKDIR /app

# Run
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "app.jar"]