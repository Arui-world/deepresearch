package com.cr.deepresearch.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlannerNode implements NodeAction {

	@Override
	public Map<String, Object> apply(OverAllState state) {
		String query = state.value("query", "");
		int maxStepNum = state.value("max_step_num", 3);
		int stepCount = Math.max(1, Math.min(maxStepNum, 3));

		List<Map<String, Object>> steps = List.of(
				step("问题界定", "明确研究问题、输出范围和关键判断口径。"),
				step("资料梳理", "整理阶段一可用上下文，形成结构化要点。"),
				step("报告生成", "基于计划和上下文生成 Markdown 简版研究报告。"))
			.subList(0, stepCount);

		Map<String, Object> plan = new LinkedHashMap<>();
		plan.put("has_enough_context", true);
		plan.put("thought", "阶段一先验证 SSE、Graph 状态流转和报告保存，不执行联网搜索或 RAG。");
		plan.put("title", query.isBlank() ? "空问题处理计划" : "关于「" + query + "」的最小研究计划");
		plan.put("steps", steps);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("current_plan", plan);
		result.put("planner_content", buildPlannerContent(plan));
		return result;
	}

	private Map<String, Object> step(String title, String description) {
		Map<String, Object> step = new LinkedHashMap<>();
		step.put("title", title);
		step.put("description", description);
		step.put("step_type", "research");
		step.put("need_web_search", false);
		step.put("execution_status", "pending");
		step.put("execution_res", null);
		return step;
	}

	@SuppressWarnings("unchecked")
	private String buildPlannerContent(Map<String, Object> plan) {
		StringBuilder builder = new StringBuilder();
		builder.append("# ").append(plan.get("title")).append("\n\n");
		builder.append(plan.get("thought")).append("\n\n");
		List<Map<String, Object>> steps = (List<Map<String, Object>>) plan.get("steps");
		for (int i = 0; i < steps.size(); i++) {
			Map<String, Object> step = steps.get(i);
			builder.append(i + 1)
				.append(". ")
				.append(step.get("title"))
				.append("：")
				.append(step.get("description"))
				.append("\n");
		}
		return builder.toString();
	}

}
