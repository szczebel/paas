package paas.rest.service.provisioning;

import java.io.File;
import java.util.Collection;
import java.util.function.BiConsumer;

public class Provisions {

    private final File appWorkDir;
    private final Collection<String> additionalCommandLine;
    private final BiConsumer<Long, String> outputLogger;

    Provisions(File appWorkDir, Collection<String> additionalCommandLine, BiConsumer<Long, String> outputLogger) {
        this.appWorkDir = appWorkDir;
        this.additionalCommandLine = additionalCommandLine;
        this.outputLogger = outputLogger;
    }

    public File getAppWorkDir() {
        return appWorkDir;
    }

    public Collection<String> getAdditionalCommandLine() {
        return additionalCommandLine;
    }

    public BiConsumer<Long, String> getOutputLogger() {
        return outputLogger;
    }
}
