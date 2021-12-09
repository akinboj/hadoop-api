package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSException;
import org.json.JSONObject;

@ApplicationScoped
public class StoreHadoop {
    public void store(JSONObject jsonMessage) throws IOException, GSSException, InterruptedException, LoginException {
    	FileWriteToHDFS writer = new FileWriteToHDFS();       
        writer.writeFileToHDFS(jsonMessage);   

    }
}