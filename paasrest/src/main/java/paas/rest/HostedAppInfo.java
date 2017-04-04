package paas.rest;

public class HostedAppInfo {

    //todo: startTime, uptime?
    private int id;
    private String jarFile;
    private String commandLineArgs;
    private boolean running;

    HostedAppInfo(int id, String jarFile, String commandLineArgs, boolean running) {
        this.id = id;
        this.jarFile = jarFile;
        this.commandLineArgs = commandLineArgs;
        this.running = running;
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
}
