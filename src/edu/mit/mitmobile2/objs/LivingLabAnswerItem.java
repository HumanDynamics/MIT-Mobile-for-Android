package edu.mit.mitmobile2.objs;

import java.io.Serializable;

import org.json.JSONObject;

public class LivingLabAnswerItem extends LivingLabDataItem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6309488714474711269L;

	public LivingLabAnswerItem(JSONObject answerJson) {
		super(answerJson);
		// NOTE: relying on the factory class to fill in the dependencies - no point in duplicating code
	}
}
