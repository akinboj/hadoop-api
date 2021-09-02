package au.gov.act.hd.aether.fhirplace.im;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@ApplicationScoped
public class MediaResourceProvider extends BaseResourceProvider implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MediaResourceProvider.class);
    private int myNextId = 2;

    private static final TableName TABLE_NAME = TableName.valueOf("MEDIA");
    private static final byte[] CF1 = Bytes.toBytes("INFO");
    private static final byte[] CF2 = Bytes.toBytes("DATA");
    private static final byte[] CF3 = Bytes.toBytes("FILE");
    private static final byte[] Q_BODY = Bytes.toBytes("BODY"); // CF2
    private static final byte[] Q_TYPE = Bytes.toBytes("TYPE"); // CF3
    private static final byte[] Q_URL = Bytes.toBytes("URL"); // CF3
    private static final byte[] Q_FILE = Bytes.toBytes("FILE"); // CF3

    /**
     * Constructor
     */
    public MediaResourceProvider() {
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Media.class;
    }

    @Read()
    public Media read(@IdParam IdType theId) {
//       Media retVal = null;
//      if (retVal == null) {
        throw new ResourceNotFoundException(theId);
//      }
//      return retVal;
    }

    @Create
    public MethodOutcome createMedia(@ResourceParam Media theEvent) {
        // Give the resource the next sequential ID
        int id = myNextId++;
       theEvent.setId(new IdType(id));
       LOG.info("Media registered: " + theEvent.fhirType());
        try {
            saveToDatabase(theEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MethodOutcome().setId(theEvent.getIdElement());
    }

    @Override
    protected void saveToDatabase(IDomainResource resource) {
        try {
            Connection connection = getConnection();
            createTable(connection.getAdmin());
            saveData(connection, (Media) resource);
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void saveData(Connection connection, Media resource) throws IOException {
        Table table = connection.getTable(TABLE_NAME);
//        LOG.info("Save data. ID: " + resource.getIdElement().getId());
        Put row = processToPut(resource);
        table.put(row);
//        LOG.info("Save successful. Id: " + Bytes.toString(row.getRow()));
        table.close();
    }
    

    private Put processToPut(Media resource) {
        Put row = new Put(Bytes.toBytes(resource.getIdElement().getId()));
        //TODO add the CF1 fields
        addContent(resource.getContent(), row);
        row.addColumn(CF2, Q_BODY, Bytes.toBytes(parseResourceToJsonString(resource)));
        return row;
    }


    private void addContent(Attachment content, Put row) {
        if (content != null) {
            row.addColumn(CF3, Q_TYPE, Bytes.toBytes(content.getContentType()));
            if(StringUtils.isNotBlank(content.getUrl())) {
                row.addColumn(CF3, Q_URL, Bytes.toBytes(content.getUrl()));
            }
            if(content.getData() != null) {
                row.addColumn(CF3, Q_FILE, content.getData());
            }            
            
//           LOG.info("Update date added: " + resource.getMeta().getLastUpdated().toString());
        }
    }


    private void createTable(Admin admin) throws IOException {
        if (!admin.tableExists(TABLE_NAME)) {
            TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(TABLE_NAME);
            Collection<ColumnFamilyDescriptor> families = new ArrayList<ColumnFamilyDescriptor>();
            families.add(ColumnFamilyDescriptorBuilder.of(CF1));
            families.add(ColumnFamilyDescriptorBuilder.of(CF2));
            families.add(ColumnFamilyDescriptorBuilder.of(CF3));
            builder.setColumnFamilies(families);
            TableDescriptor desc = builder.build();
            admin.createTable(desc);
        }
    }
}
