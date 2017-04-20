package paas.shared;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.jar.Manifest;

public class BuildTime {

    public static long readBuildTime(Manifest manifest) {
        String value = manifest.getMainAttributes().getValue("Implementation-Version");
        if(value == null) {
            throw new IllegalArgumentException("No Implementation-Version in manifest");
        }
        return parseBuildTime(value);
    }

    public static long parseBuildTime(String value) {
        return DateTimeFormatter.ISO_INSTANT.parse(value).getLong(ChronoField.INSTANT_SECONDS);
    }
}
