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
    private final File out;
    private final File err;
    private final File workingDirectory;
    private Process process;
    private ZonedDateTime start;

    public HostedApp(int id, File jarFile, String commandLineArgs, File workingDirectory) {
        this.id = id;
        this.jarFile = jarFile;
        this.commandLineArgs = commandLineArgs;
        this.out = new File(workingDirectory, jarFile.getName() + ".out");
        this.err = new File(workingDirectory, jarFile.getName() + ".err");
        this.workingDirectory = workingDirectory;
    }


    private Process spawn() throws IOException, InterruptedException {
        this.start = ZonedDateTime.now();
        List<String> commands = new ArrayList<>(asList(JAVA_BIN, "-jar", jarFile.getAbsolutePath()));
        if (commandLineArgs != null) {
            List<String> additionalArgs = asList(commandLineArgs.split(" "));
            if (!additionalArgs.isEmpty()) commands.addAll(additionalArgs);
        }
        return new ProcessBuilder()
                .command(commands)
                .directory(workingDirectory)
                .redirectOutput(out)
                .redirectError(err)//todo consider in-memory err&out logs (see Shell)
                .start();
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

    //todo: replace 'limit' with timestamp (tail logs newer than timestamp) - ONLY if switched to in-memory
    public List<String> tailSysout(int limit) throws IOException {
        return Tail.tail(out, limit);
    }

    public List<String> tailSyserr(int limit) throws IOException {
        return Tail.tail(err, limit);
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
