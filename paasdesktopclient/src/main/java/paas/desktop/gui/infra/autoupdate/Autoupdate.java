package paas.desktop.gui.infra.autoupdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.MyJar;
import paas.desktop.gui.infra.events.EventBus;
import paas.desktop.gui.infra.events.Events;
import paas.desktop.gui.infra.security.LoginData;
import paas.desktop.remoting.PaasRestClient;
import paas.shared.Links;
import swingutils.background.BackgroundOperation;
import swingutils.background.Cancellable;
import swingutils.spring.edt.MustNotBeInEDT;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static paas.shared.BuildTime.parseBuildTime;

@Component
public class Autoupdate {

    public static final String NEW_VERSION_FILENAME_SUFFIX = ".newversion";
    public static final String AUTOUPDATE_CLEANUP = "autoupdate:cleanup";
    public static final String AUTOUPDATE_REPLACE = "autoupdate:replace";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventBus eventBus;
    @Autowired
    private PaasRestClient paasRestClient;
    @Autowired
    private LoginData loginData;

    private Long myBuildTimestamp;

    private Cancellable autoupdateInProgress = () -> {
    };

    @SuppressWarnings("unused")
    @PostConstruct
    void initialize() {
        this.myBuildTimestamp = getMyBuildTimestamp();
        logger.info("My build timestamp : " + myBuildTimestamp);
        eventBus.when(Events.LOGIN_CHANGED, this::downloadIfAvailable);
    }

    public void downloadIfAvailable() {
        autoupdateInProgress.cancel();
        autoupdateInProgress = BackgroundOperation.execute(
                this::checkAndDownload,
                this::downloadCompleted,
                this::autoupdateFailed
        );
    }

    private void downloadCompleted(Optional<File> newVersion) {
        if (newVersion.isPresent()) {
            eventBus.dispatch(Events.newVersionDownloaded(newVersion.get()));
        } else {
            logger.info("No new version detected");
        }
    }

    private void autoupdateFailed(Exception ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
            if (t instanceof DownloadFailed) {
                eventBus.dispatchEvent(Events.DOWNLOAD_FAILED);
            }
        }
        logger.error("Autoupdate failed due to : ", t);
    }

    private Optional<File> checkAndDownload() throws Exception {
        if (myBuildTimestamp == null) return Optional.empty();
        if (myBuildTimestamp >= paasRestClient.getDesktopClientBuildTime()) return Optional.empty();
        try {
            return Optional.of(download().toFile());
        } catch (Exception e) {
            logger.warn("Dowanload failed", e);
            throw new DownloadFailed();
        }
    }

    @MustNotBeInEDT
    private Path download() throws IOException {
        logger.info("Newer version on server, commencing download");
        Path target = Paths.get(MyJar.getAbsolutePath() + NEW_VERSION_FILENAME_SUFFIX);
        Files.copy(
                new URL(loginData.getServerUrl() + Links.PAAS_DESKTOP_CLIENT_JAR).openStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
        return target;
    }

    private Long getMyBuildTimestamp() {
        try {
            String implementationVersion = getClass().getPackage().getImplementationVersion();
            logger.info("Implementation version : {}", implementationVersion);
            return parseBuildTime(implementationVersion);
        } catch (Exception e) {
            logger.error("Checking my build timestamp failed : ", e);
            return null;
        }
    }

    private static class DownloadFailed extends Exception {
    }
}
