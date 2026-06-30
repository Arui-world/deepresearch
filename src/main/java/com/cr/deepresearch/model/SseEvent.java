package com.cr.deepresearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public record SseEvent(
		String type,
		@JsonProperty("session_id") String sessionId,
		@JsonProperty("thread_id") String threadId,
		String node,
		Object data,
		Instant timestamp) {

	public static SseEvent of(String type, GraphId graphId, String node, Object data) {
		return new SseEvent(type, graphId.sessionId(), graphId.threadId(), node, data, Instant.now());
	}

	public static SseEvent message(String type, GraphId graphId, String node, String message) {
		return of(type, graphId, node, Map.of("message", message));
	}

}
