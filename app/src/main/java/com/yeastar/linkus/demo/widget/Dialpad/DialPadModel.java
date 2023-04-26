package com.yeastar.linkus.demo.widget.Dialpad;

public class DialPadModel {
	private String mainText = null;
	private String subText = null;

	public DialPadModel(String mainText, String subText) {
		this.mainText = mainText;
		this.subText = subText;
	}

	public String getMainText() {
		return mainText;
	}

	public void setMainText(String mainText) {
		this.mainText = mainText;
	}

	public String getSubText() {
		if(subText == null) {
			return null;
		}
		return subText.trim();
	}

	public void setSubText(String subText) {
		this.subText = subText;
	}

}
