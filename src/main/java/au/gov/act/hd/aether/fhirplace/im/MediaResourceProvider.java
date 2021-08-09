package au.gov.act.hd.aether.fhirplace.im;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

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


@Override
protected void saveToDatabase(IDomainResource resource) {
    // TODO Auto-generated method stub
    try {
        Connection connection = getConnection();
    } catch (MasterNotRunningException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (ZooKeeperConnectionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}
   

}
