FROM openjdk:17
MAINTAINER CoderWdd

COPY /target/*.jar /PicSingular.jar

EXPOSE 8806

ENTRYPOINT ["java","-jar","/PicSingular.jar"]