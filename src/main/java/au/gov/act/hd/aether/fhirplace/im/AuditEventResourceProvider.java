package au.gov.act.hd.aether.fhirplace.im;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class AuditEventResourceProvider extends BaseResourceProvider implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AuditEventResourceProvider.class);
    private int myNextId = 2;

    private Map<String, AuditEvent> myEvents = new HashMap<String, AuditEvent>();

    /**
     * Constructor
     */
    public AuditEventResourceProvider() {
        AuditEvent pat1 = new AuditEvent();
        pat1.setId("1");

        myEvents.put("1", pat1);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AuditEvent.class;
    }

    @Read()
    public AuditEvent read(@IdParam IdType theId) {
        AuditEvent retVal = myEvents.get(theId.getIdPart());
        if (retVal == null) {
            throw new ResourceNotFoundException(theId);
        }
        return retVal;
    }

    @Create
    public MethodOutcome createEvent(@ResourceParam AuditEvent theEvent) {
        // Give the resource the next sequential ID
        int id = myNextId++;
        theEvent.setId(new IdType(id));

        LOG.info("AuditEvent registered: " + theEvent.fhirType());

        try {
            String fileName = generateName();
            String parsedResource = parseResourceToJsonString(theEvent);
            LOG.info("AuditEvent parsed: " + parsedResource);
            
           writeToFileSystem(fileName, parsedResource);

            // Store the resource in memory
            myEvents.put(Integer.toString(id), theEvent);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Inform the server of the ID for the newly stored resource
        return new MethodOutcome().setId(theEvent.getIdElement());
    }
    
    private String generateName() {
        return "AuditEvent-" + myNextId;
    }

}
