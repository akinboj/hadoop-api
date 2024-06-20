FROM fhirfactory/pegacorn-base-hadoop:1.0.0

# Install Kerberos client
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        krb5-user \
        libpam-krb5 \
        libpam-ccreds \
    && rm -rf /var/lib/apt/lists/*

# Install multiple network diagnostic tools
RUN apt-get update && \
    apt-get install -y \
    iputils-ping \
    telnet \
    net-tools \
    traceroute \
    dnsutils \
    tzdata \
    && rm -rf /var/lib/apt/lists/*  # Clean up to reduce layer size

# Kerberos configuration
COPY krb5.conf /etc/krb5.conf
COPY jaas.conf /etc/jaas.conf
RUN mkdir -p /var/log/kerberos
RUN mkdir -p /etc/security/keytabs

ENV KEYTAB_DIR=/etc/security/keytabs
ENV TZ="Australia/Sydney"

# Create the group 'supergroup'
RUN groupadd supergroup

# Add the 'root' user to the 'supergroup'
RUN usermod -a -G supergroup root
    
ENV TZ="Australia/Sydney"

COPY run.sh /app/run.sh
COPY src/main/resources/core-default.xml /app/core-default.xml
COPY src/main/resources/core-site.xml /app/core-site.xml
COPY src/main/resources/hdfs-site.xml /app/hdfs-site.xml
COPY target/hadoop-poc-1.0.0-SNAPSHOT.jar /app/hadoop-poc-1.0.0-SNAPSHOT.jar

RUN chmod +x /app/run.sh

# Kube probes
RUN touch /tmp/healthy
RUN echo "JGroups hl7 application is running" > /tmp/healthy

ENTRYPOINT ["/app/run.sh"]