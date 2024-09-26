FROM amazoncorretto:17
COPY build/libs/palette-0.0.1-SNAPSHOT.jar palette.jar

ENTRYPOINT ["java", "-jar", "palette.jar", "--spring.config.location=classpath:/application.yml,classpath:/application-production.yml"]
