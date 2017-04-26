package paas.desktop.gui.infra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.io.File.separator;

public class JavaLauncher {

    private static final String JAVA_BIN = String.join(separator, System.getProperty("java.home"), "bin", "java");

    public static void spawn(File jar, String arg) throws IOException {
        spawn(JAVA_BIN, "-jar", jar.getAbsolutePath(), arg);
    }

    private static void spawn(String... commands) throws IOException {
        new ProcessBuilder()
                .command(commands)
                .directory(currentWorkingDir())
                .start();
    }

    public static File currentWorkingDir() {
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
}
