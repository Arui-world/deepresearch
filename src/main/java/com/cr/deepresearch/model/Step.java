package com.cr.deepresearch.model;

import java.io.Serializable;

public record Step(
		String title,
		String description,
		String stepType,
		boolean needWebSearch,
		String executionStatus,
		String executionRes) implements Serializable {
}
