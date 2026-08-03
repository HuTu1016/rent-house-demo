package com.renthouse.file;

import com.renthouse.auth.CurrentUser;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import java.io.IOException;
import java.nio.file.*;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    private final SnowflakeIdGenerator ids;
    private final Path root;
    public FileService(SnowflakeIdGenerator ids, @Value("${app.storage.local-dir}") String localDir) { this.ids = ids; this.root = Paths.get(localDir).toAbsolutePath().normalize(); }
    public FileView upload(MultipartFile file) throws IOException {
        CurrentUser.require();
        if (file.isEmpty() || file.getSize() > 20 * 1024 * 1024) throw new BusinessException("FILE_INVALID", "文件不能为空且不得超过20MB", HttpStatus.BAD_REQUEST);
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String name = ids.nextId() + (extension == null ? "" : "." + extension);
        Files.createDirectories(root);
        Files.copy(file.getInputStream(), root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return new FileView(name, "/api/v1/files/" + name + "/access-url", file.getContentType(), file.getSize());
    }
    public byte[] read(String id) throws IOException {
        CurrentUser.require();
        Path file = root.resolve(id).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) throw new BusinessException("FILE_NOT_FOUND", "文件不存在", HttpStatus.NOT_FOUND);
        return Files.readAllBytes(file);
    }
    public record FileView(String fileId, String accessUrl, String contentType, long size) { }
}
