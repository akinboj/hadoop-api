package au.gov.act.hd.aether.fhirplace.im;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MediaResourceProvider extends BaseResourceProvider implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MediaResourceProvider.class);
    private int myNextId = 2;


   /**
    * Constructor
    */
   public MediaResourceProvider() {
   }

   @Override
   public Class<? extends IBaseResource> getResourceType() {
      return Media.class;
   }

   @Read()
   public Media read(@IdParam IdType theId) {
//       Media retVal = null;
//      if (retVal == null) {
         throw new ResourceNotFoundException(theId);
//      }
//      return retVal;
   }

   @Create
   public MethodOutcome createMedia(@ResourceParam Media theEvent) {
       // Give the resource the next sequential ID
       int id = myNextId++;
       theEvent.setId(new IdType(id));

       LOG.info("Media registered: " + theEvent.fhirType());

       try {
           String fileName = generateName();
           String parsedResource = parseResourceToJsonString(theEvent);
           LOG.info("Media parsed: " + parsedResource);
           
          writeToFileSystem(fileName, parsedResource);


       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       // Inform the server of the ID for the newly stored resource
       return new MethodOutcome().setId(theEvent.getIdElement());
   }

@Override
protected String generateName() {
    return "Media-" + myNextId;
}
   

}
