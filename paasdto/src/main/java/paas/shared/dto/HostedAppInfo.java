package paas.shared.dto;

import java.util.Date;

public class HostedAppInfo {

    private HostedAppDesc hostedAppDesc;
    private HostedAppStatus hostedAppStatus;

    public HostedAppInfo(HostedAppDesc hostedAppDesc, HostedAppStatus hostedAppStatus) {
        this.hostedAppDesc = hostedAppDesc;
        this.hostedAppStatus = hostedAppStatus;
    }

    public HostedAppInfo() {
    }

    public HostedAppDesc getHostedAppDesc() {
        return hostedAppDesc;
    }

    public void setHostedAppDesc(HostedAppDesc hostedAppDesc) {
        this.hostedAppDesc = hostedAppDesc;
    }

    public HostedAppStatus getHostedAppStatus() {
        return hostedAppStatus;
    }

    public void setHostedAppStatus(HostedAppStatus hostedAppStatus) {
        this.hostedAppStatus = hostedAppStatus;
    }

    //convenience methods
    public static long getId(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppDesc().getId();
    }

    public static String getJarFile(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppDesc().getJarFile();
    }

    public static String getOwner(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppDesc().getOwner();
    }

    public static String getCommandLineArgs(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppDesc().getCommandLineArgs();
    }

    public static boolean isRunning(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppStatus().isRunning();
    }

    public static Date getStarted(HostedAppInfo hostedAppInfo) {
        return hostedAppInfo.getHostedAppStatus().getStarted();
    }
}
