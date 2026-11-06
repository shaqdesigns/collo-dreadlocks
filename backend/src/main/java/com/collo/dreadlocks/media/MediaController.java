package com.collo.dreadlocks.media;

import org.springframework.core.io.PathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorage mediaStorage;

    public MediaController(MediaStorage mediaStorage) {
        this.mediaStorage = mediaStorage;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<PathResource> get(@PathVariable String filename) {
        Path path = mediaStorage.resolve(filename);
        MediaType type = filename.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : filename.endsWith(".webp")
                ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(new PathResource(path));
    }
}
