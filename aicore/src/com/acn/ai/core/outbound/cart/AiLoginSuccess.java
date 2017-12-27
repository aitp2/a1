package com.acn.ai.core.outbound.cart;

public interface AiLoginSuccess
{
	void restoreCartAndMerge(String mostRecentSavedCartGuid, String sessionCartGuid);
}
