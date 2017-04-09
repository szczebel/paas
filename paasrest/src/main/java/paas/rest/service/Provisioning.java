package paas.rest.service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

@Component
public class Provisioning {

    Collection<String> provision(File appWorkDir) {
        return Collections.singleton(
                "--storage.directory=\"" + appWorkDir.getAbsolutePath() + "\""
        );
    }
}
