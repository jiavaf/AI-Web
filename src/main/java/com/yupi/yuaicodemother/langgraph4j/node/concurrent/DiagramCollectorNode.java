package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yupi.yuaicodemother.langgraph4j.tools.MermaidDiagramTool;
import com.yupi.yuaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class DiagramCollectorNode {

    private static final String NODE_NAME = "diagram_collector";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> diagrams = List.of();
            try {
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                MaterialCollectionSupport.ExecutionResult result = MaterialCollectionSupport.execute(
                        NODE_NAME,
                        plan == null ? null : plan.getDiagramTasks(),
                        ImageCollectionPlan.DiagramTask::description,
                        task -> diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())
                );
                diagrams = result.images();
                context.putMaterialCollectionSummary(NODE_NAME, result.summary());
            } catch (Exception e) {
                log.error("架构图生成失败: {}", e.getMessage(), e);
            }
            context.setDiagrams(diagrams);
            context.setCurrentStep("架构图生成");
            return WorkflowContext.saveContext(context);
        });
    }
}
