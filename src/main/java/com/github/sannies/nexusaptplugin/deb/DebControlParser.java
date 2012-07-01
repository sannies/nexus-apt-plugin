package com.github.sannies.nexusaptplugin.deb;


import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DebControlParser {

    public static Map<String, String> parse(List<String> allLines)
            throws IOException {
        LinkedList<String> rest = new LinkedList<String>(allLines);

        Map<String, String> control = new HashMap<String, String>();
        String lastKey = null;

        while (!rest.isEmpty()) {
            String line = rest.removeFirst();

            int i = line.indexOf(':');

            if (i > 0 && !line.startsWith(" ")) {
                String parts[] = line.split(":", 2);
                lastKey = parts[0];
                control.put(lastKey, parts[1].trim());
            } else {
                control.put(lastKey, control.get(lastKey) + "\n" + line.trim());
            }


        }

        return control;
    }

}
