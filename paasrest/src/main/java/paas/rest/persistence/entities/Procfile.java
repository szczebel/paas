package paas.rest.persistence.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Procfile {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String jarFileName;

    @NotNull
    private String commandLineArgs;

    public Procfile(String jarFileName, String commandLineArgs) {
        this.jarFileName = jarFileName;
        this.commandLineArgs = commandLineArgs;
    }

    protected Procfile() {
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

    public void setCommandLineArgs(String commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    @Override
    public String toString() {
        return "Procfile{" +
                "id=" + id +
                ", jarFileName='" + jarFileName + '\'' +
                ", commandLineArgs='" + commandLineArgs + '\'' +
                '}';
    }
}
