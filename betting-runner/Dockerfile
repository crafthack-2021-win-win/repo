FROM maven:3.8.1-jdk-11 as maven

COPY . /tmp/maven
WORKDIR /tmp/maven
RUN mvn package
RUN ls -ahl /tmp/maven/target

FROM openjdk:11
COPY --from=maven /tmp/maven/target/app.jar /opt
ENTRYPOINT ["java","-jar","/opt/app.jar"]
CMD "-h"