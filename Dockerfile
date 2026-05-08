FROM gradle:9.1.0-jdk21 AS build
WORKDIR /src
COPY --chown=gradle:gradle . /src
RUN gradle --no-daemon shadowJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/libs/*-all.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
