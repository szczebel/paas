package paas.desktop;

import java.util.Date;

//todo: copied from server, remove duplication
public class HostedAppInfo {


    private int id;
    private String jarFile;
    private String commandLineArgs;
    private boolean running;
    private Date start;

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

    @Override
    public String toString() {
        return "HostedAppInfo{" +
                "id=" + id +
                ", jarFile='" + jarFile + '\'' +
                ", commandLineArgs='" + commandLineArgs + '\'' +
                ", running=" + running +
                ", start=" + start +
                '}';
    }
}
