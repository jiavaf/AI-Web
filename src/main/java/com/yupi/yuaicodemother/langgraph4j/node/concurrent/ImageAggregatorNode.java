package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.model.MaterialCollectionSummary;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片聚合节点
 */
@Slf4j
public class ImageAggregatorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> allImages = new ArrayList<>();
            log.info("开始聚合并发收集的图片");

            if (context.getContentImages() != null) {
                allImages.addAll(context.getContentImages());
            }
            if (context.getIllustrations() != null) {
                allImages.addAll(context.getIllustrations());
            }
            if (context.getDiagrams() != null) {
                allImages.addAll(context.getDiagrams());
            }
            if (context.getLogos() != null) {
                allImages.addAll(context.getLogos());
            }

            Map<String, MaterialCollectionSummary> summaries = context.getMaterialCollectionSummaries();
            if (summaries != null && !summaries.isEmpty()) {
                long abnormalNodeCount = summaries.values().stream()
                        .map(MaterialCollectionSummary::getStatus)
                        .filter(status -> !"SUCCESS".equals(status) && !"SKIPPED".equals(status))
                        .count();
                log.info("图片素材节点执行摘要: nodeCount={}, abnormalNodeCount={}", summaries.size(), abnormalNodeCount);
            }

            log.info("图片聚合完成，总共 {} 张图片", allImages.size());
            context.setImageList(allImages);
            context.setCurrentStep("图片聚合");
            return WorkflowContext.saveContext(context);
        });
    }
}
