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

package com.huawei.hms.cordova.account.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.huawei.hms.common.api.CommonStatusCodes;
import com.huawei.hms.cordova.account.exceptions.TimeOutException;
import com.huawei.hms.cordova.account.logger.HMSLogger;
import com.huawei.hms.cordova.account.utils.ExceptionUtils;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.sms.common.ReadSmsConstant;

import org.apache.cordova.CallbackContext;

import java.util.Objects;


public class SMSBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = SMSBroadcastReceiver.class.getSimpleName();
    public CallbackContext cb;
    private HMSLogger logger;
    private ExceptionUtils exceptions;

    public SMSBroadcastReceiver(HMSLogger logger, CallbackContext callbackContext) {
        cb = callbackContext;
        this.logger = logger;
        exceptions = new ExceptionUtils(logger);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && intent.hasExtra(ReadSmsConstant.EXTRA_STATUS)) {
            Status status = extras.getParcelable(ReadSmsConstant.EXTRA_STATUS);
            if (Objects.requireNonNull(status).getStatusCode() == CommonStatusCodes.TIMEOUT) {
                Log.i(TAG, exceptions.getErrorMessage(new TimeOutException()).toString());
                cb.error(exceptions.logAndGetErrorJSON("smsVerificationCode", new TimeOutException(), "periodic"));
            } else if (Objects.requireNonNull(status).getStatusCode() == CommonStatusCodes.SUCCESS) {
                if (extras.containsKey(ReadSmsConstant.EXTRA_SMS_MESSAGE)) {
                    String message = extras.getString(ReadSmsConstant.EXTRA_SMS_MESSAGE);
                    cb.success(message);
                    logger.sendPeriodicEvent("smsVerificationCode");
                }
            }
        }
    }
}
