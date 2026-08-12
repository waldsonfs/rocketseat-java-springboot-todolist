FROM ubuntu:latest AS build

WORKDIR /app

RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven

COPY . .

RUN mvn clean package -DskipTests

FROM ubuntu:latest

WORKDIR /app

RUN apt-get update && \
    apt-get install -y openjdk-17-jre && \
    apt-get clean

COPY --from=build /app/target/todolist-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]