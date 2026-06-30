package com.cr.deepresearch.controller;

import com.cr.deepresearch.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@GetMapping("/{threadId}")
	public ResponseEntity<?> getReport(@PathVariable String threadId) {
		return reportService.find(threadId)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{threadId}/exists")
	public Map<String, Object> exists(@PathVariable String threadId) {
		return Map.of(
				"thread_id", threadId,
				"exists", reportService.exists(threadId));
	}

}
