package au.gov.act.hd.aether.fhirplace.hadoop;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CopyHadoop {
    private static final Logger LOG = LoggerFactory.getLogger(CopyHadoop.class);

    public void store(JSONObject jsonMessage) {       
        LOG.debug(".store(): Entry, jsonMessage --> {}", jsonMessage.toString());

    }
}
