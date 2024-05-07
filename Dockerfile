FROM openjdk:17
COPY build/libs/palette-0.0.1-SNAPSHOT.jar palette.jar
RUN ["java", "-jar", "palette.jar"]