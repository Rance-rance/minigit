package com.minigit.util;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileEncodingDetector {
    public static String detect(String path) {
        Path filePath = Paths.get(path);

        try {
            FileInputStream inputStream = new FileInputStream(filePath.toFile());
            UniversalDetector detector = new UniversalDetector(null);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                detector.handleData(buffer, 0, bytesRead);
            }

            detector.dataEnd();
            String fileEncoding = detector.getDetectedCharset();

            detector.reset();
            inputStream.close();
            return fileEncoding;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
