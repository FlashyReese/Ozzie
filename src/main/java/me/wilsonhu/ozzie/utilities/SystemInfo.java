package me.wilsonhu.ozzie.utilities;

import java.io.File;

public class SystemInfo {

    private Runtime runtime = Runtime.getRuntime();

    public String Info() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.OsInfo());
        sb.append(this.MemInfo());
        sb.append(this.DiskInfo());
        return sb.toString();
    }

    public String OSname() {
        return System.getProperty("os.name");
    }

    public String OSversion() {
        return System.getProperty("os.version");
    }

    public String OsArch() {
        return System.getProperty("os.arch");
    }

    public long totalMem() {
        return Runtime.getRuntime().totalMemory();
    }

    public long usedMem() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public String MemInfo() {
        //NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("Free memory: ");
        sb.append(humanReadableByteCount(freeMemory, true));
        sb.append("\r\n");
        sb.append("Allocated memory: ");
        sb.append(humanReadableByteCount(allocatedMemory, true));
        sb.append("\r\n");
        sb.append("Max memory: ");
        sb.append(humanReadableByteCount(maxMemory, true));
        sb.append("\r\n");
        sb.append("Total free memory: ");
        sb.append(humanReadableByteCount((freeMemory + (maxMemory - allocatedMemory)), true));
        sb.append("\r\n");
        return sb.toString();

    }

    public String OsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Operating System: ");
        sb.append(this.OSname());
        sb.append("\r\n");
        sb.append("Version: ");
        sb.append(this.OSversion());
        sb.append("\r\n");
        sb.append("System Architecture: ");
        sb.append(this.OsArch());
        sb.append("\r\n");
        sb.append("Available Processors (Cores): ");
        sb.append(runtime.availableProcessors());
        sb.append("\r\n");
        return sb.toString();
    }

    public String DiskInfo() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        StringBuilder sb = new StringBuilder();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            sb.append("File system root: ");
            sb.append(root.getAbsolutePath());
            sb.append("\r\n");
            sb.append("Total space: ");
            sb.append(humanReadableByteCount(root.getTotalSpace(), true));
            sb.append("\r\n");
            sb.append("Free space: ");
            sb.append(humanReadableByteCount(root.getFreeSpace(), true));
            sb.append("\r\n");
            sb.append("Usable space: ");
            sb.append(humanReadableByteCount(root.getUsableSpace(), true));
            sb.append("\r\n");
        }
        return sb.toString();
    }
    
    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}