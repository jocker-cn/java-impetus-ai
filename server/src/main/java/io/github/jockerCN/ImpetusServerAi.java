package io.github.jockerCN;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@SpringBootApplication
public class ImpetusServerAi {
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(ImpetusServerAi.class, args);

        IngestService ingester = context.getBean(IngestService.class);

        Path p = Paths.get("D:\\11.txt");
        ingester.ingest(
                "123",
                p.getFileName().toString(),
                Files.readAllBytes(p),
                Map.of("source", "file-path"));
    }


    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }

    @Bean
    public Advisor myRagAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .similarityThreshold(0.75)
                        .vectorStore(vectorStore)
                        .topK(8)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder().build())
                .build();
    }

}