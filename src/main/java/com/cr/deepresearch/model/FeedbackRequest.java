package com.cr.deepresearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FeedbackRequest(
		@JsonProperty("session_id") String sessionId,
		@JsonProperty("thread_id") String threadId,
		Boolean feedback,
		@JsonProperty("feedback_content") String feedbackContent,
		String query) {
}
