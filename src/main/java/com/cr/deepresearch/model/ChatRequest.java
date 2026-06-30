package com.cr.deepresearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ChatRequest(
		@JsonProperty("session_id") String sessionId,
		@JsonProperty("thread_id") String threadId,
		String query,
		@JsonProperty("enable_deepresearch") Boolean enableDeepresearch,
		@JsonProperty("max_step_num") Integer maxStepNum,
		@JsonProperty("max_plan_iterations") Integer maxPlanIterations,
		@JsonProperty("auto_accepted_plan") Boolean autoAcceptedPlan,
		@JsonProperty("search_engine") String searchEngine,
		@JsonProperty("enable_search_filter") Boolean enableSearchFilter,
		@JsonProperty("optimize_query_num") Integer optimizeQueryNum,
		@JsonProperty("user_upload_file") Boolean userUploadFile,
		@JsonProperty("mcp_settings") Map<String, Object> mcpSettings) {

	public String normalizedSessionId() {
		return isBlank(sessionId) ? "__default__" : sessionId;
	}

	public String normalizedQuery() {
		return query == null ? "" : query.trim();
	}

	public boolean normalizedEnableDeepresearch() {
		return enableDeepresearch == null || enableDeepresearch;
	}

	public int normalizedMaxStepNum() {
		return maxStepNum == null || maxStepNum <= 0 ? 3 : maxStepNum;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
