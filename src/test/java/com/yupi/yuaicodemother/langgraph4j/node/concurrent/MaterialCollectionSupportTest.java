package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialCollectionSupportTest {

    @Test
    void shouldCollectTaskSummaryForPartialSuccess() {
        MaterialCollectionSupport.ExecutionResult result = MaterialCollectionSupport.execute(
                "content_image_collector",
                List.of("banner", "hero", "broken"),
                query -> query,
                query -> {
                    if ("broken".equals(query)) {
                        throw new IllegalStateException("mock failure");
                    }
                    return List.of(ImageResource.builder()
                            .description(query)
                            .url("https://example.com/" + query)
                            .build());
                }
        );

        assertEquals("PARTIAL_SUCCESS", result.summary().getStatus());
        assertEquals(3, result.summary().getTaskCount());
        assertEquals(2, result.summary().getSuccessTaskCount());
        assertEquals(1, result.summary().getFailedTaskCount());
        assertEquals(2, result.summary().getImageCount());
        assertEquals(2, result.images().size());
        assertEquals(1, result.summary().getFailedTasks().size());
    }
}
