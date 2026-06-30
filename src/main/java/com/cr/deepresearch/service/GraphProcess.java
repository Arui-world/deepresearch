package com.cr.deepresearch.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.cr.deepresearch.model.ChatRequest;
import com.cr.deepresearch.model.GraphId;
import com.cr.deepresearch.model.SseEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GraphProcess {

	private static final long SSE_TIMEOUT_MILLIS = 10 * 60 * 1000L;

	private final CompiledGraph deepResearchGraph;

	private final ReportService reportService;

	public GraphProcess(CompiledGraph deepResearchGraph, ReportService reportService) {
		this.deepResearchGraph = deepResearchGraph;
		this.reportService = reportService;
	}

	public SseEmitter stream(ChatRequest request) {
		GraphId graphId = newGraphId(request);
		reportService.clearStopped(graphId.threadId());

		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
		AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
		AtomicBoolean finalSent = new AtomicBoolean(false);

		CompletableFuture.runAsync(() -> {
			RunnableConfig config = RunnableConfig.builder().threadId(graphId.threadId()).build();
			Disposable subscription = deepResearchGraph.stream(initialState(request, graphId), config)
					.subscribe(
							output -> handleOutput(emitter, graphId, output, finalSent, subscriptionRef),
							error -> sendError(emitter, graphId, error),
							() -> complete(emitter, graphId, finalSent));
			subscriptionRef.set(subscription);
		});

		emitter.onTimeout(() -> dispose(subscriptionRef));
		emitter.onCompletion(() -> dispose(subscriptionRef));
		emitter.onError(error -> dispose(subscriptionRef));
		return emitter;
	}

	private GraphId newGraphId(ChatRequest request) {
		String sessionId = request.normalizedSessionId();
		String threadId = request.threadId() == null || request.threadId().isBlank()
				? "thread-" + UUID.randomUUID()
				: request.threadId();
		return new GraphId(sessionId, threadId);
	}

	private Map<String, Object> initialState(ChatRequest request, GraphId graphId) {
		Map<String, Object> state = new LinkedHashMap<>();
		state.put("session_id", graphId.sessionId());
		state.put("thread_id", graphId.threadId());
		state.put("query", request.normalizedQuery());
		state.put("enable_deepresearch", request.normalizedEnableDeepresearch());
		state.put("max_step_num", request.normalizedMaxStepNum());
		return state;
	}

	private void handleOutput(
			SseEmitter emitter,
			GraphId graphId,
			NodeOutput output,
			AtomicBoolean finalSent,
			AtomicReference<Disposable> subscriptionRef) {
		if (reportService.isStopped(graphId.threadId())) {
			send(emitter, SseEvent.message("final", graphId, "stop", "任务已停止。"));
			dispose(subscriptionRef);
			emitter.complete();
			return;
		}

		if (output.isSTART()) {
			return;
		}
		if (output.isEND()) {
			sendFinal(emitter, graphId, output, finalSent);
			return;
		}

		String node = output.node();
		send(emitter, SseEvent.message("node_start", graphId, node, node + " started"));
		send(emitter, SseEvent.of("node_end", graphId, node, nodeSummary(output)));

		if ("planner".equals(node)) {
			output.state().value("current_plan").ifPresent(plan ->
					send(emitter, SseEvent.of("plan", graphId, node, plan)));
		}
		if ("reporter".equals(node)) {
			output.state().value("final_report", String.class).ifPresent(report -> {
				reportService.save(graphId.threadId(), report);
				send(emitter, SseEvent.of("report_delta", graphId, node, report));
			});
		}
	}

	private Map<String, Object> nodeSummary(NodeOutput output) {
		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("node", output.node());
		summary.put("state", output.state().data());
		return summary;
	}

	private void sendFinal(SseEmitter emitter, GraphId graphId, NodeOutput output, AtomicBoolean finalSent) {
		if (!finalSent.compareAndSet(false, true)) {
			return;
		}
		String report = output.state().value("final_report", "");
		if (!report.isBlank()) {
			reportService.save(graphId.threadId(), report);
		}
		send(emitter, SseEvent.of("final", graphId, output.node(), Map.of(
				"graph_id", graphId,
				"final_report", report)));
	}

	private void complete(SseEmitter emitter, GraphId graphId, AtomicBoolean finalSent) {
		if (!finalSent.get()) {
			reportService.find(graphId.threadId()).ifPresentOrElse(
					report -> send(emitter, SseEvent.of("final", graphId, "complete", Map.of(
							"graph_id", graphId,
							"final_report", report.finalReport()))),
					() -> send(emitter, SseEvent.message("final", graphId, "complete", "Graph execution completed.")));
		}
		emitter.complete();
	}

	private void sendError(SseEmitter emitter, GraphId graphId, Throwable error) {
		send(emitter, SseEvent.message("error", graphId, "graph", error.getMessage()));
		emitter.completeWithError(error);
	}

	private void send(SseEmitter emitter, SseEvent event) {
		try {
			emitter.send(SseEmitter.event().name(event.type()).data(event));
		}
		catch (IOException ex) {
			emitter.completeWithError(ex);
		}
	}

	private void dispose(AtomicReference<Disposable> subscriptionRef) {
		Disposable subscription = subscriptionRef.get();
		if (subscription != null && !subscription.isDisposed()) {
			subscription.dispose();
		}
	}

}
