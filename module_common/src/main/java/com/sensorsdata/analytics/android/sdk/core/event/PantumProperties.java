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
	private String extra;
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
	/** 打印机pid */
	private String pid;
	/** 打印机sn */
	private String sn;
	/** 设备品牌 */
	private String deviceBrand;
	/** 设备型号 */
	private String deviceModel;
	/** 客户端版本 */
	private String clientVersion;
	/** 链路id, 应用冷启动时重置 */
	private String traceId;
	/** 系统版本  ANDROID 11 */
	private String systemVersion;
	/** 打印机的唯一标识 printName(model)_serverName（主机名） */
	private String printerId;
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

	public String getExtra() {
		return extra;
	}

	public PantumProperties setExtra(JSONObject extra) {
		if (extra == null) {
			this.extra = null;
		} else {
			this.extra = extra.toString();
		}
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

	public String getPid() {
		return pid;
	}

	public PantumProperties setPid(String pid) {
		this.pid = pid;
		return this;
	}

	public String getSn() {
		return sn;
	}

	public PantumProperties setSn(String sn) {
		this.sn = sn;
		return this;
	}

	public String getPrinterId() {
		return printerId;
	}

	public PantumProperties setPrinterId(String printerId) {
		this.printerId = printerId;
		return this;
	}

	public String getDeviceBrand() {
		return deviceBrand;
	}

	public PantumProperties setDeviceBrand(String deviceBrand) {
		this.deviceBrand = deviceBrand;
		return this;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public PantumProperties setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
		return this;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public PantumProperties setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
		return this;
	}

	public String getTraceId() {
		return traceId;
	}

	public PantumProperties setTraceId(String traceId) {
		this.traceId = traceId;
		return this;
	}

	public String getSystemVersion() {
		return systemVersion;
	}

	public PantumProperties setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
		return this;
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("traceId",getTraceId());
			jsonObject.put("source", getSource());
			jsonObject.put("subSource", getSubSource());
			jsonObject.put("actionType", getActionType());
			jsonObject.put("userId", getUserId());
			jsonObject.put("deviceType", getDeviceType());
			jsonObject.put("deviceId",getDeviceId());
			jsonObject.put("deviceBrand",getDeviceBrand());
			jsonObject.put("deviceModel",getDeviceModel());
			jsonObject.put("clientVersion",getClientVersion());
			jsonObject.put("systemVersion",getSystemVersion());
			jsonObject.put("sn",getSn());
			jsonObject.put("pid",getPid());
			jsonObject.put("printerId",getPrinterId());
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
