package com.sensorsdata.analytics.android.sdk.core.event;

import com.sensorsdata.analytics.android.sdk.SALog;

import org.json.JSONObject;

/**
 * 该类主要用于 奔图埋点数据
 *
 * @author wwm
 * @version 1.0
 * @since 2023/4/20
 */

public class PantumProperties {
	/** 用户id */
	private long userId;
	/** 设备标识 */
	private String deviceId;
	/** 来源 页面-按钮<br/>(See: 来源类型)
		PRACTICE string 同步练习
		EXAM     string 同步试卷
	*/
	private String source;
	/** 子来源 */
	private String subSource;
	/** 其他信息 json */
	private JSONObject extra;
	/** 事件类型<br/>(See: 事件类型)
		VIEW  string 浏览
		CLICK string 点击
	*/
	private String actionType;
	/** 设备类型
	 * ANDROID 安卓
	 * IOS 苹果
	 * WEB web
	 * WECHAT 微信小程序*/
	private String deviceType;
	/** 上报时间 s */
	private long reportTime;

	public long getUserId() {
		return userId;
	}

	public PantumProperties setUserId(long userId) {
		this.userId = userId;
		return this;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public PantumProperties setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}

	public String getSource() {
		return source;
	}

	public PantumProperties setSource(String source) {
		this.source = source;
		return this;
	}

	public String getSubSource() {
		return subSource;
	}

	public PantumProperties setSubSource(String subSource) {
		this.subSource = subSource;
		return this;
	}

	public JSONObject getExtra() {
		return extra;
	}

	public PantumProperties setExtra(JSONObject extra) {
		this.extra = extra;
		return this;
	}

	public String getActionType() {
		return actionType;
	}

	public PantumProperties setActionType(String actionType) {
		this.actionType = actionType;
		return this;
	}

	public long getReportTime() {
		return reportTime;
	}

	public PantumProperties setReportTime(long reportTime) {
		this.reportTime = reportTime;
		return this;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public PantumProperties setDeviceType(String deviceType) {
		this.deviceType = deviceType;
		return this;
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userId", getUserId());
			jsonObject.put("deviceId",getDeviceId());
			jsonObject.put("source", getSource());
			jsonObject.put("subSource", getSubSource());
			jsonObject.put("actionType", getActionType());
			jsonObject.put("deviceType", getDeviceType());
			jsonObject.put("reportTime", getReportTime());
			if (getExtra() != null) {
				jsonObject.put("extra", getExtra());
			} else {
				jsonObject.put("extra", null);
			}
			return jsonObject;
		} catch (Exception e) {
			SALog.printStackTrace(e);
		}
		return null;
	}
}
