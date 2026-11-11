FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src

EXPOSE 8080

CMD ["./gradlew", "bootRun", "--no-daemon"]
