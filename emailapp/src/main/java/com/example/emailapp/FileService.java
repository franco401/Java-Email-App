package com.example.emailapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileService {
    static final Path rootLocation = Paths.get(Security.fileDirectory);

    public static void store(MultipartFile[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                if (files[i].isEmpty()) {
                    throw new RuntimeException("Failed to store empty file.");
                }
                Path destinationFile = rootLocation.resolve(
                        Paths.get(files[i].getOriginalFilename()))
                        .normalize().toAbsolutePath();
                if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                    // This is a security check
                    throw new RuntimeException(
                            "Cannot store file outside current directory.");
                }
                try (InputStream inputStream = files[i].getInputStream()) {
                    Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
                }
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to store file: ", e);
            }
        }
    }

    public static Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: ", e);
        }
    }
}
