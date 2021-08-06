package au.gov.act.hd.aether.fhirplace.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;

public class HelloWorld {

    public static void main(String[] args) {
        HelloWorld hw = new HelloWorld();
        
        Configuration config = hw.getConfiguration();
        try {
            HBaseAdmin.available(config);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    private Configuration getConfiguration() {
        Configuration config = HBaseConfiguration.create();

        String path = this.getClass()
          .getClassLoader()
          .getResource("hbase-site.xml")
          .getPath();
        config.addResource(new Path(path));
        return config;
    }
}
