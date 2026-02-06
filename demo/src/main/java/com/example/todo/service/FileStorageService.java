package com.example.todo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir;

    public FileStorageService(@Value("${app.file-storage.path}") String rootDir) throws IOException {
        this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(this.rootDir);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        String original = sanitize(file.getOriginalFilename());
        String extension = "";
        if (StringUtils.hasText(original)) {
            int idx = original.lastIndexOf('.');
            if (idx >= 0 && idx < original.length() - 1) {
                extension = original.substring(idx);
            }
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = rootDir.resolve(stored).normalize();
        file.transferTo(target);
        return new StoredFile(original, stored);
    }

    public Path resolve(String storedName) {
        return rootDir.resolve(storedName).normalize();
    }

    public byte[] readAllBytes(String storedName) throws IOException {
        return Files.readAllBytes(resolve(storedName));
    }

    public void delete(String storedName) throws IOException {
        Path target = resolve(storedName);
        Files.deleteIfExists(target);
    }

    private String sanitize(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "file";
        }
        String name = Paths.get(filename).getFileName().toString();
        name = name.replaceAll("[\\\\/\\r\\n]", "_");
        return name;
    }

    public record StoredFile(String originalName, String storedName) {}
}
