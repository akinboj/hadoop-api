package au.gov.act.hd.aether.fhirplace.im;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@ApplicationScoped
public class AuditEventResourceProvider extends BaseResourceProvider implements IResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AuditEventResourceProvider.class);

    private static final TableName TABLE_NAME = TableName.valueOf("AUDIT_EVENT");
    private static final byte[] CF1 = Bytes.toBytes("INFO");
    private static final byte[] CF2 = Bytes.toBytes("DATA");
    private static final byte[] Q_NAME = Bytes.toBytes("NAME");
    private static final byte[] Q_UPDATE = Bytes.toBytes("DATE");
    private static final byte[] Q_PSTART = Bytes.toBytes("START");
    private static final byte[] Q_PEND = Bytes.toBytes("END");
    private static final byte[] Q_TYPE = Bytes.toBytes("TYPE");
    private static final byte[] Q_PURPOSE = Bytes.toBytes("PURPOSE");
    private static final byte[] Q_BODY = Bytes.toBytes("BODY");

    private int nextId;

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
        LOG.info("Read generated: " + theId.getIdPart());
        if ("startBulk".equals(theId.getIdPart())) {
            try {
                startBulk();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            Connection connection = getConnection();
            Table table = connection.getTable(TABLE_NAME);
            Get g = new Get(Bytes.toBytes(theId.getIdPart()));
            Result result = table.get(g);
            if (result.isEmpty()) {
                throw new ResourceNotFoundException(theId);
            }
            LOG.info("Result not empty. Size: " + result.size());

            byte[] data = result.getValue(CF2, Q_BODY);
            String json = Bytes.toString(data);
            AuditEvent audit = (AuditEvent) parseResourceFromJsonString(json);

            return audit;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ResourceNotFoundException(theId);
        }

    }

    @Create
    public MethodOutcome createEvent(@ResourceParam AuditEvent theEvent) {
        theEvent.getIdElement().setId("Audit-" + nextId++);
        LOG.info("AuditEvent registered. ID: " + theEvent.getId());
        LOG.info("ID Base: " + theEvent.getIdBase() + ". IDElement ID: " + theEvent.getIdElement().getId() + ". IdElement Part: "
                + theEvent.getIdElement().getIdPart());

        try {
            saveToDatabase(theEvent);
            // writeToFileSystem(fileName, parsedResource);

        } catch (Exception e) {
            e.printStackTrace();
        }

//        }
        // Inform the server of the ID for the newly stored resource

        return new MethodOutcome().setId(theEvent.getIdElement());
    }

    @Update
    public MethodOutcome updateEvent(@ResourceParam AuditEvent theEvent) {
        LOG.debug(".updateEvent(): Entry, theEvent (AuditEvent) --> {}", theEvent);
        LOG.info("Update called. ID: " + theEvent.getIdElement().getIdPart());
        try {
            saveToDatabase(theEvent);
//           writeToFileSystem(fileName, parsedResource);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MethodOutcome().setId(theEvent.getIdElement());
    }

    public void startBulk() throws IOException {
        LOG.info("Commencing bulk transaction");
        Connection connection = getConnection();
        createTable(connection.getAdmin());

        long startTime = Calendar.getInstance().getTimeInMillis();
        long elapsed = startTime;
        int offset = new Random().nextInt(100000000);
        offset = offset - (offset % 1000);
        for (int i = offset; i < (offset + 100000); i++) {
            AuditEvent audit = generateAuditEvent("Audit-" + i);
            saveData(connection, audit);

            if (i % 1000 == 0) {
                LOG.info("Last 1000 records: " + (Calendar.getInstance().getTimeInMillis() - elapsed));
                elapsed = Calendar.getInstance().getTimeInMillis();
            }
        }
        LOG.info("Total time for 100000 records: " + (elapsed - startTime));
        LOG.info("Average time per record (ms): " + ((elapsed - startTime) / 100000));
    }

    private AuditEvent generateAuditEvent(String nextId) {
        AuditEvent audit = new AuditEvent();
        audit.getIdElement().setId(nextId);
        AuditEventAgentComponent agent = new AuditEventAgentComponent();
        agent.setName("Agent " + new Random().nextInt(100000));
        audit.addAgent(agent);
        audit.getMeta().setLastUpdated(Calendar.getInstance().getTime());
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, -1);
        audit.getPeriod().setStart(date.getTime());
        audit.getPeriod().setEnd(date.getTime());
        CodeableConcept purpose = new CodeableConcept();
        purpose.setText("Random text " + new Random().nextInt());
        audit.getPurposeOfEvent().add(purpose);
        return audit;
    }

    @Override
    protected void saveToDatabase(IDomainResource resource) {
        // TODO Auto-generated method stub
        try {
            Connection connection = getConnection();
            createTable(connection.getAdmin());
            saveData(connection, (AuditEvent) resource);
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

    private void saveData(Connection connection, AuditEvent resource) throws IOException {
        Table table = connection.getTable(TABLE_NAME);
//        LOG.info("Save data. ID: " + resource.getIdElement().getId());
        Put row = new Put(Bytes.toBytes(resource.getIdElement().getId()));

        addAgent(resource, row);
        addUpdateDate(resource, row);
        addPeriod(resource, row);
        addSource(resource, row);
        addPurposeOfEvent(resource, row);
        row.addColumn(CF2, Q_BODY, Bytes.toBytes(parseResourceToJsonString(resource)));
        table.put(row);
//        LOG.info("Save successful. Id: " + Bytes.toString(row.getRow()));
        table.close();
    }

    private void addAgent(AuditEvent resource, Put row) {
        if (resource.getAgent() != null) {
            for (AuditEventAgentComponent agent : resource.getAgent()) {
                // Store the first name found
                if (StringUtils.isNotBlank(agent.getName())) {
                    row.addColumn(CF1, Q_NAME, Bytes.toBytes(agent.getName()));
//                    LOG.info("Agent added: " + agent.getName());
                    break;
                }
            }
        }

    }

    private void addUpdateDate(AuditEvent resource, Put row) {
        if (resource.getMeta() != null && resource.getMeta().getLastUpdated() != null) {
            row.addColumn(CF1, Q_UPDATE, Bytes.toBytes(resource.getMeta().getLastUpdated().getTime()));
//           LOG.info("Update date added: " + resource.getMeta().getLastUpdated().toString());
        }
    }

    private void addPeriod(AuditEvent resource, Put row) {
        if (resource.getPeriod() != null) {
            if (resource.getPeriod().getStart() != null) {
                row.addColumn(CF1, Q_PSTART, Bytes.toBytes(resource.getPeriod().getStart().getTime()));
//               LOG.info("Pending start added: " + resource.getPeriod().getStart().toString());

            }
            if (resource.getPeriod().getEnd() != null) {
                row.addColumn(CF1, Q_PEND, Bytes.toBytes(resource.getPeriod().getEnd().getTime()));
//               LOG.info("Pending end added: " + resource.getPeriod().getEnd().toString());
            }
        }
    }

    private void addSource(AuditEvent resource, Put row) {
        if (resource.getSource() != null) {
            StringBuilder sb = new StringBuilder();
            // TODO is this correct?
            for (Coding type : resource.getSource().getType()) {
                sb.append(type.getCode());
                sb.append(',');
            }
            if (sb.length() > 0) {
                row.addColumn(CF1, Q_TYPE, Bytes.toBytes(sb.substring(0, sb.length() - 1)));
//               LOG.info("Source added: " + sb.substring(0, sb.length() -1));
            }

        }
    }

    private void addPurposeOfEvent(AuditEvent resource, Put row) {
        if (resource.getPurposeOfEvent() != null) {
            StringBuilder sb = new StringBuilder();
            for (CodeableConcept purpose : resource.getPurposeOfEvent()) {
                sb.append(purpose.getTextElement());
                sb.append(',');
            }
            if (sb.length() > 0) {
                row.addColumn(CF1, Q_PURPOSE, Bytes.toBytes(sb.substring(0, sb.length() - 1)));
//                LOG.info("Purpose added: " + sb.substring(0, sb.length() -1));
            }
        }
    }

    private void createTable(Admin admin) throws IOException {
        if (!admin.tableExists(TABLE_NAME)) {
            TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(TABLE_NAME);
            Collection<ColumnFamilyDescriptor> families = new ArrayList<ColumnFamilyDescriptor>();
            families.add(ColumnFamilyDescriptorBuilder.of(CF1));
            families.add(ColumnFamilyDescriptorBuilder.of(CF2));
            builder.setColumnFamilies(families);
            TableDescriptor desc = builder.build();
            admin.createTable(desc);
        }
    }

}
