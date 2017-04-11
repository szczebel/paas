package paas.procman;

import paas.dto.HostedAppStatus;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.io.File.separator;
import static java.util.Arrays.asList;

public class JavaProcess {

    private static final String JAVA_BIN = String.join(separator, System.getProperty("java.home"), "bin", "java");

    private final long appId;
    private final File jarFile;
    private final File workingDirectory;
    private final OutputBuffer outputBuffer = new OutputBuffer(300);
    private final AsyncOutputReader outputReader;
    private final List<String> commandLineArgs;

    private Process process;
    private ZonedDateTime start;

    public JavaProcess(long appId, File jarFile, File workingDirectory, List<String> additionalArgs, BiConsumer<Long, String> processOutputConsumer) {
        this.appId = appId;
        this.jarFile = jarFile;
        this.commandLineArgs = additionalArgs;
        this.workingDirectory = workingDirectory;

        outputReader = new AsyncOutputReader(new CompositeOutputDrain(asList(outputBuffer, forwardTo(processOutputConsumer))));
    }

    private Consumer<String> forwardTo(BiConsumer<Long, String> processOutputConsumer) {
        return outputLine -> processOutputConsumer.accept(appId, outputLine);
    }


    private Process spawn() throws IOException, InterruptedException {
        this.start = ZonedDateTime.now();
        List<String> commands = new ArrayList<>(asList(JAVA_BIN, "-jar", jarFile.getAbsolutePath()));
        commands.addAll(commandLineArgs);
        Process p = new ProcessBuilder()
                .command(commands)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start();
        outputReader.asyncCollect(
                "Spawning process " + String.join(" ", commands),
                p.getInputStream()
        );
        return p;
    }


    public void start() throws IOException, InterruptedException {
        if (isRunning()) throw new IllegalStateException("Cannot start, it is already running");
        process = spawn();
    }

    public void stop() throws InterruptedException {
        if (isRunning()) {
            process.destroyForcibly();
            process.waitFor();
        }
        process = null;
    }

    public List<DatedMessage> tailSysout(long timestamp) {
        return outputBuffer.getOutputNewerThan(timestamp);
    }

    public long getAppId() {
        return appId;
    }

    File getJarFile() {
        return jarFile;
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    HostedAppStatus getStatus() {
        return new HostedAppStatus(isRunning(), start!=null ? Date.from(start.toInstant()) : null);
    }
}
