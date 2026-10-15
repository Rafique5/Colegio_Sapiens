FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xms64m -Xmx300m -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
