package com.github.vijaypatidar.ssh.automate.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskHelper {

    public static String getDiskSpaceCommand(){
        return "df -h --output=source,avail";
    }
    public static Map<String, Double> getAvailableDiskSpace(List<String> lines) {
        Map<String, Double> spaces = new HashMap<>();
        int i = 0;
        while (i < lines.size()) {
            if (lines.get(i).startsWith("Filesystem")) {
                break;
            } else i++;
        }
        if (i < lines.size()) {
            for (int row = i + 1; row < lines.size(); row++) {
                String[] space = lines
                        .get(row)
                        .replaceAll("\\s+", " ")
                        .split(" ");
                double sizeAvail = 0;
                if (space[1].toUpperCase().endsWith("M")) {
                    sizeAvail = Double.parseDouble(space[1].replace("M", ""));
                } else if (space[1].toUpperCase().endsWith("G")) {
                    sizeAvail = 1024 * Double.parseDouble(space[1].replace("G", ""));
                }
                spaces.put(space[0], sizeAvail);
            }
        }
        return spaces;
    }
}
