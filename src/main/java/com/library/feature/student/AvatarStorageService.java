package com.library.feature.student;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

    private final Path avatarDirectory;

    public AvatarStorageService(@Value("${app.upload.root-dir:uploads}") String uploadRootDir) {
        this.avatarDirectory = Paths.get(uploadRootDir).toAbsolutePath().normalize().resolve("avatars");
    }

    public String storeStudentAvatar(Integer studentId, MultipartFile file, String currentAvatarUrl) {
        if (file == null || file.isEmpty()) {
            return currentAvatarUrl;
        }

        validateImage(file);

        try {
            Files.createDirectories(avatarDirectory);
            String extension = resolveExtension(file);
            String filename = "student-" + studentId + "-" + UUID.randomUUID() + extension;
            Path target = avatarDirectory.resolve(filename).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            deleteManagedAvatar(currentAvatarUrl);
            return "/uploads/avatars/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu ảnh đại diện lúc này.", ex);
        }
    }

    void deleteManagedAvatar(String avatarUrl) throws IOException {
        if (!StringUtils.hasText(avatarUrl) || !avatarUrl.startsWith("/uploads/avatars/")) {
            return;
        }

        String filename = avatarUrl.substring("/uploads/avatars/".length()).trim();
        if (filename.isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return;
        }

        Files.deleteIfExists(avatarDirectory.resolve(filename).normalize());
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện phải nhỏ hơn hoặc bằng 5MB.");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh cho ảnh đại diện.");
        }

        String extension = resolveExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Ảnh đại diện chỉ hỗ trợ JPG, PNG, GIF hoặc WEBP.");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("Tên file ảnh không hợp lệ.");
        }

        String normalizedName = originalFilename.trim().toLowerCase(Locale.ROOT);
        int lastDotIndex = normalizedName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == normalizedName.length() - 1) {
            throw new IllegalArgumentException("File ảnh phải có phần mở rộng hợp lệ.");
        }

        return normalizedName.substring(lastDotIndex);
    }
}
