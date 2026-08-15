FROM eclipse-temurin:8-jdk-alpine
ADD target/DBMicroservice-1.0-SNAPSHOT-jar-with-dependencies.jar DBMicroservice.jar
ADD db.txt db.txt

ENTRYPOINT ["java","-jar", "DBMicroservice.jar"]