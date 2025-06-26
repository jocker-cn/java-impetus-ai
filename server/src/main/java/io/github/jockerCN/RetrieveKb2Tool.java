package io.github.jockerCN;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP 工具：检索知识库并返回最相关片段（JSON 数组）
 */
@Slf4j
@Component                       // 放在 MCP-Server 项目里
public class RetrieveKb2Tool implements ToolCallback {

    @Autowired
    private VectorStore vectorStore;      // RedisVectorStore 已注入
    @Autowired
    private ObjectMapper mapper;

    /* ---------- 工具定义 ---------- */
    @NonNull
    @Override
    public ToolDefinition getToolDefinition() {

        // 直接写 JSON-Schema 字符串（符合 OpenAI function 格式）
        String inputSchema = """
                {
                  "type": "object",
                  "properties": {
                   "biz": {
                        "type": "string",
                        "description": "知识库代号，必须是: 王五企业"
                      },
                    "question": {
                      "type": "string",
                      "description": "要查询的问题或关键词"
                    },
                    "topK": {
                      "type": "integer",
                      "description": "返回片段数量(默认 4)"
                    }
                  },
                  "required": ["question","biz"]
                }
                """;

        return ToolDefinition.builder()
                .name("retrieve_knowledge_ww")
                .description("检索王五企业知识库，返回最相关的文本片段列表")
                .inputSchema(inputSchema)
                .build();
    }

    /* ---------- 工具调用 ---------- */
    @NonNull
    @Override
    public String call(@NonNull String toolInput) {

        try {
            JsonNode root = mapper.readTree(toolInput);
            String question = root.path("question").asText("").trim();
            String biz = root.path("biz").asText("").trim();
            int topK = root.path("topK").asInt(8);
            if (!biz.contains("王五")) {
                return "该工具只能查询王五企业信息";
            }
            if (question.isEmpty()) {
                return "字段 question 不能为空！";
            }

            // 一次性构造检索器（按 topK 动态调整）
            VectorStoreDocumentRetriever retriever =
                    VectorStoreDocumentRetriever.builder()
                            .vectorStore(vectorStore)
                            .topK(topK)
                            .build();

            List<Document> docs =
                    retriever.retrieve(new Query(question));

            /* 组装返回 JSON 数组:
               [ { "text": "...", "metadata": { ... } }, ... ] */
            ArrayNode arr = mapper.createArrayNode();
            for (var d : docs) {
                ObjectNode node = arr.addObject();
                node.put("text", d.getText());

                ObjectNode meta = node.putObject("metadata");
                d.getMetadata().forEach(
                        (k, v) -> meta.put(k, String.valueOf(v)));
            }
            return mapper.writeValueAsString(arr);

        } catch (Exception e) {
            // MCP 规范：返回非空字符串即可传递错误信息给模型
            log.error("工具执行失败", e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    @NonNull
    @Override
    public String call(@NonNull String toolInput,
                       @Nullable ToolContext toolContext) {
        return call(toolInput);   // 不使用 context，直接委托
    }
}
