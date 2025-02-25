#!/bin/bash

set -e

# Replace /etc/krb5.conf with the specified configuration
cat << EOF > /etc/krb5.conf
[libdefaults]
 default_realm = ${REALM}
 dns_lookup_realm = false
 dns_lookup_kdc = false
 ticket_lifetime = 24h
 renew_lifetime = 7d
 forwardable = true
 spake_preauth_groups = edwards25519
 kdc_timesync = 1
 ccache_type = 4
 proxiable = true
 udp_preference_limit = 1
 
[realms]
 ${REALM} = {
  kdc = ${KDC_SERVER}
  admin_server = ${KDC_SERVER}
 }

[domain_realm]
 .site-a = ${REALM}
 site-a = ${REALM}
 .svc.cluster.local = ${REALM}
 svc.cluster.local = ${REALM}

[plugins]
 disable_pkinit = true
EOF

echo "Kerberos configuration updated at /etc/krb5.conf"

# Start the Java application
exec java -jar /app/hadoop-poc-1.0.0-SNAPSHOT.jar