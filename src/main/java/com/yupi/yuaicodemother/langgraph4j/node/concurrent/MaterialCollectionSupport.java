package com.yupi.yuaicodemother.langgraph4j.node.concurrent;

import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.model.MaterialCollectionSummary;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Shared helper for concurrent material collection nodes.
 */
@Slf4j
public final class MaterialCollectionSupport {

    private MaterialCollectionSupport() {
    }

    public static <T> ExecutionResult execute(
            String nodeName,
            List<T> tasks,
            Function<T, String> taskLabelProvider,
            Function<T, List<ImageResource>> taskExecutor
    ) {
        List<T> safeTasks = tasks == null ? List.of() : tasks;
        if (safeTasks.isEmpty()) {
            MaterialCollectionSummary summary = MaterialCollectionSummary.builder()
                    .nodeName(nodeName)
                    .status("SKIPPED")
                    .taskCount(0)
                    .successTaskCount(0)
                    .failedTaskCount(0)
                    .imageCount(0)
                    .durationMillis(0)
                    .failedTasks(List.of())
                    .build();
            return new ExecutionResult(List.of(), summary);
        }

        long startNanos = System.nanoTime();
        List<ImageResource> collectedImages = Collections.synchronizedList(new ArrayList<>());
        List<String> failedTasks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successTaskCount = new AtomicInteger();
        AtomicInteger failedTaskCount = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = safeTasks.stream()
                    .map(task -> CompletableFuture.runAsync(() ->
                            executeSingleTask(nodeName, task, taskLabelProvider, taskExecutor,
                                    collectedImages, failedTasks, successTaskCount, failedTaskCount), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        long durationMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
        MaterialCollectionSummary summary = MaterialCollectionSummary.builder()
                .nodeName(nodeName)
                .status(resolveStatus(safeTasks.size(), successTaskCount.get(), failedTaskCount.get(), collectedImages.size()))
                .taskCount(safeTasks.size())
                .successTaskCount(successTaskCount.get())
                .failedTaskCount(failedTaskCount.get())
                .imageCount(collectedImages.size())
                .durationMillis(durationMillis)
                .failedTasks(List.copyOf(failedTasks))
                .build();

        log.info("素材节点 {} 执行完成: status={}, taskCount={}, successTaskCount={}, failedTaskCount={}, imageCount={}, durationMs={}",
                nodeName,
                summary.getStatus(),
                summary.getTaskCount(),
                summary.getSuccessTaskCount(),
                summary.getFailedTaskCount(),
                summary.getImageCount(),
                summary.getDurationMillis());

        return new ExecutionResult(List.copyOf(collectedImages), summary);
    }

    private static <T> void executeSingleTask(
            String nodeName,
            T task,
            Function<T, String> taskLabelProvider,
            Function<T, List<ImageResource>> taskExecutor,
            List<ImageResource> collectedImages,
            List<String> failedTasks,
            AtomicInteger successTaskCount,
            AtomicInteger failedTaskCount
    ) {
        String taskLabel = safeTaskLabel(task, taskLabelProvider);
        long taskStartNanos = System.nanoTime();
        try {
            List<ImageResource> taskImages = taskExecutor.apply(task);
            if (taskImages != null && !taskImages.isEmpty()) {
                collectedImages.addAll(taskImages);
            }
            long durationMillis = Duration.ofNanos(System.nanoTime() - taskStartNanos).toMillis();
            successTaskCount.incrementAndGet();
            log.info("素材节点 {} 任务成功: task={}, imageCount={}, durationMs={}",
                    nodeName,
                    taskLabel,
                    taskImages == null ? 0 : taskImages.size(),
                    durationMillis);
        } catch (Exception e) {
            long durationMillis = Duration.ofNanos(System.nanoTime() - taskStartNanos).toMillis();
            failedTaskCount.incrementAndGet();
            String failureSummary = String.format("%s -> %s", taskLabel, e.getMessage());
            failedTasks.add(failureSummary);
            log.warn("素材节点 {} 任务失败: task={}, durationMs={}, error={}",
                    nodeName, taskLabel, durationMillis, e.getMessage(), e);
        }
    }

    private static <T> String safeTaskLabel(T task, Function<T, String> taskLabelProvider) {
        try {
            String label = taskLabelProvider.apply(task);
            return label == null || label.isBlank() ? String.valueOf(task) : label;
        } catch (Exception e) {
            return String.valueOf(task);
        }
    }

    private static String resolveStatus(int taskCount, int successTaskCount, int failedTaskCount, int imageCount) {
        if (taskCount == 0) {
            return "SKIPPED";
        }
        if (failedTaskCount == 0) {
            return "SUCCESS";
        }
        if (successTaskCount > 0 || imageCount > 0) {
            return "PARTIAL_SUCCESS";
        }
        return "FAILED";
    }

    public record ExecutionResult(List<ImageResource> images, MaterialCollectionSummary summary) {
    }
}
