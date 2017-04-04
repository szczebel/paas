package paas.procman;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HostedAppManager {

    private final AtomicInteger sequence = new AtomicInteger();
    private final File workingDirectory;
    private List<HostedApp> hostedApps = new ArrayList<>();

    public HostedAppManager(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Collection<HostedApp> getApps() {
        return Collections.unmodifiableCollection(hostedApps);
    }

    public HostedApp getApp(int id) {
        return hostedApps.stream().filter(ha -> id==ha.getId()).findAny().orElseThrow(() -> new IllegalArgumentException("Unknown appId:"+id));
    }

    public HostedApp add(File jarFile, String commandLineArgs) {
        if(!jarFile.exists()) throw new IllegalArgumentException(jarFile.getAbsolutePath() + " does not exist!");
        int id = sequence.incrementAndGet();
        HostedApp hostedApp = new HostedApp(id, jarFile, commandLineArgs, workingDirectory);
        hostedApps.add(hostedApp);
        return hostedApp;
    }

    public Optional<HostedApp> findByJarName(String jarFileName) {
        return hostedApps.stream().filter(ha -> jarFileName.equals(ha.getJarFile().getName())).findAny();
    }
}
