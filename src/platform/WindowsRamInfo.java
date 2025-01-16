// platform/WindowsRamInfo.java

package platform;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class WindowsRamInfo {
    public static String getRamInfo() {
        OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Total physical memory in MB
        long totalPhysicalMemory = (long) Math.ceil(osMXBean.getTotalMemorySize() / 1024 / 1024 / 1024);

        return "RAM : " + totalPhysicalMemory + " GB\n";
    }
}
