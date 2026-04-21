FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# Pre-cache dependencies, then package
RUN mvn -q -B -DskipTests -T 1C dependency:go-offline \
 && mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Match whichever artifact version pom.xml currently produces
COPY --from=build /workspace/target/pvms-backend-*.jar /app/pvms-backend.jar

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx768m"

EXPOSE 8091

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/pvms-backend.jar"]
