package paas.procman;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

public class JavaProcessManager {

    private List<JavaProcess> apps = new ArrayList<>();

    public Collection<JavaProcess> getApps() {
        return Collections.unmodifiableCollection(apps);
    }

    public JavaProcess getApp(long id) {
        return apps.stream().filter(ha -> id == ha.getAppId()).findAny().orElseThrow(() -> new IllegalArgumentException("Unknown appId:" + id));
    }

    public JavaProcess create(long id, File jarFile, File appWorkDir, List<String> commandLine, BiConsumer<Long, String> processOutputConsumer) {
        if (!jarFile.exists()) throw new IllegalArgumentException(jarFile.getAbsolutePath() + " does not exist!");
        JavaProcess app = new JavaProcess(id, jarFile, appWorkDir, commandLine, processOutputConsumer);
        apps.add(app);
        return app;
    }

    public void stopAndRemoveIfExists(String jarFileName) throws InterruptedException {
        stopAndRemoveIfExists(findByJarName(jarFileName));
    }

    public void stopAndRemoveIfExists(long appId) throws InterruptedException {
        stopAndRemoveIfExists(findById(appId));
    }

    private void stopAndRemoveIfExists(Optional<JavaProcess> existingApp) throws InterruptedException {
        if (existingApp.isPresent()) {
            JavaProcess javaProcess = existingApp.get();
            javaProcess.stop();
            apps.remove(javaProcess);
        }
    }

    private Optional<JavaProcess> findByJarName(String jarFileName) {
        return apps.stream().filter(ha -> jarFileName.equals(ha.getJarFile().getName())).findAny();
    }

    private Optional<JavaProcess> findById(long appId) {
        return apps.stream().filter(ha -> appId == ha.getAppId()).findAny();
    }

    public void shutdown() {
        for (JavaProcess app : apps) {
            try {
                app.stop();
            } catch (InterruptedException ignored) {
            }
        }
    }
}