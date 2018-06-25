package com.accenture.aitp.tailor.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;


public class ModelMonitoredInfo extends VelocityContext {
	private String itemType;
	private String pk;
	private ModelOperatorEnum modifyType;

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String typeCode) {
		this.put("itemType", typeCode);
		this.itemType = typeCode;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.put("pk", pk);
		this.pk = pk;
	}

	public String toString() {
		return this.getItemType() + " pk:" + this.getPk();
	}

	public ModelOperatorEnum getModifyType() {
		return modifyType;
	}

	public void setModifyType(ModelOperatorEnum modifyType) {
		this.put("modifyType", modifyType);
		this.modifyType = modifyType;
	}
}
