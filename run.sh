#!/bin/bash

set -e

echo "==== Authenticating to realm ==============================================================="
echo "============================================================================================"
# Initialize Kerberos
KRB5_TRACE=/dev/stderr kinit -f fn/pegacorn-fhirplace-bigdata-api-0.pegacorn-fhirplace-bigdata-api.site-a.svc.cluster.local@${REALM} -kt ${KEYTAB_DIR}/client.service.keytab -V &
wait -n
echo "Application server TGT completed."
echo ""

echo "==== Confirm Kerberos credential cache ======================================================="
echo "=============================================================================================="
klist
echo ""

echo "==== Confirming HDFS config files =============================================================="
echo "================================================================================================"
rm -f /etc/hadoop/core-site.xml
rm -f /etc/hadoop/hdfs-site.xml
cp -f /app/core-default.xml /etc/hadoop/
cp -f /app/core-site.xml /etc/hadoop/
cp -f /app/hdfs-site.xml /etc/hadoop/
echo "================================================================================================"
echo "core-default.xml CONFIGURATION:"
echo ""
cat /etc/hadoop/core-default.xml
echo ""
echo "================================================================================================"
echo "core-site.xml CONFIGURATION:"
echo ""
cat /etc/hadoop/core-site.xml
echo ""
echo ""
echo "================================================================================================"
echo "hdfs-site.xml CONFIGURATION:"
echo ""
cat /etc/hadoop/hdfs-site.xml
echo ""
echo "==== Confirmed HDFS configuration ==============================================================="

# Start the Java application
echo "==== Starting the application ==================================================================="
echo "================================================================================================="
java -Djava.security.auth.login.config=/etc/jaas.conf -Djavax.security.auth.useSubjectCredsOnly=false -Dsun.security.krb5.debug=true -Dsun.security.jgss.debug=true -jar /app/hadoop-poc-1.0.0-SNAPSHOT.jar
echo ""