package com.cr.deepresearch.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.cr.deepresearch.node.CoordinatorNode;
import com.cr.deepresearch.node.PlannerNode;
import com.cr.deepresearch.node.ReporterNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Configuration
public class DeepResearchGraphConfig {

	@Bean
	public CompiledGraph deepResearchGraph(
			CoordinatorNode coordinatorNode,
			PlannerNode plannerNode,
			ReporterNode reporterNode) throws GraphStateException {
		StateGraph graph = new StateGraph("phase-one-deepresearch", keyStrategyFactory())
				.addNode("coordinator", node_async(coordinatorNode))
				.addNode("planner", node_async(plannerNode))
				.addNode("reporter", node_async(reporterNode))
				.addEdge(START, "coordinator")
				.addEdge("coordinator", "planner")
				.addEdge("planner", "reporter")
				.addEdge("reporter", END);

		return graph.compile(CompileConfig.builder().recursionLimit(20).build());
	}

	private KeyStrategyFactory keyStrategyFactory() {
		ReplaceStrategy replace = new ReplaceStrategy();
		return new KeyStrategyFactoryBuilder()
				.addStrategy("session_id", replace)
				.addStrategy("thread_id", replace)
				.addStrategy("query", replace)
				.addStrategy("enable_deepresearch", replace)
				.addStrategy("max_step_num", replace)
				.addStrategy("coordinator_content", replace)
				.addStrategy("current_plan", replace)
				.addStrategy("planner_content", replace)
				.addStrategy("final_report", replace)
				.addStrategy("errors", replace)
				.defaultStrategy(replace)
				.build();
	}

}
