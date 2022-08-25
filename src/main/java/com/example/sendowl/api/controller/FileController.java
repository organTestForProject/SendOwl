package com.example.sendowl.api.controller;


import com.example.sendowl.api.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/files")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "single file upload api", description = "single file upload api")
    @PostMapping(path = "single") // 단일 파일 업로드
    public ResponseEntity<?> fileUpload(@RequestPart MultipartFile file) throws Exception {

        String fileName = fileService.fileUpload(file);

        return new ResponseEntity(fileName, HttpStatus.OK);
    }

    @Operation(summary = "multiple files upload api", description = "multiple files upload api")
    @PostMapping(path = "multiple") // 다중 파일 업로드
    public ResponseEntity<?> multipleFilesUpload(@RequestPart List<MultipartFile> files) throws Exception {

        List<String> fileNames = fileService.multipleFilesUpload(files);

        return new ResponseEntity(fileNames, HttpStatus.OK);
    }

}