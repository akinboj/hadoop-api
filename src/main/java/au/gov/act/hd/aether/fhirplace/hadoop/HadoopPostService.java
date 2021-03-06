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
public class HadoopPostService extends  RouteBuilderCommonBase{
    private static final Logger LOG = LoggerFactory.getLogger(HadoopPostService.class);


	@Override
	protected Logger getLogger() {
		return LOG;
	}

	
	@Override
	protected void configureManagedRoutes() {

		rest().post("/hadoopPOST")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:storeRequest");
        // Validate the request
        from("direct:storeRequest")
        	.bean(String2JSONObject.class, "convert2JSON")
        	.bean(StoreHadoop.class);
	}
        	
                       
}
