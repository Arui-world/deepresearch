package com.cr.deepresearch.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportService {

	private final Map<String, SavedReport> reports = new ConcurrentHashMap<>();

	private final Set<String> stoppedThreads = ConcurrentHashMap.newKeySet();

	public void save(String threadId, String report) {
		if (threadId != null && report != null) {
			reports.put(threadId, new SavedReport(threadId, report, Instant.now()));
		}
	}

	public Optional<SavedReport> find(String threadId) {
		return Optional.ofNullable(reports.get(threadId));
	}

	public boolean exists(String threadId) {
		return reports.containsKey(threadId);
	}

	public void markStopped(String threadId) {
		if (threadId != null && !threadId.isBlank()) {
			stoppedThreads.add(threadId);
		}
	}

	public boolean isStopped(String threadId) {
		return stoppedThreads.contains(threadId);
	}

	public void clearStopped(String threadId) {
		stoppedThreads.remove(threadId);
	}

	public record SavedReport(String threadId, String finalReport, Instant createdAt) {
	}

}
