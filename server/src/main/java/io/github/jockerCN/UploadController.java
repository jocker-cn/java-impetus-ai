package io.github.jockerCN;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController

@RequestMapping("/api/rag")
public class UploadController {

    @Autowired
    private  IngestService ingester;

    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(250)// 每块 ≤ 250 tokens
//            .withOverlapSize(64)         // 相邻块重复 64 tokens（1.0.0-M8+ 提供）
            .build();

    @PostMapping("/upload")
    public void upload(@RequestParam MultipartFile file,
                       @RequestParam(required = false) String docId,
                       @RequestParam(required = false) String tags) throws IOException {

        ingester.ingest(
                Optional.ofNullable(docId).orElse(UUID.randomUUID().toString()),
                file.getOriginalFilename(),
                file.getBytes(),
                parseTags(tags));
    }

    @PostMapping("/api/rag/path")
    public void ingestByPath(@RequestParam String path,
                             @RequestParam(required = false) String docId) throws IOException {

        Path p = Paths.get(path);
        ingester.ingest(
                Optional.ofNullable(docId).orElse(p.toAbsolutePath().toString()),
                p.getFileName().toString(),
                Files.readAllBytes(p),
                Map.of("source", "file-path"));
    }

    /* tags="key1=val1,key2=val2" → Map */
    private Map<String,String> parseTags(String s) {
        if (s == null || s.isBlank()) return Map.of();
        return Arrays.stream(s.split(","))
                .map(kv -> kv.split("=", 2))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }
}
