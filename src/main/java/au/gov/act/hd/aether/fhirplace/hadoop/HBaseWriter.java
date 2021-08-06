package au.gov.act.hd.aether.fhirplace.hadoop;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.hbase.util.Bytes;

@ApplicationScoped
public class HBaseWriter {
    private static final Logger LOG = LoggerFactory.getLogger(HBaseWriter.class);
    private static final TableName TABLE_NAME = TableName.valueOf("emp");
    private static final byte[] CF_NAME = Bytes.toBytes("personal");
    private static final byte[] QUALIFIER = Bytes.toBytes("professional");
    private static final byte[] ROW_ID = Bytes.toBytes("row01");
	
	public void write() throws IOException {
		Configuration config = getConfiguration();
		
		try {
			HBaseAdmin.available(config);
			LOG.info("Yemi. Yes, HBase is available.");
		} catch (Exception e) {
			LOG.info("Yemi. Oops, HBase is not available.", e);
		}
	}

	private Configuration getConfiguration() throws IOException {
		LOG.info("Yemi. In getCOnfiguration");
		
		Configuration config = HBaseConfiguration.create();

		String hbaseIP = (System.getenv("HBASE_IP"));
		String hbasePort = (System.getenv("HBASE_PORT"));
		config.set("hbase.zookeeper.quorum", hbaseIP);
		config.set("hbase.zookeeper.property.clientPort", hbasePort);

		Connection connection = ConnectionFactory.createConnection(config);
		Admin admin = connection.getAdmin();

		createTable(admin);
		
        try(Table table = connection.getTable(TABLE_NAME)) {
            putRow(table);
        }
        System.out.println(" Table created ");
		return config;
	}
	
	public void createTable(final Admin admin) throws IOException {
        if(!admin.tableExists(TABLE_NAME)) {
            TableDescriptor desc = TableDescriptorBuilder.newBuilder(TABLE_NAME)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.of(CF_NAME))
                    .build();
            admin.createTable(desc);
        }
    }
	
    public void putRow(final Table table) throws IOException {
        table.put(new Put(ROW_ID).addColumn(CF_NAME, QUALIFIER, Bytes.toBytes("Hello, World!")));
    }

}
