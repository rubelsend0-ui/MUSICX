FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system jarvisgroup && adduser --system --ingroup jarvisgroup jarvisuser
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown jarvisuser:jarvisgroup app.jar
USER jarvisuser
ENV PORT=8080
EXPOSE ${PORT}
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
