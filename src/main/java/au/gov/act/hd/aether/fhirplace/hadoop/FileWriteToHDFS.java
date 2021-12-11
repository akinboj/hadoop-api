// https://gist.github.com/dbathgate/87986ad5c659084dd0710f471de66e00
package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.ietf.jgss.GSSException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriteToHDFS {
	private static final Logger LOG = LoggerFactory.getLogger(FileWriteToHDFS.class);

  public void writeFileToHDFS(JSONObject jsonMessage) throws IOException, GSSException, LoginException, InterruptedException {
		  // set kerberos host and realm
		  String realm = "PEGACORN-FHIRPLACE-AUDIT.LOCAL";
		  String fqdn = "pegacorn-fhirplace-namenode.site-a.svc.cluster.local";
	      String kdcServer = (System.getenv("KDC_SERVER"));
	      String namenodeIP = (System.getenv("NAMENODE_IP"));
	      String loginUser = (System.getenv("LOGIN_USER"));
	      String keyTabPath = (System.getenv("KEYTAB_PATH"));
	      
	      System.setProperty("java.security.krb5.realm", realm);
	      System.setProperty("java.security.krb5.kdc", kdcServer+":88");
	
	      Configuration conf = new Configuration();
	      conf.set("hadoop.security.authentication", "kerberos");
	      conf.set("hadoop.security.authorization", "true");
	      
	      conf.set("fs.defaultFS", "hdfs://"+namenodeIP+":9820");
	      conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
	      
	      conf.set("dfs.client.use.datanode.hostname", "true");
	      
	      // server principal
	      // the kerberos principle that the namenode is using
	      conf.set("dfs.namenode.kerberos.principal", "nn/pegacorn-fhirplace-namenode-0."+fqdn+"@"+realm);
	
	      UserGroupInformation.setConfiguration(conf);
	      LOG.info("Security enabled:=:=> " + UserGroupInformation.isSecurityEnabled());
	      UserGroupInformation.loginUserFromKeytab(loginUser+"@"+realm, keyTabPath+"/hbase-krb5.keytab");
	      LOG.info("Logged in user:=:=> " + UserGroupInformation.getLoginUser());
	      
	      FileSystem fileSystem = FileSystem.get(conf);
	      // Create a path
	      String fileName = "mock-data.json";
	      Path hdfsWritePath = new Path("/data/pegacorn/sample-dataset/" + fileName);
	      FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);
	      // Set replication
	      fileSystem.setReplication(hdfsWritePath, (short) 1);

	      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
	      bufferedWriter.write(jsonMessage.toString());
	      bufferedWriter.newLine();
	      bufferedWriter.close();
	      fileSystem.close();
          }
  }