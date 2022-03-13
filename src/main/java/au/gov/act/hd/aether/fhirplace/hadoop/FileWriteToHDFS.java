// https://gist.github.com/dbathgate/87986ad5c659084dd0710f471de66e00
package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.ietf.jgss.GSSException;
import org.json.JSONObject;

public class FileWriteToHDFS {
	public void writeFileToHDFS(JSONObject jsonMessage) throws IOException, GSSException, LoginException, InterruptedException {
	      String realm = "PEGACORN-FHIRPLACE-AUDIT.LOCAL";
	      String loginUser = (System.getenv("LOGIN_USER"));
	      String keyTabPath = (System.getenv("KEYTAB_PATH"));
	      String kdcServer = (System.getenv("KDC_SERVER"));
	      String namenodeHost = (System.getenv("NAMENODE_HOST"));
	      String kerberosConfigFileLocation = "/etc/krb5.conf";
	      System.setProperty("sun.security.krb5.debug", "true");
	      System.setProperty("java.security.krb5.realm", realm);
	      System.setProperty("java.security.krb5.kdc", kdcServer);
	      System.setProperty("java.security.krb5.conf", kerberosConfigFileLocation);

	      Configuration conf = new Configuration();
	      conf.set("hadoop.security.authentication", "kerberos");
	      conf.set("hadoop.security.authorization", "true");
	      conf.set("hadoop.rpc.protection", "privacy");
	      conf.set("dfs.data.transfer.protection", "privacy");
	      conf.set("fs.defaultFS", "hdfs://"+namenodeHost+":8020");
	      conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
	      conf.set("dfs.client.use.datanode.hostname", "true");
	      conf.set("dfs.namenode.kerberos.principal", "nn/"+namenodeHost+"@"+realm);
	      
	      UserGroupInformation.setConfiguration(conf);
	      UserGroupInformation.loginUserFromKeytab(loginUser+"@"+realm, keyTabPath+"/hbase-krb5.keytab");
	      UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
		  
	      ugi.doAs(new PrivilegedExceptionAction<Object>() {
	    	  @Override
	    	  public Object run() throws IOException, InterruptedException, URISyntaxException { 
	      // Create a path
	      String fileName = "mock.json";
	      Path hdfsWritePath = new Path("/data/pegacorn/sample-dataset/" + fileName);
	      URI uri = new URI("hdfs:"+namenodeHost+":"+hdfsWritePath);
	      FileSystem fileSystem = FileSystem.get(new URI("hdfs:" + uri.getSchemeSpecificPart()), conf);
	      FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);
	      // Set replication
	      fileSystem.setReplication(hdfsWritePath, (short) 1);
	      
	      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
	      bufferedWriter.write(jsonMessage.toString());
	      bufferedWriter.newLine();
	      bufferedWriter.close();
	      fileSystem.close();
	      return bufferedWriter;
          }
	      });
	      
          }
  }