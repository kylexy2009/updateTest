package com.sureserve.update;

import java.io.Serializable;

public class UpdateInfo implements Serializable {
	String state;
	String appVision;
	String appInfo;
	String url;
	String appPath;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAppVision() {
		return appVision;
	}

	public void setAppVision(String appVision) {
		this.appVision = appVision;
	}

	public String getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(String appInfo) {
		this.appInfo = appInfo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}
}
