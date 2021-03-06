package paas.shared.dto;

public class HostedAppRequestedProvisions {

    private boolean wantsDB;
    private boolean wantsFileStorage;
    private boolean wantsLogstash;
    private boolean wantsLogging;
    private boolean wantsMonitoring;

    public HostedAppRequestedProvisions(boolean wantsDB, boolean wantsFileStorage, boolean wantsLogstash, boolean wantsLogging, boolean wantsMonitoring) {
        this.wantsDB = wantsDB;
        this.wantsFileStorage = wantsFileStorage;
        this.wantsLogstash = wantsLogstash;
        this.wantsLogging = wantsLogging;
        this.wantsMonitoring = wantsMonitoring;
    }

    public HostedAppRequestedProvisions() {
    }

    public boolean isWantsDB() {
        return wantsDB;
    }

    public void setWantsDB(boolean wantsDB) {
        this.wantsDB = wantsDB;
    }

    public boolean isWantsFileStorage() {
        return wantsFileStorage;
    }

    public void setWantsFileStorage(boolean wantsFileStorage) {
        this.wantsFileStorage = wantsFileStorage;
    }

    public boolean isWantsLogstash() {
        return wantsLogstash;
    }

    public void setWantsLogstash(boolean wantsLogstash) {
        this.wantsLogstash = wantsLogstash;
    }

    public boolean isWantsLogging() {
        return wantsLogging;
    }

    public void setWantsLogging(boolean wantsLogging) {
        this.wantsLogging = wantsLogging;
    }

    public boolean isWantsMonitoring() {
        return wantsMonitoring;
    }

    public void setWantsMonitoring(boolean wantsMonitoring) {
        this.wantsMonitoring = wantsMonitoring;
    }
}
