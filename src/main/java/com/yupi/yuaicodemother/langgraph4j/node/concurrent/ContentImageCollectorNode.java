package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yupi.yuaicodemother.langgraph4j.tools.ImageSearchTool;
import com.yupi.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ContentImageCollectorNode {

    private static final String NODE_NAME = "content_image_collector";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> contentImages = List.of();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                MaterialCollectionSupport.ExecutionResult result = MaterialCollectionSupport.execute(
                        NODE_NAME,
                        plan == null ? null : plan.getContentImageTasks(),
                        ImageCollectionPlan.ImageSearchTask::query,
                        task -> imageSearchTool.searchContentImages(task.query())
                );
                contentImages = result.images();
                context.putMaterialCollectionSummary(NODE_NAME, result.summary());
            } catch (Exception e) {
                log.error("内容图片收集失败: {}", e.getMessage(), e);
            }
            context.setContentImages(contentImages);
            context.setCurrentStep("内容图片收集");
            return WorkflowContext.saveContext(context);
        });
    }
}
