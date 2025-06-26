package io.github.jockerCN;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ImpetusClientAi {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ImpetusClientAi.class, args);
        ChatClient chatClient = context.getBean(ChatClient.class);

        String question = """
                请遵守系统级规则:
                系统：
                你是企业知识助手，回答时遵循：
                1. 所有带`[source]`标签的引用 **禁止增删改**。
                2. 如需概括，请使用自己的文字，但不得更改引用中的实体名称以及实体相关的信息。
                3. 如果引用与用户问题主体不一致（如名称不同,企业信息不同），请提示“未查询到相关企业信息”而非强行替换。
                
                用户提问：
                请帮我介绍一下狗屁王公司,并告知我狗屁王公司1年的订单数量大概是多少
                """;

        String content = chatClient.prompt(question).call().content();

        System.out.println(content);
    }

    @Bean
    public ChatClient client(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpSyncClients) {
        return chatClientBuilder
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .build();
    }
}