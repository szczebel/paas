package paas.rest.dto;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class HostedAppInfo {

    private long id;
    private String jarFile;
    private String commandLineArgs;
    private boolean running;
    private Date start;//ZoneDateTime doesn't get marshalled to json well by default (own converter needed and I'm lazy)

    public HostedAppInfo(long id, String jarFile, List<String> commandLineArgs, boolean running, ZonedDateTime start) {
        this.id = id;
        this.jarFile = jarFile;
        this.commandLineArgs = String.join(" ", commandLineArgs);
        this.running = running;
        this.start = Date.from(start.toInstant());
    }

    public HostedAppInfo() {
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getCommandLineArgs() {
        return commandLineArgs;
    }

    public void setCommandLineArgs(String commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }
}
