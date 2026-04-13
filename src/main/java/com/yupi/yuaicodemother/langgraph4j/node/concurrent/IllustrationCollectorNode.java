package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yupi.yuaicodemother.langgraph4j.tools.UndrawIllustrationTool;
import com.yupi.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class IllustrationCollectorNode {

    private static final String NODE_NAME = "illustration_collector";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> illustrations = List.of();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                MaterialCollectionSupport.ExecutionResult result = MaterialCollectionSupport.execute(
                        NODE_NAME,
                        plan == null ? null : plan.getIllustrationTasks(),
                        ImageCollectionPlan.IllustrationTask::query,
                        task -> illustrationTool.searchIllustrations(task.query())
                );
                illustrations = result.images();
                context.putMaterialCollectionSummary(NODE_NAME, result.summary());
            } catch (Exception e) {
                log.error("插画图片收集失败: {}", e.getMessage(), e);
            }
            context.setIllustrations(illustrations);
            context.setCurrentStep("插画图片收集");
            return WorkflowContext.saveContext(context);
        });
    }
}
