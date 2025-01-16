// platform/LinuxGpuInfo.java

package platform;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class LinuxGpuInfo {

    public static String getGpuName() {
        String gpuName = getGpuNameFromOshi();

        if (gpuName.equals("Unknown GPU")) {
            gpuName = getGpuNameFromGlxInfo();
            if (gpuName.equals("Unknown GPU")) {
                gpuName = getGpuNameFromLspci();
            }
        }

        return gpuName;
    }

    private static String getGpuNameFromOshi() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            List<GraphicsCard> graphicsCards = hal.getGraphicsCards();

            if (!graphicsCards.isEmpty()) {
                GraphicsCard gpu = graphicsCards.get(0);
                return removeUnwantedParenthesesContent(gpu.getName());
            }
        } catch (Exception e) {
        }
        return "Unknown GPU";
    }

    private static String getGpuNameFromGlxInfo() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "glxinfo | grep 'Device:'");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("device")) {
                    return removeUnwantedParenthesesContent(line.split(":")[1].trim());
                }
            }
        } catch (Exception e) {
        }
        return "Unknown GPU";
    }

    private static String getGpuNameFromLspci() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("lspci");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("vga")) {
                    return removeUnwantedParenthesesContent(line.split(":")[2].trim());
                }
            }
        } catch (Exception e) {
        }
        return "Unknown GPU";
    }

    public static long getGpuVram() {
        long vram = getVramFromOshi();

        if (vram == 0) {  //
            vram = getVramFromGlxInfo();
            if (vram == 0) {
                vram = getVramFromLspci();
            }
        }

        return vram;
    }

    // Méthode pour récupérer la VRAM avec OSHI
    private static long getVramFromOshi() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            List<GraphicsCard> graphicsCards = hal.getGraphicsCards();

            if (!graphicsCards.isEmpty()) {
                GraphicsCard gpu = graphicsCards.get(0);
                return gpu.getVRam() / (1024 * 1024 * 1024);  // Convertir de Mo à Go
            }
        } catch (Exception e) {
        }
        return 0;
    }

    // Méthode pour récupérer la VRAM via glxinfo
    private static long getVramFromGlxInfo() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("glxinfo");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("total available memory")) {
                    return Long.parseLong(line.replaceAll("[^0-9]", "").trim()) / 1024;  // Convertir de Mo à Go
                } else if (line.toLowerCase().contains("video memory")) {
                    return Long.parseLong(line.replaceAll("[^0-9]", "").trim()) / 1024;  // Convertir de Mo à Go
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    // Méthode pour récupérer la VRAM via lspci
    private static long getVramFromLspci() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("lspci", "-v");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean foundVGA = false;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("vga compatible controller")) {
                    foundVGA = true;
                }
                if (foundVGA && line.toLowerCase().contains("memory at")) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.matches("[0-9a-f]+K")) {
                            return Long.parseLong(part.replace("K", "").trim()) / 1024 / 1024;  // Convertir de Ko à Go
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private static String removeUnwantedParenthesesContent(String name) {
        return name.replaceAll("\\s*\\(.*?\\)", "");
    }
}
