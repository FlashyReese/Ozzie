package me.flashyreese.common.util;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtil {
    public static <T> T readJson(Gson gson, File file, Type type) {
        try {
            return gson.fromJson(new BufferedReader(new FileReader(file)), type);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void writeJson(Gson gson, File file, Object object) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(object));
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static File changeExtension(File file, String extension) {
        if (file.isDirectory()) return file;
        String filename = file.getName();

        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        filename += "." + extension;

        File newFile = file;
        if (file.renameTo(new File(file.getParentFile(), filename))) {
            newFile = new File(file.getParentFile(), filename);
        }
        return newFile;
    }

    public static String getFileNameWithExtension(File file) {
        return file.getName();
    }

    public static String getFileName(File file) {
        String fileNameWithExtension = getFileNameWithExtension(file);
        if (fileNameWithExtension.contains(".")) {
            return fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
        }
        return fileNameWithExtension;
    }

    public static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    public static List<String> readLines(File file) {
        List<String> lines = new ArrayList<>();
        if (file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                while (line != null) {
                    lines.add(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        } else {
            return null;
        }
    }

    public static boolean removeFileDirectory(File directory) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                removeFileDirectory(file);
            }
            if (!file.delete()) return false;
        }
        return true;
    }

    public static boolean createDirectoryIfNotExist(File file) {
        if (!file.exists())
            return file.mkdirs();
        else
            return file.isDirectory();
    }
}
