package au.gov.act.hd.aether.fhirplace.hadoop;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class String2JSONObject {
    private static final Logger LOG = LoggerFactory.getLogger(String2JSONObject.class);

    public JSONObject convert2JSON(String incomingPacket) {
        if (StringUtils.isEmpty(incomingPacket)) {
            throw new JSONException("The payload is empty");
        }
        
        LOG.debug(".encapsulateAPInvoicesMessage(): Entry, incomingPacket --> {}", incomingPacket);
        JSONObject outgoingJSONObject = new JSONObject(incomingPacket);
        return(outgoingJSONObject);
    }
}
