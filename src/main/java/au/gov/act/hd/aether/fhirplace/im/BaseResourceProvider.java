package au.gov.act.hd.aether.fhirplace.im;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public abstract class BaseResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BaseResourceProvider.class);

    HBaseAdmin hba = null;
    
   
    
    protected HBaseAdmin getConfiguration() throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        if(hba == null) {
            Configuration config = HBaseConfiguration.create();

            String path = this.getClass()
              .getClassLoader()
              .getResource("hbase-site.xml")
              .getPath();
            config.addResource(new Path(path));
           
            HBaseAdmin.available(config);
//            ClusterConnection cc = new 
//            hba = new HBaseAdmin(cc);
        }
        return hba;
    }
    
    protected abstract void saveToDatabase();
    
    
    protected void writeToFileSystem(String fileName, String json) throws IOException {
        Configuration configuration = new Configuration();
        String clusterIP = (System.getenv("CLUSTER_IP"));
      configuration.set("fs.defaultFS", "hdfs://"+clusterIP+":8020");
      FileSystem fileSystem = FileSystem.get(configuration);
      Path hdfsWritePath = new Path("/data/pegacorn/sample-dataset/" + fileName + ".json");
      
        FSDataOutputStream fsDataOutputStream = fileSystem.create(hdfsWritePath,true);
        
        // Set replication
        fileSystem.setReplication(hdfsWritePath, (short) 2);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream,StandardCharsets.UTF_8));
        bufferedWriter.write(json.toString());
        bufferedWriter.newLine();
        bufferedWriter.close();
        fileSystem.close();
    }
    
    protected abstract String generateName();
    
    protected String parseResourceToJsonString(IDomainResource resource) {
        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();
        String parsedResource = parser.encodeResourceToString(resource);
        
        return parsedResource;
    }
}
