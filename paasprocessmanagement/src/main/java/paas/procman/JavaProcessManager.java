package paas.procman;

import paas.shared.dto.HostedAppStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class JavaProcessManager {

    private List<JavaProcess> apps = new ArrayList<>();

    public Optional<HostedAppStatus> getStatus(long id) {
        return apps.stream().filter(ha -> id == ha.getAppId()).map(JavaProcess::getStatus).findAny();
    }

    public synchronized JavaProcess create(long id, File jarFile, File appWorkDir, List<String> commandLine, BiConsumer<Long, String> processOutputConsumer) {
        findById(id).ifPresent(p -> {throw new IllegalStateException("Process for appId:"+id+" already running");});
        if (!jarFile.exists()) throw new IllegalArgumentException(jarFile.getAbsolutePath() + " does not exist!");
        JavaProcess app = new JavaProcess(id, jarFile, appWorkDir, commandLine, processOutputConsumer);
        apps.add(app);
        return app;
    }

    public void stopAndRemoveIfExists(long appId) throws InterruptedException {
        stopAndRemoveIfExists(findById(appId));
    }

    private synchronized void stopAndRemoveIfExists(Optional<JavaProcess> existingApp) throws InterruptedException {
        if (existingApp.isPresent()) {
            JavaProcess javaProcess = existingApp.get();
            javaProcess.stop();
            apps.remove(javaProcess);
        }
    }

    public Optional<JavaProcess> findById(long appId) {
        return apps.stream().filter(ha -> appId == ha.getAppId()).findAny();
    }

    public void shutdown() {
        apps.forEach(JavaProcess::stop);
        apps = null;
    }
}
