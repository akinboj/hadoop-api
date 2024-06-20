# Using the ARM64 Debian Buster slim image as the base
FROM arm64v8/debian:buster-slim

# Install necessary packages for adding a new repository
RUN apt-get update && apt-get install -y wget gnupg software-properties-common \
    && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - \
    && echo "deb https://packages.adoptium.net/artifactory/deb buster main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y temurin-17-jdk \
    && apt-get install -y --no-install-recommends \
        net-tools \
        curl \
        netcat \
        gnupg \
        wget \
        libsnappy-dev \
        tzdata \
        sudo \
    && rm -rf /var/lib/apt/lists/* # Clean up unnecessary files to reduce the image size

# Set JAVA_HOME environment variable, helpful for some applications
ENV JAVA_HOME /usr/lib/jvm/temurin-17-jdk-arm64

# Set PATH environment variable
ENV PATH $JAVA_HOME/bin:$PATH

# Verify installation
RUN java -version

# Install Kerberos client
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        krb5-user \
        libpam-krb5 \
        libpam-ccreds \
    && rm -rf /var/lib/apt/lists/*
    
# Kerberos config
RUN mkdir -p etc/ssl/keytab
ENV KEYTAB_DIR=/etc/ssl/keytab

COPY krb5.conf /etc/krb5.conf
COPY jaas.conf /etc/jaas.conf

# Install multiple network diagnostic tools
RUN apt-get update && \
    apt-get install -y \
    iputils-ping \
    telnet \
    net-tools \
    traceroute \
    dnsutils \
    && rm -rf /var/lib/apt/lists/*  # Clean up to reduce layer size

# Install Python3 and pip3
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        python \
        python3 \
        python3-pip \
        git \
        bash \
    && rm -rf /var/lib/apt/lists/*

RUN bash Miniconda3-latest-Linux-x86_64.sh

RUN pip install hdfs
    
ENV TZ="Australia/Sydney"

# Copy your Python script and entrypoint script
COPY hdfs_access.py /app/hdfs_access.py
COPY python.sh /app/python.sh
RUN chmod +x /app/python.sh

WORKDIR /app

ENTRYPOINT ["/app/python.sh"]
