package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedExceptionAction;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;

public class FileWriteToHDFS {
    private static final Logger LOG = LoggerFactory.getLogger(FileWriteToHDFS.class);
    private static final String KERBEROS_REALM = "PEGACORN-FHIRPLACE-AUDIT.LOCAL";
    private static final String KERBEROS_KDC = System.getenv("KDC_SERVER");
    private static final String KERBEROS_CONFIG_FILE = "/etc/krb5.conf";
    private static final String JAAS_CONFIG_FILE = "/etc/jaas.conf";
    private static final String NAMENODE_HOST = System.getenv("NAMENODE_HOST");
    private static final String LOGIN_USER = System.getenv("LOGIN_USER");
    private static final String KEYTAB_PATH = System.getenv("KEYTAB_PATH") + "/hbase-krb5.keytab";
    
    public void init() throws ServletException {
        // Initialize Kerberos authentication during servlet initialization
        initializeKerberosAuthentication();
    }
    
    private void initializeKerberosAuthentication() {
        try {
            LoginContext lc = new LoginContext("KerberosLogin");
            lc.login(); // Perform Kerberos authentication
            // Access the authenticated subject using lc.getSubject()
        } catch (LoginException e) {
            // Handle authentication failure
            e.printStackTrace();
        }
    }

    public void writeFileToHDFS(final String json) throws Exception {
        configureKerberos();

        UserGroupInformation.getCurrentUser().doAs(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", "hdfs://" + NAMENODE_HOST + ":8020");
                conf.set("hadoop.security.authentication", "kerberos");
                FileSystem fs = FileSystem.get(conf);
                Path filePath = new Path("/data/sample-dataset/mock.json");

                // Ensure the parent directory exists
                Path parentDir = filePath.getParent();
                if (!fs.exists(parentDir)) {
                    fs.mkdirs(parentDir); // Try creating the directory if it doesn't exist
                }

                // Now create the file
                try (FSDataOutputStream out = fs.create(filePath, true)) {
                    out.writeBytes(json);
                    LOG.info("Successfully written to HDFS: {}", json);
                } catch (Exception e) {
                    LOG.error("Failed to write to HDFS", e);
                    throw e;
                }
                return null;
            }
        });
    }

    private void configureKerberos() throws Exception {
        System.setProperty("java.security.krb5.realm", KERBEROS_REALM);
        System.setProperty("java.security.krb5.kdc", KERBEROS_KDC);
        System.setProperty("java.security.krb5.conf", KERBEROS_CONFIG_FILE);
        System.setProperty("java.security.auth.login.config", JAAS_CONFIG_FILE);
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.spnego.debug", "true");
        System.setProperty("java.security.debug", "gssapi:trace,configfile,configparser,logincontext");
                
        Configuration hdfsConfig = new Configuration();
        hdfsConfig.set("hadoop.security.authentication", "kerberos");
        hdfsConfig.set("hadoop.rpc.protection", "privacy");
        hdfsConfig.set("hadoop.security.authorization", "false");
        hdfsConfig.set("dfs.data.transfer.protection", "privacy");
        hdfsConfig.set("dfs.encrypt.data.transfer", "true");
        hdfsConfig.set("fs.defaultFS", "hdfs://" + NAMENODE_HOST + ":8020");
        hdfsConfig.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        hdfsConfig.set("dfs.client.use.datanode.hostname", "false");
        hdfsConfig.set("dfs.namenode.kerberos.principal", "nn/" + NAMENODE_HOST + "@" + KERBEROS_REALM);

        UserGroupInformation.setConfiguration(hdfsConfig);
        UserGroupInformation.loginUserFromKeytab(LOGIN_USER + "@" + KERBEROS_REALM, KEYTAB_PATH);

        LOG.info("Kerberos authentication configured successfully with principal: {}", LOGIN_USER + "@" + KERBEROS_REALM);
    }
}
