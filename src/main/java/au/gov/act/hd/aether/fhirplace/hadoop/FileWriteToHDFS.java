package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriteToHDFS {
    private static final Logger LOG = LoggerFactory.getLogger(FileWriteToHDFS.class);
    private static final String KERBEROS_REALM = System.getenv("REALM");
    private static final String LOGIN_USER = System.getenv("LOGIN_USER");
    private static final String NAMENODE_HOST = System.getenv("NAMENODE_HOST");
    private static final String KERBEROS_KDC = System.getenv("KDC_SERVER");
    private static final String KEYTAB_PATH = System.getenv("KEYTAB_DIR") + "/client.service.keytab";

    public void writeFileToHDFS(final String json) throws Exception {
        // Set Kerberos and Hadoop properties
        System.setProperty("java.security.krb5.realm", KERBEROS_REALM);
        System.setProperty("java.security.krb5.kdc", KERBEROS_KDC);
        System.setProperty("sun.security.krb5.debug", "true");

        // Create Hadoop configuration and set properties
        Configuration conf = new Configuration();

        conf.set("hadoop.security.authentication", "kerberos");
        conf.set("hadoop.rpc.protection", "privacy");
        conf.set("fs.defaultFS", "hdfs://" + NAMENODE_HOST);
        conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
        conf.set("dfs.namenode.kerberos.principal.pattern", "nn/*@" + KERBEROS_REALM);

        UserGroupInformation.setConfiguration(conf);
        
        // Login using Kerberos keytab
        UserGroupInformation.loginUserFromKeytab(LOGIN_USER + "@" + KERBEROS_REALM, KEYTAB_PATH);
        UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
        LOG.info("Current user: " + ugi);
        LOG.info("Login user has Kerberos credentials: " + ugi.hasKerberosCredentials());
        LOG.info("Hadoop " + conf.toString());

        // Perform HDFS operations as the logged-in user
        ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
            FileSystem fs = FileSystem.get(conf);
            Path filePath = new Path("/data/hl7-dataset/congress.json");

            // Ensure the parent directory exists
            Path parentDir = filePath.getParent();
            if (!fs.exists(parentDir)) {
                fs.mkdirs(parentDir); // Try creating the directory if it doesn't exist
            }

            // Create and write to the file
            try (FSDataOutputStream out = fs.create(filePath, true)) {
                out.writeBytes(json);
                LOG.info("Successfully written to HDFS: {}", json);
            } catch (IOException e) {
                LOG.error("Failed to write to HDFS", e);
                throw e;
            }
            return null;
        });
    }
}
