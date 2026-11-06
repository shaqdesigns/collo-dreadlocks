package com.collo.dreadlocks.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaStorage {

    private final Path directory;

    public MediaStorage(@Value("${app.uploads.directory:uploads}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMediaException("Choose a photo to upload");
        }
        String extension = extensionFor(file.getContentType());
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(filename).normalize();
            if (!target.startsWith(directory)) {
                throw new InvalidMediaException("Could not store that photo");
            }
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
        } catch (IOException exception) {
            throw new InvalidMediaException("Could not store that photo");
        }
        return "/api/media/" + filename;
    }

    public Path resolve(String filename) {
        if (filename == null || !filename.toLowerCase(Locale.ROOT).matches("[a-f0-9\\-]{36}\\.(jpg|png|webp)")) {
            throw new InvalidMediaException("Photo not found");
        }
        Path target = directory.resolve(filename).normalize();
        if (!target.startsWith(directory) || !Files.exists(target)) {
            throw new InvalidMediaException("Photo not found");
        }
        return target;
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            throw new InvalidMediaException("Upload a JPG, PNG, or WebP photo");
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new InvalidMediaException("Upload a JPG, PNG, or WebP photo");
        };
    }
}
