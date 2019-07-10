FROM denvazh/scala:2.11.8-openjdk8

ARG HADOOP_VERSION=27
ARG FLINK_VERSION=1.2.1
ARG SCALA_BINARY_VERSION=2.11

ENV FLINK_INSTALL_PATH /opt
ENV FLINK_HOME $FLINK_INSTALL_PATH/flink
ENV PATH $PATH:$FLINK_HOME/bin

RUN set -x && \
    mkdir -p ${FLINK_INSTALL_PATH} && \
    apk --update add --virtual curl

RUN curl -s https://archive.apache.org/dist/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala_${SCALA_BINARY_VERSION}.tgz | tar -xz -C ${FLINK_INSTALL_PATH}

RUN ln -s ${FLINK_INSTALL_PATH}/flink-${FLINK_VERSION} ${FLINK_HOME} && \
    sed -i -e "s/echo \$mypid >> \$pid/echo \$mypid >> \$pid \&\& wait/g" ${FLINK_HOME}/bin/flink-daemon.sh && \
    sed -i -e "s/ > \"\$out\" 2>&1 < \/dev\/null//g" ${FLINK_HOME}/bin/flink-daemon.sh && \
    rm -rf /var/cache/apk/* && \
    echo Installed Flink ${FLINK_VERSION} to ${FLINK_HOME}

COPY ci/docker_entrypoint.sh ${FLINK_HOME}/bin/

# Additional output to console, allows gettings logs with 'docker-compose logs'
COPY log4j.properties ${FLINK_HOME}/conf/

# WORKDIR ${FLINK_HOME}/bin/

ENTRYPOINT ["/opt/flink/bin/docker_entrypoint.sh"]
CMD ["sh", "-c"]
