package au.gov.act.hd.aether.fhirplace.im;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.IdType;
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
public class AuditEventResourceProvider extends BaseResourceProvider implements IResourceProvider {
    private static final String ROW_PREFIX = "AuditEvent-";
    private static final Logger LOG = LoggerFactory.getLogger(AuditEventResourceProvider.class);
    private int myNextId = 1;

    private static final TableName TABLE_NAME = TableName.valueOf("AUDIT_EVENT");
    private static final byte[] CF_NAME = Bytes.toBytes("d");
    private static final byte[] QUALIFIER = Bytes.toBytes("q");


    /**
     * Constructor
     */
    public AuditEventResourceProvider() {

    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AuditEvent.class;
    }

    @Read()
    public AuditEvent read(@IdParam IdType theId) {
//        AuditEvent retVal = myEvents.get(theId.getIdPart());
//        if (retVal == null) {
        byte[] b = Bytes.toBytes(ROW_PREFIX + theId.getValue());
        LOG.info("Searching for: " + b);
        throw new ResourceNotFoundException(theId);
//        }
//        return retVal;
    }

    @Create
    public MethodOutcome createEvent(@ResourceParam AuditEvent theEvent) {
        // Give the resource the next sequential ID
        int id = myNextId++;
        theEvent.setId(new IdType(id));

        LOG.info("AuditEvent registered: " + theEvent.fhirType());

        try {
            saveToDatabase(theEvent);
//           writeToFileSystem(fileName, parsedResource);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Inform the server of the ID for the newly stored resource
        return new MethodOutcome().setId(theEvent.getIdElement());
    }

    @Override
    protected String generateName() {
        return ROW_PREFIX + myNextId++;
    }

    @Override
    protected void saveToDatabase(IDomainResource resource) {
        // TODO Auto-generated method stub
        try {
           Connection connection = getConnection();
            createTable(connection.getAdmin());
            saveData(connection, resource);
        } catch (MasterNotRunningException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void saveData(Connection connection, IDomainResource resource) throws IOException {
        Table table = connection.getTable(TABLE_NAME);
        Put row = new Put(Bytes.toBytes(generateName()));
        row.addColumn(CF_NAME, QUALIFIER, 
                Bytes.toBytes(parseResourceToJsonString(resource)));
        table.put(row);
        LOG.info("Save successful. Id: " + row.getRow().toString());
        table.close();
    }

    private void createTable(Admin admin) throws IOException {
        if (!admin.tableExists(TABLE_NAME)) {

            TableDescriptor desc = TableDescriptorBuilder.newBuilder(TABLE_NAME)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.of(CF_NAME)).build();
            admin.createTable(desc);
        }
    }

}
