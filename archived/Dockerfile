FROM openjdk:8-jdk-alpine

Run apk update && \
    apk upgrade && \
    apk add git

RUN apk add maven --update-cache --repository http://dl-4.alpinelinux.org/alpine/edge/community/ --allow-untrusted \
	&& rm -rf /var/cache/apk/*

ENV MAVEN_HOME /usr/share/java/maven-3
ENV PATH $PATH:$MAVEN_HOME/bin
ENV ORION_BROKER http://orion:1026

WORKDIR /app

RUN git clone https://github.com/fiware/NGSI-LD_Wrapper.git

WORKDIR /app/NGSI-LD_Wrapper

RUN mvn install

EXPOSE 8080

ENTRYPOINT exec java -jar `pwd`/target/NGSI-LD_Wrapper-1.0-SNAPSHOT-jar-with-dependencies.jar ${ORION_BROKER}
