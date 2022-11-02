FROM openjdk:17 as buildstage
WORKDIR /Timely
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw package
COPY target/*.jar timely.jar

FROM openjdk:17
COPY --from=buildstage /Timely/timely.jar .
ENTRYPOINT ["java","-jar","/timely.jar"]

EXPOSE 5000