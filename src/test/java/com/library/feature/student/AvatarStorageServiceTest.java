package com.library.feature.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeStudentAvatar_shouldPersistImageUnderUploadsFolder() throws IOException {
        AvatarStorageService storageService = new AvatarStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "avatarFile",
                "avatar.png",
                "image/png",
                "png-content".getBytes()
        );

        String avatarUrl = storageService.storeStudentAvatar(7, file, null);

        assertThat(avatarUrl).startsWith("/uploads/avatars/student-7-").endsWith(".png");
        String filename = avatarUrl.substring("/uploads/avatars/".length());
        assertThat(Files.exists(tempDir.resolve("avatars").resolve(filename))).isTrue();
    }

    @Test
    void storeStudentAvatar_shouldRejectNonImageFile() {
        AvatarStorageService storageService = new AvatarStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "avatarFile",
                "avatar.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> storageService.storeStudentAvatar(7, file, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ảnh");
    }
}
