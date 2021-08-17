package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class KerberosCallBackHandler implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        // call database or retrieve credentials by other means
    	String loginUser = (System.getenv("LOGIN_USER"));
	    String keyTabPath = (System.getenv("KEYTAB_PATH"));
//    	String user = "CHANGEME";
//        String password = "CHANGEME";

        for (Callback callback : callbacks) {

            if (callback instanceof NameCallback) {
                NameCallback nc = (NameCallback) callback;
                nc.setName(loginUser);
            } else if (callback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callback;
                pc.setPassword(keyTabPath.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback, "Unknown Callback");
            }

        }
    }
}