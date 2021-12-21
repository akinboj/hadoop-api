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
import org.apache.hadoop.security.UserGroupInformation;
import org.ietf.jgss.GSSException;
import org.json.JSONObject;

public class FileWriteToHDFS {
	public void writeFileToHDFS(JSONObject jsonMessage) throws IOException, GSSException, LoginException, InterruptedException {
	      String namenodeIP = (System.getenv("NAMENODE_IP"));
	      String realm = "PEGACORN-FHIRPLACE-AUDIT.LOCAL";
	      String loginUser = (System.getenv("LOGIN_USER"));
	      String keyTabPath = (System.getenv("KEYTAB_PATH"));
	      String fqdn = "pegacorn-fhirplace-namenode.site-a.svc.cluster.local";
	      
	      System.setProperty("sun.security.krb5.debug", "true");
	      System.setProperty("javax.security.auth.useSubjectCredsOnly", "true");
	      System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
	      System.setProperty("java.security.auth.login.config", "/etc/jaas.conf");

	      Configuration conf = new Configuration();
	      conf.set("hadoop.security.authentication", "kerberos");      
	      conf.set("fs.defaultFS", "hdfs://"+namenodeIP+":8020");
	      conf.set("hadoop.rpc.protection", "privacy");
	      conf.set("dfs.namenode.kerberos.principal", "nn/pegacorn-fhirplace-namenode-0."+fqdn+"@"+realm);
	      
	      UserGroupInformation.setConfiguration(conf);
	      UserGroupInformation.loginUserFromKeytab(loginUser+"@"+realm, keyTabPath+"/hbase-krb5.keytab");
	      
	      FileSystem fileSystem = FileSystem.get(conf);
	      // Create a path
	      String fileName = "logs.json";
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