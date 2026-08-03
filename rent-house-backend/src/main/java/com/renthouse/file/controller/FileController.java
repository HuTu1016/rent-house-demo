package com.renthouse.file.controller;

import com.renthouse.common.api.ApiResponse;
import com.renthouse.file.service.FileService;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/files")
public class FileController {
    private final FileService service;
    public FileController(FileService service) { this.service = service; }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileService.FileView> upload(@RequestPart("file") MultipartFile file) throws IOException { return ApiResponse.ok(service.upload(file)); }
    @GetMapping("/{id}/access-url")
    public ResponseEntity<byte[]> read(@PathVariable String id) throws IOException { return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(service.read(id)); }
}
