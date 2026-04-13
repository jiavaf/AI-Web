package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yupi.yuaicodemother.langgraph4j.tools.LogoGeneratorTool;
import com.yupi.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class LogoCollectorNode {

    private static final String NODE_NAME = "logo_collector";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> logos = List.of();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                MaterialCollectionSupport.ExecutionResult result = MaterialCollectionSupport.execute(
                        NODE_NAME,
                        plan == null ? null : plan.getLogoTasks(),
                        ImageCollectionPlan.LogoTask::description,
                        task -> logoTool.generateLogos(task.description())
                );
                logos = result.images();
                context.putMaterialCollectionSummary(NODE_NAME, result.summary());
            } catch (Exception e) {
                log.error("Logo 生成失败: {}", e.getMessage(), e);
            }
            context.setLogos(logos);
            context.setCurrentStep("Logo 生成");
            return WorkflowContext.saveContext(context);
        });
    }
}
