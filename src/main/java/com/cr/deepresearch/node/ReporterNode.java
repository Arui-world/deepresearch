package com.cr.deepresearch.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ReporterNode implements NodeAction {

	@Override
	public Map<String, Object> apply(OverAllState state) {
		String query = state.value("query", "");
		String coordinatorContent = state.value("coordinator_content", "");
		String plannerContent = state.value("planner_content", "");

		String finalReport = buildReport(query, coordinatorContent, plannerContent);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("final_report", finalReport);
		return result;
	}

	private String buildReport(String query, String coordinatorContent, String plannerContent) {
		StringBuilder builder = new StringBuilder();
		builder.append("# DeepResearch 阶段一简版报告\n\n");
		builder.append("## 用户问题\n\n");
		builder.append(query.isBlank() ? "未提供有效问题。" : query).append("\n\n");
		builder.append("## 执行状态\n\n");
		builder.append("- ").append(coordinatorContent).append("\n");
		builder.append("- 已完成最小 Graph 闭环：coordinator -> planner -> reporter。\n");
		builder.append("- 阶段一未启用联网搜索、RAG、多 Agent 并行和 Python 沙箱。\n\n");
		builder.append("## 研究计划\n\n");
		if (plannerContent.isBlank()) {
			builder.append("当前没有生成有效计划。\n\n");
		}
		else {
			builder.append(plannerContent).append("\n");
		}
		builder.append("## 阶段结论\n\n");
		builder.append("阶段一已经提供可流式输出的后端链路，后续阶段可以在该链路上逐步替换模板节点为真实 Agent、搜索和 RAG 能力。\n");
		return builder.toString();
	}

}
