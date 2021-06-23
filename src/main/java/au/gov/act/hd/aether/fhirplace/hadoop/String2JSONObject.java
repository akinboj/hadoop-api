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

    public JSONObject convert2JSON(String bufferedWriter) {
        if (StringUtils.isEmpty(bufferedWriter)) {
            throw new JSONException("The payload is empty");
        }
        
//        LOG.info(bufferedWriter);
        LOG.debug(".encapsulateAPInvoicesMessage(): Entry, bufferedWriter --> {}", bufferedWriter);
        JSONObject outgoingJSONObject = new JSONObject(bufferedWriter);
        return(outgoingJSONObject);
    }
}
