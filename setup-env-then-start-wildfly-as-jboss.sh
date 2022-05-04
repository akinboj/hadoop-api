#!/usr/bin/env bash
# NOTE: this file should have Unix (LF) EOL conversion performed on it to avoid: "env: can't execute 'bash ': No such file or directory"

echo "Staring setup-env-then-start-wildfly-as-jboss.sh as user $(whoami) with params $@"

echo "DOCKER IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}"
echo "HELM_RELEASE_TIME=${HELM_RELEASE_TIME}"

# Update modules.xml for vulnerable jars that were updated in the Docker image
sed -i "s/sshd-core-2.6.0.jar/sshd-core-2.7.0.jar/g" $JBOSS_HOME/modules/system/layers/base/org/apache/sshd/main/module.xml
sed -i "s/jackson-databind-2.12.3.jar/jackson-databind-2.12.6.1.jar/g" $JBOSS_HOME/modules/system/layers/base/com/fasterxml/jackson/core/jackson-databind/main/module.xml
sed -i "s/libthrift-0.13.0.jar/libthrift-0.14.0.jar/g" $JBOSS_HOME/modules/system/layers/base/org/apache/thrift/main/module.xml
sed -i "s/xmlsec-2.1.6.jar/xmlsec-2.2.3.jar/g" $JBOSS_HOME/modules/system/layers/base/org/apache/santuario/xmlsec/main/module.xml
sed -i "s/jsoup-1.8.3.jar/jsoup-1.14.3.jar/g" $JBOSS_HOME/modules/system/layers/base/org/jsoup/main/module.xml
sed -i "s/jaeger-core-1.5.0.jar/jaeger-core-1.6.0.jar/g" $JBOSS_HOME/modules/system/layers/base/io/jaegertracing/jaeger/main/module.xml
sed -i "s/jaeger-thrift-1.5.0.jar/jaeger-thrift-1.6.0.jar/g" $JBOSS_HOME/modules/system/layers/base/io/jaegertracing/jaeger/main/module.xml

# Copy the certificate files based on
# 1. https://stackoverflow.com/questions/55072221/deploying-postgresql-docker-with-ssl-certificate-and-key-with-volumes
# 2. https://itnext.io/postgresql-docker-image-with-ssl-certificate-signed-by-a-custom-certificate-authority-ca-3df41b5b53

echo "Copying certificates like /var/lib/pegacorn-ssl-certs/ca.cer to /etc/ssl/certs/"

cp /var/lib/pegacorn-ssl-certs/ca.cer /etc/ssl/certs/pegacorn-ca.cer

chmod 400 /etc/ssl/certs/pegacorn-ca.cer
chown jboss:jboss /etc/ssl/certs/pegacorn-ca.cer 

ls -la /etc/ssl/certs/

mkdir -p /var/lib/pegacorn-keystores
cp /var/lib/pegacorn-ssl-certs/${EXTERNAL_DNS_ENTRY:-$KUBERNETES_SERVICE_NAME.$MY_POD_NAMESPACE}.jks /var/lib/pegacorn-keystores/keystore.jks
cp /var/lib/pegacorn-ssl-certs/truststore.jks /var/lib/pegacorn-keystores/truststore.jks

chmod 400 /var/lib/pegacorn-keystores/keystore.jks
chown jboss:jboss /var/lib/pegacorn-keystores/keystore.jks 
chmod 400 /var/lib/pegacorn-keystores/truststore.jks
chown jboss:jboss /var/lib/pegacorn-keystores/truststore.jks 

ls -la /var/lib/pegacorn-keystores/

# then start /start-wildfly.sh script as jboss user
# NOTE: gosu is used instead of su-exec as the wildfly docker image is based on centos, whereas the postgres one is based on alpine,
# and the Alpine su-exec program is a substitute for gosu (see https://devops.stackexchange.com/a/5242 and
# https://github.com/docker-library/postgres/blob/33bccfcaddd0679f55ee1028c012d26cd196537d/12/docker-entrypoint.sh line 281 vs
# https://github.com/docker-library/postgres/blob/33bccfcaddd0679f55ee1028c012d26cd196537d/12/alpine/docker-entrypoint.sh line 281
exec gosu jboss "/start-wildfly.sh" "$@"
