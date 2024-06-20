#!/bin/bash
# python.sh

# Initialize Kerberos
echo "Obtaining Kerberos ticket..."
kinit -kt /etc/ssl/keytab/merged-krb5.keytab myapp/pegacorn-fhirplace-bigdata-api-0.pegacorn-fhirplace-bigdata-api.site-a.svc.cluster.local@PEGACORN-FHIRPLACE-AUDIT.LOCAL

# Keep renewing the ticket
while true; do
    sleep 3600
    kinit -R
done &

echo "Starting the application..."

# Start your application
exec python3 /app/hdfs_access.py