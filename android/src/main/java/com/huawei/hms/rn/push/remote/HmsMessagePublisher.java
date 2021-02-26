/*
    Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License")
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.huawei.hms.rn.push.remote;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.rn.push.constants.Core;
import com.huawei.hms.rn.push.utils.RemoteMessageUtils;

import java.util.Timer;
import java.util.TimerTask;

public class HmsMessagePublisher extends ReactContextBaseJavaModule {
    private static String TAG = HmsMessagePublisher.class.getSimpleName();
    private static volatile ReactApplicationContext context;

    public HmsMessagePublisher(ReactApplicationContext reactContext) {

        super(reactContext);
        setContext(reactContext);
    }


    @Override
    public String getName() {

        return TAG;
    }

    public static ReactApplicationContext getContext() {
        return context;
    }

    public static void setContext(ReactApplicationContext context) {
        HmsMessagePublisher.context = context;
    }

    public static void sendOnNewTokenEvent(String token) {

        WritableMap params = Arguments.createMap();
        params.putString(Core.Event.Result.TOKEN, token);

        getContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(Core.Event.TOKEN_RECEIVED_EVENT, params);
    }

  public static boolean isInit() {
    return HmsPushMessaging.getContext() != null && HmsPushMessaging.getContext().hasActiveCatalystInstance();
  }

  private static void doPolling(String event, WritableMap params) {
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        if(HmsMessagePublisher.isInit() && HmsPushMessaging.isEventRegistered(event)) {
          Timer pushTimer = new Timer();
          pushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
              HmsPushMessaging.getContext()
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                  .emit(event, params);
            }
          }, 1000);
        }else{
          doPolling(event, params);
        }
      }
    };
    Timer timer = new Timer();
    timer.schedule(task, 1000);
  }

    public static void sendMessageReceivedEvent(RemoteMessage remoteMessage) {

        WritableMap params = Arguments.createMap();
        params.putMap(Core.Event.Result.MSG, RemoteMessageUtils.toWritableMap(remoteMessage));
//        HmsPushMessaging.getContext()
//            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//            .emit(Core.Event.REMOTE_DATA_MESSAGE_RECEIVED, params);
        doPolling(Core.Event.REMOTE_DATA_MESSAGE_RECEIVED, params);
    }

    public static void sendTokenErrorEvent(Exception e) {

        WritableMap params = Arguments.createMap();
        params.putString(Core.Event.Result.EXCEPTION, e.getMessage());
        getContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(Core.Event.ON_TOKEN_ERROR_EVENT, params);
    }

    public static void sendOnMessageSentEvent(String msgId) {

        WritableMap params = Arguments.createMap();
        params.putString(Core.Event.Result.MSG_ID, msgId);

        getContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(Core.Event.ON_PUSH_MESSAGE_SENT, params);
    }

    public static void sendOnMessageSentErrorEvent(String msgId, int errorCode, String errorInfo) {

        WritableMap params = Arguments.createMap();
        params.putString(Core.Event.Result.RESULT, errorCode + "");
        params.putString(Core.Event.Result.MSG_ID, msgId);
        params.putString(Core.Event.Result.RESULT_INFO, errorInfo);

        getContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(Core.Event.ON_PUSH_MESSAGE_SENT_ERROR, params);
    }

    public static void sendOnMessageDeliveredEvent(String msgId, int errorCode, String errorInfo) {

        WritableMap params = Arguments.createMap();
        params.putString(Core.Event.Result.RESULT, errorCode + "");
        params.putString(Core.Event.Result.MSG_ID, msgId);
        params.putString(Core.Event.Result.RESULT_INFO, errorInfo);

        getContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(Core.Event.ON_PUSH_MESSAGE_SENT_DELIVERED, params);
    }
}
