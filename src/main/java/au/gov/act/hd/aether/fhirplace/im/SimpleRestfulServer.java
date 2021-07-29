package au.gov.act.hd.aether.fhirplace.im;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

@WebServlet("/fhir/*")
public class SimpleRestfulServer extends RestfulServer {
    @Inject
    AuditEventResourceProvider auditEvent;
    @Inject
    MediaResourceProvider media;
    
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
		
		List<IResourceProvider> providers = new ArrayList<IResourceProvider>();
		providers.add(auditEvent);
		providers.add(media);
        registerProviders(providers);

		// Format the responses in nice HTML
		registerInterceptor(new ResponseHighlighterInterceptor());
	}
}
