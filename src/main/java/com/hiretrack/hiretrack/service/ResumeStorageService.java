package com.hiretrack.hiretrack.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeStorageService {

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024; // 5 MB

    private static final Path UPLOAD_DIR =
            Paths.get("uploads", "resumes")
                    .toAbsolutePath()
                    .normalize();

    public String save(MultipartFile file)
            throws IOException {

        validate(file);

        Files.createDirectories(UPLOAD_DIR);

        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );

        String extension = getExtension(
                originalFilename
        );

        String filename =
                UUID.randomUUID()
                        .toString()
                        + extension;

        Path destination =
                UPLOAD_DIR
                        .resolve(filename)
                        .normalize();

        /*
         * Prevent path traversal.
         */
        if (!destination.startsWith(UPLOAD_DIR)) {
            throw new IOException(
                    "Invalid file path."
            );
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return filename;
    }

    public Path getPath(String filename) {

        String safeFilename =
                Paths.get(filename)
                        .getFileName()
                        .toString();

        Path path =
                UPLOAD_DIR
                        .resolve(safeFilename)
                        .normalize();

        if (!path.startsWith(UPLOAD_DIR)) {
            throw new IllegalArgumentException(
                    "Invalid filename."
            );
        }

        return path;
    }

    public String buildResumeUrl(
            String filename) {

        return "/api/resume/download/"
                + filename;
    }

    public MediaType contentType(
            Path path) {

        try {

            String type =
                    Files.probeContentType(path);

            if (type != null) {

                return MediaType.parseMediaType(
                        type
                );
            }

        } catch (Exception ignored) {
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    public void deleteQuietly(
            String filename) {

        try {

            Path path =
                    getPath(filename);

            Files.deleteIfExists(path);

        } catch (Exception ignored) {
            // Do not hide the original application error.
        }
    }

    private void validate(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a resume."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Resume must be 5 MB or smaller."
            );
        }

        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );

        String extension =
                getExtension(
                        originalFilename
                ).toLowerCase();

        if (!extension.equals(".pdf") &&
                !extension.equals(".doc") &&
                !extension.equals(".docx")) {

            throw new IllegalArgumentException(
                    "Only PDF, DOC, and DOCX files are allowed."
            );
        }
    }

    private String getExtension(
            String filename) {

        int index =
                filename.lastIndexOf('.');

        if (index == -1) {
            return "";
        }

        return filename.substring(index);
    }
}