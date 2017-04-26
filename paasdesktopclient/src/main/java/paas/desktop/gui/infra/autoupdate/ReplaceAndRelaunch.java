package paas.desktop.gui.infra.autoupdate;

import paas.desktop.gui.infra.JavaLauncher;
import paas.desktop.gui.infra.MyJar;
import swingutils.splash.ImageSplash;
import swingutils.splash.Splash;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static paas.desktop.gui.infra.autoupdate.Autoupdate.NEW_VERSION_FILENAME_SUFFIX;

public class ReplaceAndRelaunch {

    public static void replaceAndRelaunch() {
        Splash splash = new ImageSplash(new ImageIcon(ReplaceAndRelaunch.class.getResource("/splash.png")), null);
        try {
            splash.show();
            Path myJarPath = Paths.get(MyJar.getAbsolutePath());
            String filename = myJarPath.getFileName().toString();
            if (filename.endsWith(NEW_VERSION_FILENAME_SUFFIX)) {
                Path target = Paths.get(myJarPath.getParent().toString(), filename.substring(0, filename.length() - NEW_VERSION_FILENAME_SUFFIX.length()));
                splash.setProgressText("Replacing " + target);
                Files.deleteIfExists(target);
                Files.copy(myJarPath, target);
                splash.setProgressText("Restarting");
                JavaLauncher.spawn(target.toFile(), Autoupdate.AUTOUPDATE_CLEANUP);
            } else {
                JOptionPane.showMessageDialog(null, myJarPath + " is not a " + NEW_VERSION_FILENAME_SUFFIX + " file, aborting");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Replacing the version failed with " + e.getClass() + " : " + e.getMessage());
        } finally {
            splash.close();
        }
    }
}
