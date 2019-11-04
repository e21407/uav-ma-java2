package com.lbc.ma.structure;
/**
 * 将代码改成适应动态情况时候新增的类，给工作流新增了一个时间属性描述工作流执行的具体时长
 * @author liubaichuan
 * @since 2018-10-11
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Workflow {

	public Integer workflowId;

	public ArrayList<Flow> flows;

	public Workflow(Integer wF_ID, ArrayList<Flow> flows) {
		super();
		workflowId = wF_ID;
		if (flows == null) {
			this.flows = new ArrayList<Flow>();
		} else {
			this.flows = flows;
		}
	}

	public boolean addFlow(Flow f) {
		if (f == null)
			return false;
		flows.add(f);
		return true;
	}

	public Set<Task> getTasks(){
		Set<Task> result = new HashSet<>();
		for(Flow flows : flows){
			result.add(flows.currTask);
			result.add(flows.succTask);
		}
		return result;
	}

}
