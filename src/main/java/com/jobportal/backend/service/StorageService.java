package com.jobportal.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootPath = Paths.get("uploads");

    public StorageService() {
        try {
            Files.createDirectories(rootPath);
            Files.createDirectories(rootPath.resolve("photos"));
            Files.createDirectories(rootPath.resolve("resumes"));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        Path targetDir = rootPath.resolve(subDir);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), targetDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public Resource loadFile(String filename, String subDir) {
        try {
            Path file = rootPath.resolve(subDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public void deleteFile(String filename, String subDir) throws IOException {
        Path file = rootPath.resolve(subDir).resolve(filename);
        Files.deleteIfExists(file);
    }
}
