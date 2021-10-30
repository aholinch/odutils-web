# syntax=docker/dockerfile:1
FROM debian:buster-slim
FROM openjdk:8
#RUN apk add bash libc6-compat=1.1.19-r11 --update-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.8/main
WORKDIR /app
ENV LD_LIBRARY_PATH /app
COPY target/* /app/
COPY target/web/* /app/web/
EXPOSE 9000
ENTRYPOINT ["java","-Djava.librarypath=/app","-classpath","/app/odutils-web-dist.jar:/app","odutils.web.ODUtilsServer"]
