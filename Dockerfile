FROM gradle:8.0-jdk19 AS BUILD

COPY . /opt/java-discord-music
WORKDIR /opt/java-discord-music
RUN gradle jar --no-daemon

FROM openjdk:19
RUN mkdir /opt/app
COPY --from=build /opt/java-discord-music/build/libs/java-discord-music.jar /opt/app/java-discord-music.jar
COPY config.toml /opt/app/config.toml
WORKDIR /opt/app
ENTRYPOINT ["java","-jar","java-discord-music.jar"]
