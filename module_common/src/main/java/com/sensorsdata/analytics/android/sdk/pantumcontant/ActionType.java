package com.sensorsdata.analytics.android.sdk.pantumcontant;

/**
 * 该类主要用于 pantum自定义的事件类型
 *
 * @author wwm
 * @version 1.0
 * @since 2023/4/21
 */

public @interface ActionType {
	/** 浏览 */
	String VIEW = "VIEW";
	/** 点击 */
	String CLICK = "CLICK";
	/** 浏览时长 */
	String TIME = "TIME";
	/** 应用启动 */
	String LAUNCH = "LAUNCH";
	/** 应用退出 */
	String EXIT = "EXIT";
	/** 自定义 */
	String CUSTOM = "CUSTOM";
}
