FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/myapp.jar"]
