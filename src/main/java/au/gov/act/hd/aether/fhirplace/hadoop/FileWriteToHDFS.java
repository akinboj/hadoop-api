package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.json.JSONObject;
import org.apache.hadoop.security.UserGroupInformation;
import org.ietf.jgss.GSSException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileWriteToHDFS {

  public void readFileFromHDFS() throws IOException {
      Configuration configuration = new Configuration();
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":9820");
      FileSystem fileSystem = FileSystem.get(configuration);
      //Create a path
      String fileName = "senators.json";
      Path hdfsReadPath = new Path("/data/pegacorn/sample-dataset/" + fileName);
      //Init input stream
      FSDataInputStream inputStream = fileSystem.open(hdfsReadPath);
      //Classical input stream usage
      String out= IOUtils.toString(inputStream, "UTF-8");
      System.out.println(out);

      /*BufferedReader bufferedReader = new BufferedReader(
              new InputStreamReader(inputStream, StandardCharsets.UTF_8));

      String line = null;
      while ((line=bufferedReader.readLine())!=null){
          System.out.println(line);
      }*/

      inputStream.close();
      fileSystem.close();
  }

  public void writeFileToHDFS(JSONObject jsonMessage) throws IOException, GSSException {
      // set kerberos host and realm
      System.setProperty("java.security.krb5.realm", "PEGACORN-FHIRPLACE-NAMENODE.SITE-A");
      System.setProperty("java.security.krb5.kdc", "pegacorn-fhirplace-namenode.kerberos.com");

      Configuration configuration = new Configuration();
      configuration.set("hadoop.security.authentication", "kerberos");
      configuration.set("hadoop.security.authorization", "true");
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":9820");
      configuration.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
      // hack for running locally with fake DNS records
      // set this to true if overriding the host name in /etc/hosts
//      configuration.set("dfs.client.use.datanode.hostname", "true");
      // server principal
      // the kerberos principal that the namenode is using
      configuration.set("dfs.namenode.kerberos.principal", "jboss/admin@PEGACORN-FHIRPLACE-NAMENODE.SITE-A");
      
      UserGroupInformation.setConfiguration(configuration);
      String loginUser = (System.getenv("LOGIN_USER"));
      String keyTabPath = (System.getenv("KEYTAB_PATH"));
      UserGroupInformation.loginUserFromKeytab(loginUser, keyTabPath);
      
      configuration.set("hadoop.rpc.protection", "privacy");
      configuration.set("hadoop.security.auth_to_local", "DEFAULT");
      
      FileSystem fileSystem = FileSystem.get(configuration);
      // Create a path
      String fileName = "mock-data.json";
      Path hdfsWritePath = new Path("/data/pegacorn/sample-dataset/" + fileName);
      FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);
      
      // Set replication
      fileSystem.setReplication(hdfsWritePath, (short) 2);

      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
      bufferedWriter.write(jsonMessage.toString());
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileSystem.close();
  }

  public void appendToHDFSFile() throws IOException {
      Configuration configuration = new Configuration();
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":9820");
      FileSystem fileSystem = FileSystem.get(configuration);
      //Create a path
      String fileName = "senators.json";
      Path hdfsWritePath = new Path("/data/pegacorn/sample-dataset/" + fileName);
      FSDataOutputStream fsDataOutputStream = fileSystem.append(hdfsWritePath);

      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
      bufferedWriter.write("Java API to append data in HDFS file");
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileSystem.close();
  }

  public void createDirectory() throws IOException {
      Configuration configuration = new Configuration();
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":9820");
      FileSystem fileSystem = FileSystem.get(configuration);
      String directoryName = "pegacorn/sample-dataset";
      Path path = new Path(directoryName);
      fileSystem.mkdirs(path);
  }

  public void checkExists() throws IOException {
      Configuration configuration = new Configuration();
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":9820");
      FileSystem fileSystem = FileSystem.get(configuration);
      String directoryName = "pegacorn/sample-dataset";
      Path path = new Path(directoryName);
      if(fileSystem.exists(path)){
          System.out.println("File/Folder Exists : "+path.getName());
      }else{
          System.out.println("File/Folder does not Exists : "+path.getName());
      }
  }
}
