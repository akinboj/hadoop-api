package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ietf.jgss.GSSException;
import java.io.*;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.login.LoginException;

public class FileWriteToHDFS {
	private static final Logger LOG = LoggerFactory.getLogger(FileWriteToHDFS.class);

  public void writeFileToHDFS() throws IOException, GSSException, LoginException, InterruptedException {
		  // set kerberos host and realm
		  String realm = "PEGACORN-FHIRPLACE-NAMENODE.SITE-A";
	      String kdcServer = (System.getenv("KDC_SERVER"));
	      String namenodeIP = (System.getenv("NAMENODE_IP"));
	      String loginUser = (System.getenv("LOGIN_USER"));
	      String keyTabPath = (System.getenv("KEYTAB_PATH"));
	      
	      System.setProperty("java.security.krb5.realm", realm);
	      System.setProperty("java.security.krb5.kdc", kdcServer+":88");
	      System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
	      System.setProperty("java.security.auth.login.config", "/etc/jaas.conf");
	
	      Configuration conf = new Configuration();
	      conf.set("fs.defaultFS", "hdfs://"+namenodeIP+":9820");
	      conf.set("hadoop.security.authentication", "kerberos");
	      conf.set("hadoop.rpc.protection", "privacy");
	      conf.set("hadoop.security.auth_to_local", "DEFAULT");
	      conf.set("dfs.client.use.datanode.hostname", "false");
	      conf.set("dfs.namenode.kerberos.principal", "root/"+kdcServer+"@"+realm);
	
	      UserGroupInformation.setConfiguration(conf);
	      LOG.info("Security enabled " + UserGroupInformation.isSecurityEnabled());
	      UserGroupInformation.loginUserFromKeytab(loginUser, keyTabPath);
	      UserGroupInformation ugi = UserGroupInformation.getLoginUser();
	      FileSystem fileSystem = ugi.doAs(new PrivilegedExceptionAction<FileSystem>() {
	    	  public FileSystem run() throws Exception {
	                 return  FileSystem.get(conf);
	             }
	         });
	      String fileName = "mock-data.txt";
	      FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path("/data/pegacorn/sample-dataset/" + fileName));
	      fsDataOutputStream.write("This is pegacorn-fhirplace!!! testing this\n".getBytes());
	         fsDataOutputStream.close();
	         fileSystem.close();
          }
  }