package paas.shared.dto;

public class HostedAppDesc {

    private long id;
    private String owner;
    private String jarFile;
    private String commandLineArgs;
    private HostedAppRequestedProvisions requestedProvisions;

    public HostedAppDesc(long id, String owner, String jarFile, String commandLineArgs, HostedAppRequestedProvisions requestedProvisions) {
        this.id = id;
        this.owner = owner;
        this.jarFile = jarFile;
        this.commandLineArgs = commandLineArgs;
        this.requestedProvisions = requestedProvisions;
    }

    public HostedAppDesc() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getCommandLineArgs() {
        return commandLineArgs;
    }

    public void setCommandLineArgs(String commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public HostedAppRequestedProvisions getRequestedProvisions() {
        return requestedProvisions;
    }

    public void setRequestedProvisions(HostedAppRequestedProvisions requestedProvisions) {
        this.requestedProvisions = requestedProvisions;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
