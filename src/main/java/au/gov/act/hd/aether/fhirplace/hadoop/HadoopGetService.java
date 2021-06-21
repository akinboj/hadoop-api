/**
 * Copyright (c) 2020 ACT Health
 */
package au.gov.act.hd.aether.fhirplace.hadoop;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the POST service WUPs
 * 
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class HadoopGetService extends  RouteBuilderCommonBase{
    private static final Logger LOG = LoggerFactory.getLogger(HadoopGetService.class);


	@Override
	protected Logger getLogger() {
		return LOG;
	}

	
	@Override
	protected void configureManagedRoutes() {
        rest().get("/hadoopGET")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:retrieveRequest");

        // Validate the request
        from("direct:retrieveRequest")
        	.process(exchange -> {
        		LOG.info("Yemi.  GET service called");
        	});
	}                   
}