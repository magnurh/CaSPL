FROM openjdk:8

MAINTAINER  Magnus Røed Hestvik <magnus.hestvik@gmail.com>

COPY . /usr/src/CaSPL
WORKDIR /usr/src/CaSPL

RUN curl -L https://services.gradle.org/distributions/gradle-4.1-bin.zip -o gradle-4.1-bin.zip
RUN apt-get install -y unzip
RUN unzip gradle-4.1-bin.zip
RUN echo 'export GRADLE_HOME=/app/gradle-4.1’ >> $HOME/.bashrc
RUN echo 'export PATH=$PATH:$GRADLE_HOME/bin' >> $HOME/.bashrc
RUN source ~/.bashrc

RUN ./gradlew build
CMD [“./gradlew”, “run”]