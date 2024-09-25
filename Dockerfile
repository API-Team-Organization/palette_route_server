FROM amazoncorretto:17
COPY build/libs/palette-0.0.1-SNAPSHOT.jar palette.jar
COPY production.env .env

ENTRYPOINT [
    "set", "-a", "&&", "source", ".env", "&&", "set", "+a;",
    "java", "-jar", "palette.jar", "--spring.config.location=classpath:/application.yml,classpath:/application-production.yml"
]
