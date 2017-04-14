package paas.rest.persistence.entities;

import paas.shared.dto.HostedAppDesc;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class HostedAppDescriptor {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @NotNull
    private String owner;

    @NotNull
    private String localJarName;

    @NotNull
    private String originalJarName;

    @NotNull
    private String commandLineArgs;

    @Embedded
    @NotNull
    private RequestedProvisions requestedProvisions;

    public HostedAppDescriptor(String owner, String localJarName, String originalJarName, String commandLineArgs, RequestedProvisions requestedProvisions) {
        this.owner = owner;
        this.localJarName = localJarName;
        this.originalJarName = originalJarName;
        this.commandLineArgs = commandLineArgs;
        this.requestedProvisions = requestedProvisions;
    }

    protected HostedAppDescriptor() {
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getLocalJarName() {
        return localJarName;
    }

    public String getOriginalJarName() {
        return originalJarName;
    }

    public String getCommandLineArgs() {
        return commandLineArgs;
    }

    public RequestedProvisions getRequestedProvisions() {
        return requestedProvisions;
    }

    public void setLocalJarName(String localJarName) {
        this.localJarName = localJarName;
    }

    public void setOriginalJarName(String originalJarName) {
        this.originalJarName = originalJarName;
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
                ", jarFileName='" + localJarName + '\'' +
                ", commandLineArgs='" + commandLineArgs + '\'' +
                '}';
    }

    public HostedAppDesc toDto() {
        return new HostedAppDesc(id, owner, originalJarName, commandLineArgs, requestedProvisions.toDto());
    }
}
