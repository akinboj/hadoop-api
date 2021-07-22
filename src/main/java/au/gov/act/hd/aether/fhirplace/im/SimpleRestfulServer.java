package au.gov.act.hd.aether.fhirplace.im;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

@WebServlet("/fhir/*")
public class SimpleRestfulServer extends RestfulServer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRestfulServer.class);
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
	protected void initialize() throws ServletException {
        LOG.info("SimpleRestfulServer - Initialised");
		// Create a context for the appropriate version
		setFhirContext(FhirContext.forR4());
		
		// Register resource providers
		registerProvider(new PatientResourceProvider());
	    registerProvider(new AuditEventResourceProvider());
        registerProvider(new MediaResourceProvider());

		// Format the responses in nice HTML
		registerInterceptor(new ResponseHighlighterInterceptor());
	}
}
