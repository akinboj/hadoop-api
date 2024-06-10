package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.camel.impl.DefaultCamelContext;

public class MainApp {
    public static void main(String[] args) {
        try (DefaultCamelContext camelContext = new DefaultCamelContext()) {
            // Add route builders to the context
            camelContext.addRoutes(new HadoopPostService());
            camelContext.start();
            // Keep main thread running until you decide to stop the context
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();  // Consider using a logger here for better error management
        }
    }
}
