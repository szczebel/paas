package paas.rest.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.stereotype.Component;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

@Component
public class Monitor {

    private Map<String, MonitoringRecord> records = new HashMap<>();

    //less strict synchronization would be better
    synchronized void record(String method, long executionTime, boolean exception) {
        MonitoringRecord record = records.computeIfAbsent(method, this::newRecord);
        record.accumulate(executionTime, exception);
    }

    @Autowired MBeanExporter mBeanExporter;

    private MonitoringRecord newRecord(String method) {
        MonitoringRecord newRecord = new MonitoringRecord(method);
        try {
            mBeanExporter.registerManagedResource(newRecord,
                    new ObjectName(getClass().getPackage().getName(), "endpoint", method));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        return newRecord;
    }
}
