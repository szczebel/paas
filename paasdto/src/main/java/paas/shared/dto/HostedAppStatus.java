package paas.shared.dto;

import java.util.Date;

@SuppressWarnings("unused")
public class HostedAppStatus {

    private boolean running;
    private Date started;//ZoneDateTime doesn't get marshalled to json well by default (own converter needed and I'm lazy)

    public HostedAppStatus(boolean running, Date started) {
        this.running = running;
        this.started = started;
    }

    public HostedAppStatus() {
    }

    boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }
}
