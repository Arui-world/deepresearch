package com.cr.deepresearch.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CoordinatorNode implements NodeAction {

	@Override
	public Map<String, Object> apply(OverAllState state) {
		String query = state.value("query", "");
		boolean enableDeepresearch = state.value("enable_deepresearch", true);
		String coordinatorContent = enableDeepresearch
				? "已进入 DeepResearch 最小闭环，将生成研究计划并汇总报告。"
				: "已按普通问答模式处理；阶段一仍通过最小闭环生成简要报告。";

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("coordinator_content", coordinatorContent);
		result.put("enable_deepresearch", enableDeepresearch);
		if (query.isBlank()) {
			result.put("errors", "query must not be blank");
		}
		return result;
	}

}
