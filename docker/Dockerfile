FROM openjdk:8-jre

ENV APP=tveebot
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME

VOLUME /downloads
VOLUME /library

RUN mkdir -p /config
COPY docker/resources/* /config/

COPY app/build/distributions/$APP-*.zip .
RUN unzip $APP-*.zip \
    && rm $APP-*.zip \
    && mv $APP-*/* .

EXPOSE 8080
CMD $APP_HOME/bin/$APP -c /config/application.conf
