package paas.procman;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;
import static java.util.Arrays.asList;

public class HostedApp {

    private static final String JAVA_BIN = String.join(separator, System.getProperty("java.home"), "bin", "java");

    private final int id;
    private final File jarFile;
    private final String commandLineArgs;
    private final File workingDirectory;
    private Process process;
    private ZonedDateTime start;
    private AsyncOutputCollector outputCollector = new AsyncOutputCollector(300);

    public HostedApp(int id, File jarFile, String commandLineArgs, File workingDirectory) {
        this.id = id;
        this.jarFile = jarFile;
        this.commandLineArgs = commandLineArgs;
        this.workingDirectory = workingDirectory;
    }


    private Process spawn() throws IOException, InterruptedException {
        this.start = ZonedDateTime.now();
        List<String> commands = new ArrayList<>(asList(JAVA_BIN, "-jar", jarFile.getAbsolutePath()));
        if (commandLineArgs != null) {
            List<String> additionalArgs = asList(commandLineArgs.split(" "));
            if (!additionalArgs.isEmpty()) commands.addAll(additionalArgs);
        }
        Process p = new ProcessBuilder()
                .command(commands)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start();
        outputCollector.asyncCollect(
                String.join(" ", commands),
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
        return outputCollector.getOutputNewerThan(timestamp);
    }

    public int getId() {
        return id;
    }

    public File getJarFile() {
        return jarFile;
    }

    public String getCommandLineArgs() {
        return commandLineArgs;
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    public ZonedDateTime getStart() {
        return start;
    }
}
