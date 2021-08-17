package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StoreHadoop {
    private static final Logger LOG = LoggerFactory.getLogger(StoreHadoop.class);

    public void store(JSONObject jsonMessage) throws IOException, GSSException, InterruptedException, LoginException {       
        LOG.debug(".store(): Entry, jsonMessage --> {}", jsonMessage.toString());
        
        FileWriteToHDFS writer = new FileWriteToHDFS();
        
        writer.writeFileToHDFS(jsonMessage);   

    }
}