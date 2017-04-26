package paas.shared;

public class Links {
    public static final String REGISTER = "/unrestricted/register";
    public static final String DESKTOP_CLIENT_BUILD_TIMESTAMP = "/unrestricted/desktopClientLastModified";
    public static final String PAAS_DESKTOP_CLIENT_JAR = "/unrestricted/PaaSDesktopClient.jar";
    public static final String KIBANA = "/unrestricted/kibana/{appId}";
    public static final String MONITOR = "/unrestricted/monitor/{appId}";

    public static final String ADMIN_EXECUTE_SHELL_COMMAND = "/admin/executeShellCommand";
    public static final String ADMIN_GET_SHELL_OUTPUT = "/admin/getShellOutput";
    public static final String ADMIN_UPLOAD_DESKTOP_CLIENT = "/admin/uploadDesktopClient";

    public static final String APPLICATIONS = "/hosting/applications";
    public static final String DEPLOY = "/hosting/deploy";
    public static final String REDEPLOY = "/hosting/redeploy";
    public static final String UNDEPLOY = "/hosting/undeploy";
    public static final String RESTART = "/hosting/restart";
    public static final String STOP = "/hosting/stop";
    public static final String TAIL_SYSOUT = "/hosting/tailSysout";

    public static final String WHOAMI = "/whoami";

    public static String substitute(String template, Object... actuals){
        String retval = template;
        for (Object actual : actuals) {
            int start = retval.indexOf('{');
            if(start == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing {");
            int end = retval.indexOf('}');
            if(end == -1) throw new IllegalArgumentException("No place to insert " + actual + " missing }");
            if(end<start) throw new IllegalArgumentException("No place to insert " + actual + ", }...{");
            retval = retval.substring(0, start) + actual + retval.substring(end + 1);
        }

        int start = retval.indexOf('{');
        int end = retval.indexOf("}");
        if(start!=-1 && end>start) throw new IllegalArgumentException("Not enough actuals");

        return retval;
    }
}
