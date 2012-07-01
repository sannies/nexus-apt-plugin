package com.github.sannies.nexusaptplugin.ar;


import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ArReaderTest {

    @Test
    public void test() throws IOException {
        File deb = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "php5_5.3.10-1ubuntu3.2_all.deb");
        ArReader arReader = new ArReader(deb);
        for (ReadableArFile readableArFile : arReader) {
            System.err.println(readableArFile.getName());
             readableArFile.open();
        }
    }
}
