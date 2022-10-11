package com.ftn.socialnetwork.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;

public class FileUploadUtil {

    public static void saveFile(String uploadDir, String fileName,
                                BufferedImage image) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

    }
}