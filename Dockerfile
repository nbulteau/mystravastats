# Use an OpenJDK Runtime as a parent image
FROM openjdk:18.0.2-slim-buster

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update &&  \
    apt-get -y install --no-install-recommends libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev libgconf-2-4 libnss3 libxss1 libasound2 libxtst6 xauth xvfb xdg-utils git

RUN git clone https://github.com/nbulteau/mystravastats.git

# Set the working directory to /mystravastats
WORKDIR mystravastats

RUN ./gradlew build

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Add a volume pointing to /app
VOLUME /mystravastats

CMD ["./gradlew", "run"]
