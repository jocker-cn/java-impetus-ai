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

        String question = "狗屁王公司简介";

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