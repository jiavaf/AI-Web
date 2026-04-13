package com.yupi.yuaicodemother.langgraph4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Material collection execution summary for a single workflow node.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCollectionSummary implements Serializable {

    private String nodeName;

    /**
     * SUCCESS / PARTIAL_SUCCESS / FAILED / SKIPPED
     */
    private String status;

    private int taskCount;

    private int successTaskCount;

    private int failedTaskCount;

    private int imageCount;

    private long durationMillis;

    private List<String> failedTasks;

    @Serial
    private static final long serialVersionUID = 1L;
}
