package au.gov.act.hd.aether.fhirplace.im;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Media;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.HashMap;
import java.util.Map;

public class MediaResourceProvider implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MediaResourceProvider.class);
    private int myNextId = 2;


   private Map<String, Media> myEvents = new HashMap<String, Media>();

   /**
    * Constructor
    */
   public MediaResourceProvider() {
       Media pat1 = new Media();
      pat1.setId("1");
      
      myEvents.put("1", pat1);
   }

   @Override
   public Class<? extends IBaseResource> getResourceType() {
      return Media.class;
   }

   @Read()
   public Media read(@IdParam IdType theId) {
       Media retVal = myEvents.get(theId.getIdPart());
      if (retVal == null) {
         throw new ResourceNotFoundException(theId);
      }
      return retVal;
   }

   @Create
   public MethodOutcome createEvent(@ResourceParam Media theMedia) {
       // Give the resource the next sequential ID
       int id = myNextId++;
       theMedia.setId(new IdType(id));
       
       LOG.info("Media registered: " + theMedia.fhirType());

       // Store the resource in memory
       myEvents.put(Integer.toString(id), theMedia);

       // Inform the server of the ID for the newly stored resource
       return new MethodOutcome().setId(theMedia.getIdElement());
   }


}
