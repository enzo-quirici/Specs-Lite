import platform.LinuxGpuInfo;
import platform.MacGpuInfo;
import platform.WindowsGpuInfo;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Upload {

    // Main method called by the menu
    public void uploadSpecs() {
        // Collect system information via Specs.java
        String osInfo = Specs.getOperatingSystem();
        String cpuInfo = Specs.getCpuInfo();
        String gpuInfo = Specs.getGpuInfo();
        String ramInfo = Specs.getRamInfo();

        // Parse and format the system information for the new format
        String os = parseOperatingSystem(osInfo);
        String version = parseVersion(osInfo);
        String cpu = parseCpuName(cpuInfo);
        String cores = parseCpuCores(cpuInfo);
        String threads = parseCpuThreads(cpuInfo);
        String gpu = parseGpuName(gpuInfo);
        String vram = getVram(); // Use the new getVram method
        String ram = parseRamTotal(ramInfo);

        // Create a JSON object with the cleaned system information
        String jsonData = String.format(
                "{\"os\": \"%s\", \"version\": \"%s\", \"cpu\": \"%s\", \"cores\": \"%s\", \"threads\": \"%s\", \"gpu\": \"%s\", \"vram\": \"%s\", \"ram\": \"%s\"}",
                os, version, cpu, cores, threads, gpu, vram, ram
        );

        // Use JFileChooser to allow the user to select a directory
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setDialogTitle("Select a Directory to Save the JSON File");
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = directoryChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            // Get the selected directory
            File selectedDirectory = directoryChooser.getSelectedFile();
            String filePath = selectedDirectory.getAbsolutePath() + File.separator + "Specs.json";

            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(jsonData);
                JOptionPane.showMessageDialog(null, "Data saved successfully to JSON file at " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to save data to file. Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operation canceled.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to get GPU VRAM
    private String getVram() {
        String osName = System.getProperty("os.name").toLowerCase();
        long gpuVram = 0;

        try {
            if (osName.contains("mac")) {
                gpuVram = MacGpuInfo.getGpuVram();
            } else if (osName.contains("linux")) {
                gpuVram = LinuxGpuInfo.getGpuVram();
            } else if (osName.contains("win")) {
                gpuVram = WindowsGpuInfo.getGpuVram();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return VRAM as a string; if GPU VRAM is 0, assume it's unavailable
        return gpuVram > 0 ? String.valueOf(gpuVram) : "N/A";
    }

    // Helper methods to parse the system information into the required format
    private String parseOperatingSystem(String osInfo) {
        return osInfo.split("\n")[0].split(":")[1].trim();
    }

    private String parseVersion(String osInfo) {
        return osInfo.split("\n")[1].split(":")[1].trim();
    }

    private String parseCpuName(String cpuInfo) {
        return cpuInfo.split("\n")[0].split(":")[1].trim();
    }

    private String parseCpuCores(String cpuInfo) {
        return cpuInfo.split("\n")[1].split(":")[1].trim();
    }

    private String parseCpuThreads(String cpuInfo) {
        return cpuInfo.split("\n")[2].split(":")[1].trim();
    }

    private String parseGpuName(String gpuInfo) {
        return gpuInfo.split("\n")[0].split(":")[1].trim();
    }

    private String parseRamTotal(String ramInfo) {
        return ramInfo.split("\n")[0].split(":")[1].trim().split(" ")[0];
    }
}
