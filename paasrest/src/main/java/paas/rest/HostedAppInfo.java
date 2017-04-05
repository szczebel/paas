package paas.rest;

import java.util.Date;

public class HostedAppInfo {

    private int id;
    private String jarFile;
    private String commandLineArgs;
    private boolean running;
    private Date start;//ZoneDateTime doesn't get marshalled to json well by default (own converter needed and I'm lazy)

    HostedAppInfo(int id, String jarFile, String commandLineArgs, boolean running,  Date start) {
        this.id = id;
        this.jarFile = jarFile;
        this.commandLineArgs = commandLineArgs;
        this.running = running;
        this.start = start;
    }

    public HostedAppInfo() {
    }

    public int getId() {
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
