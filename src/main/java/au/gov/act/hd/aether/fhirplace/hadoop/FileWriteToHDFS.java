package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriteToHDFS {
    private static final Logger LOG = LoggerFactory.getLogger(FileWriteToHDFS.class);
    private static final String KERBEROS_REALM = "PEGACORN-FHIRPLACE-AUDIT.LOCAL";
    private static final String KERBEROS_KDC = System.getenv("KDC_SERVER");
    private static final String KERBEROS_CONFIG_FILE = "/etc/krb5.conf";
    private static final String JAAS_CONFIG_FILE = "/etc/jaas.conf";
    private static final String NAMENODE_HOST = System.getenv("NAMENODE_HOST");
    private static final String LOGIN_USER = System.getenv("LOGIN_USER");
    private static final String KEYTAB_PATH = System.getenv("KEYTAB_PATH") + "/client-krb5.keytab";

    public void writeFileToHDFS(final String json) throws Exception {
        configureSystemProperties();
        UserGroupInformation loginUser = loginKerberos();

        loginUser.doAs(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                writeFile(json);
                return null;
            }
        });
    }

    private void configureSystemProperties() {
        System.setProperty("java.security.krb5.realm", KERBEROS_REALM);
        System.setProperty("java.security.krb5.kdc", KERBEROS_KDC);
        System.setProperty("java.security.krb5.conf", KERBEROS_CONFIG_FILE);
        System.setProperty("java.security.auth.login.config", JAAS_CONFIG_FILE);
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.spnego.debug", "true");
        System.setProperty("java.security.debug", "gssapi:trace,configfile,configparser,logincontext");
    }

    private UserGroupInformation loginKerberos() throws IOException {
        Configuration conf = new Configuration();
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(LOGIN_USER + "@" + KERBEROS_REALM, KEYTAB_PATH);
        LOG.info("Kerberos authentication configured successfully with principal: {}", LOGIN_USER + "@" + KERBEROS_REALM);
        return UserGroupInformation.getLoginUser();
    }

    private void writeFile(String json) throws IOException {
        Configuration conf = new Configuration();
        setupHadoopSecurity(conf);
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
    }

    private void setupHadoopSecurity(Configuration conf) {
        conf.set("fs.defaultFS", "hdfs://" + NAMENODE_HOST + ":8020");
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("dfs.client.use.datanode.hostname", "false");
        conf.set("dfs.namenode.kerberos.principal", "root/" + NAMENODE_HOST + "@" + KERBEROS_REALM);
        conf.set("hadoop.rpc.protection", "privacy");
        conf.set("hadoop.security.authorization", "false");
        conf.set("dfs.data.transfer.protection", "privacy");
        conf.set("dfs.encrypt.data.transfer", "true");
        conf.set("hadoop.security.token.service.use_ip", "true");
    }
}
