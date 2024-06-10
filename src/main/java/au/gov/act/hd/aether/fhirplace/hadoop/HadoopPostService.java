package au.gov.act.hd.aether.fhirplace.hadoop;

import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HadoopPostService extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(HadoopPostService.class);

    @Override
    public void configure() {
        // Explicit REST component configuration
        restConfiguration()
            .component("servlet") // Explicitly set to use the servlet component
            .contextPath("/fhirplace-bigdata") // Adjust if your application context path is different
            .port(32550) // Specify the correct port if necessary
            .host("pegacorn-fhirplace-bigdata-api.site-a") // Use the appropriate host for your deployment environment
            .scheme("https"); // Use "https" if your server is configured for SSL/TLS

        // Configure the REST endpoint directly under the adjusted context-root
        rest().post("/hadoopPOST") // Now accessible at /fhirplace-bigdata/hadoopPOST
            .consumes("application/json")
            .produces("application/json")
            .to("direct:storeRequest");

        // Define the consumer route for direct:storeRequest
        from("direct:storeRequest")
            .process(exchange -> LOG.info("Processing stored request: {}", exchange.getIn().getBody(String.class)))
            .bean(FileWriteToHDFS.class, "writeFileToHDFS(${body})");
    }
}
