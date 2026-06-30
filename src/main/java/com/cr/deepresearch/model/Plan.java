package com.cr.deepresearch.model;

import java.io.Serializable;
import java.util.List;

public record Plan(
		boolean hasEnoughContext,
		String thought,
		String title,
		List<Step> steps) implements Serializable {
}
