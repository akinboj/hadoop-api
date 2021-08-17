package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSException;

@ApplicationScoped
public class StoreHadoop {
    public void store() throws IOException, GSSException, InterruptedException, LoginException {
    	FileWriteToHDFS writer = new FileWriteToHDFS();       
        writer.writeFileToHDFS();   

    }
}