FROM eclipse-temurin:11-jre-ubi9-minimal

# Configure build options
ARG IMAGE_BUILD_TIMESTAMP
ENV IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}
RUN echo IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}

# Kerberos configuration
RUN mkdir -p /etc/security/keytabs
ENV KEYTAB_DIR=/etc/security/keytabs

# Set timezone
ENV TZ="Australia/Sydney"


COPY target/hadoop-poc-1.0.0-SNAPSHOT.jar /app/hadoop-poc-1.0.0-SNAPSHOT.jar

# Copy the startup script
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

WORKDIR /app

# Use the script to start the application
CMD ["/app/start.sh"]