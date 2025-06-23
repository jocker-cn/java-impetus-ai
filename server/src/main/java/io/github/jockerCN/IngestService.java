package io.github.jockerCN;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisDataException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IngestService {

    @Autowired
    private VectorStore vectorStore;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(250)// 每块 ≤ 250 tokens
//            .withOverlapSize(64)         // 相邻块重复 64 tokens（1.0.0-M8+ 提供）
            .build();    // :contentReference[oaicite:1]{index=1}


    /*
      RedisVectorStore 需要先建立索引   其中DIM 1536 向量的数量 OpenAI 1536  |  BAAI/bge-large-zh-v1.5 模型 1024
      FT.CREATE default-index ON JSON PREFIX 1 "default:" SCHEMA
      $.content AS content TEXT
      $.docId AS docId TAG
      $.ingestedAt AS ingestedAt TEXT
      $.filename AS filename TEXT
      $.source AS source TEXT
      $.embedding AS embedding VECTOR HNSW 6 TYPE FLOAT64 DIM 1536 DISTANCE_METRIC COSINE
     */

    /**
     * 新增或替换一份文档
     * @param docId    唯一 ID，重复即覆盖
     * @param filename 源文件名
     * @param bytes    文件内容（这里只示例纯文本）
     * @param tags     额外元数据
     */
    public void ingest(String docId, String filename, byte[] bytes,
                       Map<String, String> tags) {

        Document raw = new Document(
                "123",
                new String(bytes, StandardCharsets.UTF_8),
                new HashMap<>(tags) {{
                    put("docId", docId);
                    put("filename", filename);
                    put("ingestedAt", Instant.now().toString());
                }});

        // RedisVectorStore 不会自动覆盖同 docId；先删再加

        try {
            vectorStore.delete("docId==123");
        } catch (IllegalStateException ex) {
            if (!(ex.getCause() instanceof JedisDataException)
                || !ex.getCause().getMessage().contains("no such index")) {
                throw ex;      // 只有真正“索引不存在”才忽略
            }
        }

        List<Document> chunks = splitter.apply(List.of(raw));
        vectorStore.add(chunks);
    }
}
