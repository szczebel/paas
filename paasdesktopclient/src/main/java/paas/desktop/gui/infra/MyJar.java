package paas.desktop.gui.infra;

import java.net.URL;

public class MyJar {

    public static String getAbsolutePath() {
        URL location = MyJar.class.getProtectionDomain().getCodeSource().getLocation();
        String locationString = location.toString();
        return locationString.substring("jar:file:".length()+1, locationString.lastIndexOf("!/BOOT-INF"));
    }
}
