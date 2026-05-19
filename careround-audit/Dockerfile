FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY careround-core/pom.xml careround-core/
COPY careround-notification/pom.xml careround-notification/
COPY careround-audit/pom.xml careround-audit/

RUN mvn dependency:go-offline -pl careround-audit -am -q

COPY careround-audit/src careround-audit/src

RUN mvn clean package -pl careround-audit -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S careround && adduser -S careround -G careround
WORKDIR /app
COPY --from=builder /build/careround-audit/target/careround-audit-*.jar /app/app.jar
RUN chown careround:careround /app/app.jar
USER careround
EXPOSE 8082
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
