package au.gov.act.hd.aether.fhirplace.hadoop;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileWriteToHDFS {

  public void readFileFromHDFS() throws IOException {
      Configuration configuration = new Configuration();
      configuration.set("fs.defaultFS", "hdfs://pvmone:54310");
      FileSystem fileSystem = FileSystem.get(configuration);
      //Create a path
      String fileName = "senators.json";
      Path hdfsReadPath = new Path("/user/pegacorn/sample-dataset/" + fileName);
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

  public void writeFileToHDFS(JSONObject jsonMessage) throws IOException {
      Configuration configuration = new Configuration();
//      configuration.set("fs.defaultFS", "hdfs://10.152.183.63:8020");
      String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":8020");
      FileSystem fileSystem = FileSystem.get(configuration);
      //Create a path
      String fileName = "sample-data.json";
      Path hdfsWritePath = new Path("/user/pegacorn/sample-dataset/" + fileName);
      FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);

      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
      bufferedWriter.write(jsonMessage.toString());
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileSystem.close();
  }

  public void appendToHDFSFile() throws IOException {
      Configuration configuration = new Configuration();
      configuration.set("fs.defaultFS", "hdfs://pvmone:54310");
      FileSystem fileSystem = FileSystem.get(configuration);
      //Create a path
      String fileName = "senators.json";
      Path hdfsWritePath = new Path("/user/pegacorn/sample-dataset/" + fileName);
      FSDataOutputStream fsDataOutputStream = fileSystem.append(hdfsWritePath);

      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
      bufferedWriter.write("Java API to append data in HDFS file");
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileSystem.close();
  }

  public void createDirectory() throws IOException {
      Configuration configuration = new Configuration();
      configuration.set("fs.defaultFS", "hdfs://pvmone:54310");
      FileSystem fileSystem = FileSystem.get(configuration);
      String directoryName = "pegacorn/sample-dataset";
      Path path = new Path(directoryName);
      fileSystem.mkdirs(path);
  }

  public void checkExists() throws IOException {
      Configuration configuration = new Configuration();
      configuration.set("fs.defaultFS", "hdfs://pvmone:54310");
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
