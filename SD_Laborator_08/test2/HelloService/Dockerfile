FROM eclipse-temurin:8-jdk-alpine
ADD target/HelloService-1.0-SNAPSHOT-jar-with-dependencies.jar HelloMicroservice.jar
ENTRYPOINT ["java", "-jar", "HelloMicroservice.jar"]