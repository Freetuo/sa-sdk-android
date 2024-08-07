/*
 * Created by dengshiwei on 2022/06/15.
 * Copyright 2015－2021 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.analytics.android.sdk.core.event.imp;

import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.core.SAContextManager;
import com.sensorsdata.analytics.android.sdk.core.business.timer.EventTimer;
import com.sensorsdata.analytics.android.sdk.core.business.timer.EventTimerManager;
import com.sensorsdata.analytics.android.sdk.core.event.Event;
import com.sensorsdata.analytics.android.sdk.core.event.InputData;
import com.sensorsdata.analytics.android.sdk.core.event.PantumProperties;
import com.sensorsdata.analytics.android.sdk.core.event.TrackEvent;
import com.sensorsdata.analytics.android.sdk.data.persistent.PersistentLoader;
import com.sensorsdata.analytics.android.sdk.internal.beans.EventType;
import com.sensorsdata.analytics.android.sdk.pantumcontant.ActionType;
import com.sensorsdata.analytics.android.sdk.plugin.property.SAPropertyPlugin;
import com.sensorsdata.analytics.android.sdk.plugin.property.beans.SAPropertiesFetcher;
import com.sensorsdata.analytics.android.sdk.plugin.property.beans.SAPropertyFilter;
import com.sensorsdata.analytics.android.sdk.plugin.property.impl.InternalCustomPropertyPlugin;
import com.sensorsdata.analytics.android.sdk.util.AppInfoUtils;
import com.sensorsdata.analytics.android.sdk.util.AppStateTools;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;
import com.sensorsdata.analytics.android.sdk.util.SADataHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.UUID;

class TrackEventAssemble extends BaseEventAssemble {
    private static final String TAG = "SA.TrackEventAssemble";
    private final SAContextManager mContextManager;
    public TrackEventAssemble(SAContextManager saContextManager) {
        super(saContextManager);
        mContextManager = saContextManager;
    }

    @Override
    public Event assembleData(InputData input) {
        try {
            EventType eventType = input.getEventType();

            JSONObject properties = null;
            if (!input.isPantum()) {
                properties = JSONUtils.cloneJsonObject(input.getProperties());
            }
            if (properties == null) {
                properties = new JSONObject();
            }

            if (isEventIgnore(input.getEventName(), eventType, mContextManager)) {
                return null;
            }

            TrackEvent trackEvent = new TrackEvent();
            trackEvent.setProperties(properties);
            appendDefaultProperty(input, trackEvent);
            appendEventDuration(trackEvent);
            appendLibProperty(eventType, trackEvent);
            appendUserIDs(input, trackEvent);
            appendSessionId(eventType, trackEvent);
            appendPluginProperties(eventType, input, trackEvent);
            handlePropertyProtocols(trackEvent);
            if (!handleEventCallback(eventType, trackEvent)) {
                return null;
            }
            appendPluginVersion(eventType, trackEvent);
            // 单独处理pantum数据
            if (!handlePantumProperty(input, eventType, trackEvent)){
                return null;
            }
            SADataHelper.assertPropertyTypes(trackEvent.getProperties());
            handleEventListener(eventType, trackEvent, mContextManager);
            if (SALog.isLogEnabled()) {
                SALog.i(TAG, "track event:\n" + JSONUtils.formatJson(trackEvent.toJSONObject().toString()));
            }
            return trackEvent;
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
        return null;
    }

    /**
     * 处理奔图数据
     * @param input
     * @param eventType
     * @param trackEvent
     * @return 是否是奔图数据且成功处理
     */
    private boolean handlePantumProperty(InputData input, EventType eventType, TrackEvent trackEvent) {
        String eventName = trackEvent.getEventName();
        // 是否继续处理
        boolean continueHandle = "$AppStart".equals(eventName) ||
                "$AppEnd".equals(eventName) ||
                input.isPantum();
        if (!continueHandle) {
            SALog.i(TAG, "handlePantumProperty, Is a not event!");
            return false;
        }

        JSONObject properties = input.getProperties();
        JSONObject sysProperties = trackEvent.getProperties();
        String deviceId = sysProperties.optString("$device_id");
        String deviceBrand = sysProperties.optString("$brand");
        String deviceModel = sysProperties.optString("$model");
        String appVersion = sysProperties.optString("$app_version");
        String os = sysProperties.optString("$os").toUpperCase();
        String osVersion = sysProperties.optString("$os_version");
        /* userId、 sn、pid、deviceName、printerId 通过动态属性获取 */
        long userId = sysProperties.optLong("userId");
        String sn = sysProperties.optString("sn");
        String pid = sysProperties.optString("pid");
        String printerId = sysProperties.optString("printerId");
        String deviceName = sysProperties.optString("deviceName");
        /* 以下属性需要特殊处理 */
        String actionType;
        String source;
        String subSource;
        String traceId = SensorsDataAPI.sharedInstance().getPtTraceId();
        JSONObject extra = null;
        if ("$AppStart".equals(eventName)) {
            boolean resumeFromBackground = sysProperties.optBoolean("$resume_from_background");
            if (resumeFromBackground) {
                source = "SWITCH";
            } else {
                source = "NEW";
                // 只有当发生AppStart事件，且是冷启动时才去重置traceId
                traceId = UUID.randomUUID().toString();
                SensorsDataAPI.sharedInstance().setPtTraceId(traceId);
            }
            actionType = ActionType.LAUNCH;
            subSource = "";
            try {
                if (input.getProperties().has("extra")) {
                    extra = input.getProperties().getJSONObject("extra");
                } else {
                    extra = new JSONObject();
                }
                String screenWidth = sysProperties.optString("$screen_width");
                String screenHeight = sysProperties.optString("$screen_height");
                extra.put("screenResolution", screenWidth + "*" + screenHeight);
                if (deviceName.length() > 0) {
                    extra.put("deviceName", deviceName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("$AppEnd".equals(eventName)) {
            actionType = ActionType.EXIT;
            String eventTraceId = sysProperties.optString("pt_trace_id");
            /* 如果事件携带的traceId等于当前的traceId, 则认为是HIDE，否则为KILLED。
               这里的判断依赖于：
               冷启动先产生了AppStart, 生成了新的traceId, 再产生补发的AppEnd事件, 补发的AppEnd的traceId不等于新的traceId；
               从后台切换回来，先产生补发的AppEnd事件，再产生AppStart事件, 没有新的traceId产生。
            */
            if (eventTraceId.equals(traceId)) {
                source = "HIDE";
            } else {
                source = "KILLED";
            }
            traceId = eventTraceId;
            subSource = "";
            try {
                if (input.getProperties().has("extra")) {
                    extra = input.getProperties().getJSONObject("extra");
                } else {
                    extra = new JSONObject();
                }
                int duration = (int) (sysProperties.optDouble("event_duration") * 1000);
                extra.put("duration", duration);
                if (deviceName.length() > 0) {
                    extra.put("deviceName", deviceName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            /* 由埋点方法调用处参数传入 */
            actionType = properties.optString("actionType");
            source = properties.optString("source");
            subSource = properties.optString("subSource");
            try {
                if (actionType.equals(ActionType.TIME)) {
                    if (sysProperties.has("event_duration")) {
                        int duration = (int) (sysProperties.optDouble("event_duration") * 1000);
                        if (duration <= 0) {
                            SALog.i(TAG, "source:" + source + " subSource:" + subSource + "event_duration is invalid, not handle event");
                            return false;
                        }
                        if (input.getProperties().has("extra")) {
                            extra = input.getProperties().getJSONObject("extra");
                        } else {
                            extra = new JSONObject();
                        }
                        extra.put("duration", duration);
                        if (deviceName.length() > 0) {
                            extra.put("deviceName", deviceName);
                        }
                    } else {
                        SALog.i(TAG, "source:" + source + " subSource:" + subSource + "is time event, but not find event_duration");
                        return false;
                    }
                } else {
                    if (deviceName.length() > 0) {
                        if (input.getProperties().has("extra")) {
                            extra = input.getProperties().getJSONObject("extra");
                        } else {
                            extra = new JSONObject();
                        }
                        extra.put("deviceName", deviceName);
                    } else {
                        if (input.getProperties().has("extra")) {
                            extra = input.getProperties().getJSONObject("extra");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        PantumProperties pantumProperties = new PantumProperties();
        pantumProperties
                .setSource(source)
                .setSubSource(subSource)
                .setUserId(userId)
                .setDeviceId(deviceId)
                .setDeviceType(os)
                .setDeviceBrand(deviceBrand)
                .setDeviceModel(deviceModel)
                .setSystemVersion(os + " " + osVersion)
                .setActionType(actionType)
                .setSn(sn)
                .setPid(pid)
                .setPrinterId(printerId)
                .setClientVersion(appVersion)
                .setTraceId(traceId)
                .setExtra(extra)
                .setReportTime(trackEvent.getTime());
        trackEvent.setPantumProperties(pantumProperties.toJSONObject());
        SALog.i(TAG, "handlePantumProperty, " + trackEvent.getPantumProperties().toString());
        return true;
    }

    private boolean isEventIgnore(String eventName, EventType eventType, SAContextManager contextManager) {
        if (eventType.isTrack()) {
            SADataHelper.assertEventName(eventName);
            //如果在线控制禁止了事件，则不触发
            return !TextUtils.isEmpty(eventName) && contextManager.getRemoteManager() != null &&
                    contextManager.getRemoteManager().ignoreEvent(eventName);
        }
        return false;
    }

    private void appendDefaultProperty(InputData inputData, TrackEvent trackEvent) {
        try {
            trackEvent.setTime(inputData.getTime());
            trackEvent.setEventName(inputData.getEventName());
            trackEvent.setType(inputData.getEventType().getEventType());
            SecureRandom secureRandom = new SecureRandom();
            trackEvent.setTrackId(secureRandom.nextInt());
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private void appendEventDuration(TrackEvent trackEvent) {
        try {
            String eventName = trackEvent.getEventName();
            if (!TextUtils.isEmpty(eventName)) {
                EventTimer eventTimer = EventTimerManager.getInstance().getEventTimer(eventName);
                if (eventTimer != null) {
                    float duration = eventTimer.duration();
                    if (duration > 0) {
                        SALog.i(TAG, "event_duration = " + duration);
                        trackEvent.getProperties().put("event_duration", Float.valueOf(duration));
                        // trackEvent.getProperties().put("event_duration", Math.round(duration));
                    }
                }
                if (eventName.endsWith("_SATimer") && eventName.length() > 45) {// Timer 计时交叉计算拼接的字符串长度 45
                    eventName = eventName.substring(0, eventName.length() - 45);
                    trackEvent.setEventName(eventName);
                    SALog.i(TAG, "trigger event name = " + eventName);
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private void appendLibProperty(EventType eventType, TrackEvent trackEvent) throws JSONException {
        JSONObject libProperties = new JSONObject();
        JSONObject propertyJson = trackEvent.getProperties();
        String libDetail = null;
        if (propertyJson != null) {
            if (eventType.isTrack()) {
                String libMethod = trackEvent.getProperties().optString("$lib_method", "code");
                libProperties.put("$lib_method", libMethod);
                propertyJson.put("$lib_method", libMethod);
            } else {
                libProperties.put("$lib_method", "code");
            }
            // replace $lib_detail
            if (propertyJson.has("$lib_detail")) {
                libDetail = propertyJson.optString("$lib_detail");
                propertyJson.remove("$lib_detail");
            }
        } else {
            libProperties.put("$lib_method", "code");
            if (eventType.isTrack()) {
                propertyJson = new JSONObject();
                propertyJson.put("$lib_method", "code");
            }
        }
        libProperties.put("$lib", "Android");
        libProperties.put("$lib_version", mContextManager.getSensorsDataAPI().getSDKVersion());
        libProperties.put("$app_version", AppInfoUtils.getAppVersionName(mContextManager.getContext()));
        //update lib $app_version from super properties
        JSONObject superProperties = PersistentLoader.getInstance().getSuperPropertiesPst().get();
        if (superProperties != null) {
            if (superProperties.has("$app_version")) {
                libProperties.put("$app_version", superProperties.get("$app_version"));
            }
        }

        if (mContextManager.getSensorsDataAPI().isAutoTrackEnabled()
                && propertyJson != null && isAutoTrackType(trackEvent.getEventName())) {
            SensorsDataAPI.AutoTrackEventType trackEventType = autoTrackEventTypeFromEventName(trackEvent.getEventName());
            if (trackEventType != null && !mContextManager.getSensorsDataAPI().isAutoTrackEventTypeIgnored(trackEventType)
                    && trackEvent.getProperties().has("$screen_name")) {
                String screenName = propertyJson.getString("$screen_name");
                if (!TextUtils.isEmpty(screenName)) {
                    String[] screenNameArray = screenName.split("\\|");
                    if (screenNameArray.length > 0) {
                        libDetail = String.format("%s##%s##%s##%s", screenNameArray[0], "", "", "");
                    }
                }
            }
        }

        if (TextUtils.isEmpty(libDetail)) {
            StackTraceElement[] trace = (new Exception()).getStackTrace();
            if (trace.length > 1) {
                StackTraceElement traceElement = trace[0];
                libDetail = String.format("%s##%s##%s##%s", traceElement
                                .getClassName(), traceElement.getMethodName(), traceElement.getFileName(),
                        traceElement.getLineNumber());
            }
        }

        libProperties.put("$lib_detail", libDetail);
        trackEvent.setLib(libProperties);
        trackEvent.setProperties(propertyJson);
    }

    private void appendPluginProperties(EventType eventType, InputData input, TrackEvent trackEvent) throws JSONException {
        JSONObject properties = input.getProperties();

        SAPropertyFilter filter = new SAPropertyFilter();
        filter.setEvent(trackEvent.getEventName());
        filter.setTime(trackEvent.getTime());
        filter.setEventJson(SAPropertyFilter.LIB, trackEvent.getLib());
        filter.setEventJson(SAPropertyFilter.IDENTITIES, new JSONObject(trackEvent.getIdentities().toString()));
        filter.setProperties(trackEvent.getProperties());
        filter.setType(eventType);
        if (!input.isPantum()) {
            // custom properties from user
            SAPropertyPlugin customPlugin = mContextManager.getPluginManager().getPropertyPlugin(InternalCustomPropertyPlugin.class.getName());
            if (customPlugin instanceof InternalCustomPropertyPlugin) {
                ((InternalCustomPropertyPlugin) customPlugin).saveCustom(properties);
            }
        }

        SAPropertiesFetcher propertiesFetcher = mContextManager.getPluginManager().propertiesHandler(filter);
        if (propertiesFetcher != null) {
            trackEvent.setProperties(propertiesFetcher.getProperties());
            trackEvent.setLib(propertiesFetcher.getEventJson(SAPropertyFilter.LIB));
        }
    }

    private void appendUserIDs(InputData inputData, TrackEvent trackEvent) throws JSONException {
        String distinctId = mContextManager.getUserIdentityAPI().getDistinctId();
        String loginId = mContextManager.getUserIdentityAPI().getLoginId();
        String anonymousId = mContextManager.getUserIdentityAPI().getAnonymousId();
        try {
            //针对 SF 弹窗展示事件特殊处理
            if ("$PlanPopupDisplay".equals(trackEvent.getEventName())) {
                if (trackEvent.getProperties().has("$sf_internal_anonymous_id")) {
                    anonymousId = trackEvent.getProperties().optString("$sf_internal_anonymous_id");
                    trackEvent.getProperties().remove("$sf_internal_anonymous_id");
                    inputData.getProperties().remove("$sf_internal_anonymous_id");
                }

                if (trackEvent.getProperties().has("$sf_internal_login_id")) {
                    loginId = trackEvent.getProperties().optString("$sf_internal_login_id");
                    trackEvent.getProperties().remove("$sf_internal_login_id");
                    inputData.getProperties().remove("$sf_internal_login_id");
                }
                if (!TextUtils.isEmpty(loginId)) {
                    distinctId = loginId;
                } else {
                    distinctId = anonymousId;
                }
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }

        trackEvent.setDistinctId(distinctId);
        if (!TextUtils.isEmpty(loginId)) {
            trackEvent.setLoginId(loginId);
        }
        trackEvent.setAnonymousId(anonymousId);
        EventType eventType = inputData.getEventType();
        trackEvent.setIdentities(mContextManager.getUserIdentityAPI().getIdentities(eventType));
        if (eventType == EventType.TRACK || eventType == EventType.TRACK_ID_BIND || eventType == EventType.TRACK_ID_UNBIND) {
            //是否首日访问
            trackEvent.getProperties().put("$is_first_day", mContextManager.isFirstDay(trackEvent.getTime()));
        } else if (eventType == EventType.TRACK_SIGNUP) {
            trackEvent.setOriginalId(trackEvent.getAnonymousId());
        }
    }

    static boolean isAutoTrackType(String eventName) {
        if (!TextUtils.isEmpty(eventName)) {
            switch (eventName) {
                case "$AppStart":
                case "$AppEnd":
                case "$AppClick":
                case "$AppViewScreen":
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    static SensorsDataAPI.AutoTrackEventType autoTrackEventTypeFromEventName(String eventName) {
        if (TextUtils.isEmpty(eventName)) {
            return null;
        }

        switch (eventName) {
            case "$AppStart":
                return SensorsDataAPI.AutoTrackEventType.APP_START;
            case "$AppEnd":
                return SensorsDataAPI.AutoTrackEventType.APP_END;
            case "$AppClick":
                return SensorsDataAPI.AutoTrackEventType.APP_CLICK;
            case "$AppViewScreen":
                return SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN;
            default:
                break;
        }

        return null;
    }
}
