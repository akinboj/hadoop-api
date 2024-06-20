package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HadoopPostService extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(HadoopPostService.class);
    private static final String SERVICE_NAME = System.getenv("KUBERNETES_SERVICE_NAME") + "." + System.getenv("KUBERNETES_NAMESPACE");
    private static final int SERVICE_PORT = 8443;
    private static final String HOST_ADDRESS = System.getenv("MY_POD_IP");
    
    public void configureSSL() {
        // Keystore parameters
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(SERVICE_NAME + ".jks");
        ksp.setPassword(System.getenv("KEY_PASSWORD")); // Keystore password

        // Key manager parameters
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword(System.getenv("KEY_PASSWORD")); // Key password

        // Truststore parameters
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource("truststore.jks"); // Adjust the path
        tsp.setPassword(System.getenv("TRUSTSTORE_PASSWORD")); // Truststore password

        // Trust manager parameters
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);

        // Setup SSL context parameters
        SSLContextParameters scp = new SSLContextParameters();
        scp.setKeyManagers(kmp);
        scp.setTrustManagers(tmp);

        // Apply to Jetty component
        getContext().getComponent("jetty", JettyHttpComponent.class)
                    .setSslContextParameters(scp);
    }


    @Override
    public void configure() {
    	configureSSL(); // Set up SSL
    	
        // Configure Jetty component
        restConfiguration()
            .component("jetty")
            .host(HOST_ADDRESS) // Listen on all network interfaces
            .port(SERVICE_PORT) // Specify the port to listen on
            .bindingMode(RestBindingMode.auto)
            .scheme("https");

        // Define the REST endpoint
        rest("/fhirplace-bigdata")
            .post("/hadoopPOST")
            .consumes("application/json")
            .produces("application/json")
            .to("direct:processJSON");

        // Define the processing route
        from("direct:processJSON")
            .log("Received JSON: ${body}")
            .process(exchange -> {
                String json = exchange.getIn().getBody(String.class);
                LOG.info("Processing JSON: {}", json);
                // You can add more processing logic here
            })
            .bean(FileWriteToHDFS.class, "writeFileToHDFS(${body})");
    }
}
