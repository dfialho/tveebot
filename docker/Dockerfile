FROM openjdk:8-jre

ENV APP=tveebot
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME

VOLUME /downloads
VOLUME /library
VOLUME /config

COPY app/build/distributions/$APP-*.zip .
RUN unzip $APP-*.zip \
    && rm $APP-*.zip \
    && mv $APP-*/* . \
    && rm -rf $APP-*/

COPY docker/resources/conf/* conf/

EXPOSE 8080
CMD $APP_HOME/bin/$APP
