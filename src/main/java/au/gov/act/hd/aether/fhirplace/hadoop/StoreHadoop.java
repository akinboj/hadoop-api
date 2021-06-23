package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StoreHadoop {
    private static final Logger LOG = LoggerFactory.getLogger(StoreHadoop.class);

    public void store(JSONObject jsonMessage) throws IOException {       
        LOG.debug(".store(): Entry, jsonMessage --> {}", jsonMessage.toString());
        
        FileWriteToHDFS writer = new FileWriteToHDFS();
        
        writer.writeFileToHDFS(jsonMessage);
        
        

    }
}
