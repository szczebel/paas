package paas.rest.persistence.entities;

import paas.dto.HostedAppDesc;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class HostedAppDescriptor {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String jarFileName;

    @NotNull
    private String commandLineArgs;

    @Embedded
    private RequestedProvisions requestedProvisions;

    public HostedAppDescriptor(String jarFileName, String commandLineArgs, RequestedProvisions requestedProvisions) {
        this.jarFileName = jarFileName;
        this.commandLineArgs = commandLineArgs;
        this.requestedProvisions = requestedProvisions;
    }

    protected HostedAppDescriptor() {
    }

    public Long getId() {
        return id;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public String getCommandLineArgs() {
        return commandLineArgs;
    }

    public RequestedProvisions getRequestedProvisions() {
        return requestedProvisions;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public void setCommandLineArgs(String commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public void setRequestedProvisions(RequestedProvisions requestedProvisions) {
        this.requestedProvisions = requestedProvisions;
    }

    @Override
    public String toString() {
        return "HostedAppDescriptor{" +
                "id=" + id +
                ", jarFileName='" + jarFileName + '\'' +
                ", commandLineArgs='" + commandLineArgs + '\'' +
                '}';
    }

    public HostedAppDesc toDto() {
        return new HostedAppDesc(id, jarFileName, commandLineArgs, requestedProvisions.toDto());
    }
}
