FROM adoptopenjdk/openjdk11:alpine-jre

COPY *.zip /opt/app/app.zip

WORKDIR /opt/app

RUN apk add --no-cache bash jattach && unzip app.zip && rm app.zip

EXPOSE 9000

ENV JAVA_OPTS -Xms300m -Xmx300m -XX:StartFlightRecording=disk=true,dumponexit=true,filename=recording.jfr,maxsize=256m

CMD ["lobby-impl-1.0-SNAPSHOT/bin/lobby-impl", "-Dconfig.resource=production.conf"]

