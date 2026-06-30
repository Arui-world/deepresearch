package com.cr.deepresearch.controller;

import com.cr.deepresearch.model.ChatRequest;
import com.cr.deepresearch.model.FeedbackRequest;
import com.cr.deepresearch.model.GraphId;
import com.cr.deepresearch.service.GraphProcess;
import com.cr.deepresearch.service.ReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

	private final GraphProcess graphProcess;

	private final ReportService reportService;

	public ChatController(GraphProcess graphProcess, ReportService reportService) {
		this.graphProcess = graphProcess;
		this.reportService = reportService;
	}

	@PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@RequestBody ChatRequest request) {
		return graphProcess.stream(request);
	}

	@PostMapping("/stop")
	public ResponseEntity<Map<String, Object>> stop(@RequestBody GraphId graphId) {
		reportService.markStopped(graphId.threadId());
		return ResponseEntity.ok(Map.of(
				"stopped", true,
				"thread_id", graphId.threadId()));
	}

	@PostMapping("/resume")
	public ResponseEntity<Map<String, Object>> resume(@RequestBody FeedbackRequest request) {
		String threadId = request.threadId();
		return reportService.find(threadId)
				.<ResponseEntity<Map<String, Object>>>map(report -> ResponseEntity.ok(Map.of(
						"resumed", false,
						"thread_id", threadId,
						"final_report", report.finalReport(),
						"message", "阶段一未实现 checkpoint 恢复，已返回当前线程已有报告。")))
				.orElseGet(() -> ResponseEntity.accepted().body(Map.of(
						"resumed", false,
						"thread_id", threadId == null ? "" : threadId,
						"message", "阶段一未实现 checkpoint 恢复，且当前线程暂无已保存报告。")));
	}

}
