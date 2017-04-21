package paas.rest.monitoring;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class MonitoringRecord {

    private final String method;

    private long successfulCounter;
    private long failureCounter;
    private long totalExecutionTime;

    MonitoringRecord(String method) {
        this.method = method;
    }

    synchronized void accumulate(long executionTime, boolean exception) {
        if (exception) failureCounter++;
        else successfulCounter++;
        totalExecutionTime += executionTime;
    }

    public String getMethod() {
        return method;
    }

    @ManagedAttribute
    public long getSuccessfulCounter() {
        return successfulCounter;
    }

    @ManagedAttribute
    public long getFailureCounter() {
        return failureCounter;
    }

    @ManagedAttribute
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    @ManagedAttribute
    public Long getAverageExecutionTime() {
        return totalExecutionTime / (successfulCounter + failureCounter);
    }
}
