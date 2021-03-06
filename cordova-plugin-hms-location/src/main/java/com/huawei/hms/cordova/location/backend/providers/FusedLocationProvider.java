/*
Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.huawei.hms.cordova.location.backend.providers;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.cordova.location.backend.logger.HMSLogger;
import com.huawei.hms.cordova.location.backend.logger.HMSMethod;
import com.huawei.hms.cordova.location.backend.utils.LocationUtils;
import com.huawei.hms.cordova.location.backend.utils.PermissionUtils;
import com.huawei.hms.cordova.location.backend.utils.PlatformUtils;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationEnhanceService;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.NavigationRequest;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.cordova.location.backend.helpers.Constants;
import com.huawei.hms.cordova.location.backend.helpers.Exceptions;
import com.huawei.hms.cordova.location.backend.helpers.LocationCallbackWithHandler;
import com.huawei.hms.cordova.location.backend.interfaces.HMSCallback;
import com.huawei.hms.cordova.location.backend.interfaces.HMSProvider;
import com.huawei.hms.cordova.location.backend.interfaces.ResultHandler;
import com.huawei.hms.cordova.location.backend.interfaces.TriMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.huawei.hms.cordova.location.backend.helpers.Exceptions.ERR_DUPLICATE_ID;
import static com.huawei.hms.cordova.location.backend.helpers.Exceptions.ERR_EMPTY_CALLBACK;

public class FusedLocationProvider extends HMSProvider implements ResultHandler {
    protected static final String TAG = FusedLocationProvider.class.getSimpleName();

    private HMSCallback permissionResultCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<String, LocationCallback> locationCallbackMap;

    private SettingsClient settingsClient;
    private LocationEnhanceService locationEnhanceService;

    public FusedLocationProvider(Context ctx) {
        super(ctx);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        settingsClient = LocationServices.getSettingsClient(getContext());
        locationEnhanceService = LocationServices.getLocationEnhanceService(getContext());
        locationCallbackMap = new HashMap<>();
    }

    @Override
    public JSONObject getConstants() throws JSONException {
        final JSONObject priorityConstants = new JSONObject();
        priorityConstants.put("PRIORITY_BALANCED_POWER_ACCURACY", LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        priorityConstants.put("PRIORITY_HIGH_ACCURACY", LocationRequest.PRIORITY_HIGH_ACCURACY);
        priorityConstants.put("PRIORITY_LOW_POWER", LocationRequest.PRIORITY_LOW_POWER);
        priorityConstants.put("PRIORITY_NO_POWER", LocationRequest.PRIORITY_NO_POWER);

        final JSONObject eventConstants = new JSONObject();
        eventConstants.put("SCANNING_RESULT", Constants.Event.SCANNING_RESULT.getVal());

        final JSONObject navigationRequestConstants = new JSONObject();
        navigationRequestConstants.put("OVERPASS", NavigationRequest.OVERPASS);
        navigationRequestConstants.put("IS_SUPPORT_EX", NavigationRequest.IS_SUPPORT_EX);

        final JSONObject constants = new JSONObject();
        constants.put("PriorityConstants", priorityConstants);
        constants.put("Events", eventConstants);
        constants.put("NavigationRequestConstants", navigationRequestConstants);

        return constants;
    }

    // @ExposedMethod
    public void flushLocations(final HMSCallback callback) {
        Log.i(TAG, "flushLocations begin");
        HMSMethod method = new HMSMethod("flushLocations");

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .flushLocations()
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));
    }

    // @ExposedMethod
    public void checkLocationSettings(final JSONObject locationRequestMap, final HMSCallback callback) {
        Log.i(TAG, "checkLocationSettings begin");
        HMSMethod method = new HMSMethod("checkLocationSettings");
        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        settingsClient
                .checkLocationSettings(LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_SETTINGS_REQUEST.map(locationRequestMap))
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        LocationUtils.FROM_LOCATION_SETTINGS_STATES_RESPONSE_TO_JSON_OBJECT))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));
        Log.i(TAG, "checkLocationSettings end");
    }

    // @ExposedMethod
    public void getNavigationContextState(int requestType, final HMSCallback callback) {
        Log.i(TAG, "getNavigationContextState begin");
        HMSMethod method = new HMSMethod("getNavigationContextState");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        locationEnhanceService.getNavigationState(new NavigationRequest(requestType))
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        LocationUtils.FROM_NAVIGATION_RESULT_TO_JSON_OBJECT))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "getNavigationContextState end");
    }

    // @ExposedMethod
    public void getLastLocation(final HMSCallback callback) {
        Log.i(TAG, "getLastLocation begin");
        HMSMethod method = new HMSMethod("getLastLocation");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .getLastLocation()
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        LocationUtils.FROM_LOCATION_TO_JSON_OBJECT))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "getLastLocation end");
    }

    // @ExposedMethod
    public void getLastLocationWithAddress(final JSONObject map, final HMSCallback callback) {
        Log.i(TAG, "getLastLocationWithAddress begin");
        HMSMethod method = new HMSMethod("getLastLocationWithAddress");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .getLastLocationWithAddress(LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_REQUEST.map(map))
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        LocationUtils.FROM_HW_LOCATION_TO_JSON_OBJECT))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "getLastLocationWithAddress end");
    }

    // @ExposedMethod
    public void getLocationAvailability(final HMSCallback callback) {
        HMSMethod method = new HMSMethod("getLocationAvailability");
        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        Log.i(TAG, "getLocationAvailability begin");
        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        fusedLocationProviderClient
                .getLocationAvailability()
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        LocationUtils.FROM_LOCATION_AVAILABILITY_TO_JSON_OBJECT))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "getLocationAvailability end");
    }

    // @ExposedMethod
    public void setMockLocation(JSONObject map, final HMSCallback callback) {
        Log.i(TAG, "setMockLocation begin");
        HMSMethod method = new HMSMethod("setMockLocation");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        Location location = new Location("HMS-MOCK");
        location.setLongitude(map.optDouble("longitude"));
        location.setLatitude(map.optDouble("latitude"));

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .setMockLocation(location)
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "setMockLocation end");
    }

    // @ExposedMethod
    public void setMockMode(final boolean shouldMock, final HMSCallback callback) {
        Log.i(TAG, "setMockMode -> shouldMock=" + shouldMock);
        HMSMethod method = new HMSMethod("setMockMode");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .setMockMode(shouldMock)
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "setMockMode end");
    }

    // @ExposedMethod
    public void requestLocationUpdates(final JSONObject json, final HMSCallback callback) {
        HMSMethod method = new HMSMethod("requestLocationUpdates", true);
        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        requestLocationUpdatesGeneric(method, fusedLocationProviderClient::requestLocationUpdates, json, callback);
    }

    // @ExposedMethod
    public void requestLocationUpdatesEx(final JSONObject json, final HMSCallback callback) {
        HMSMethod method = new HMSMethod("requestLocationUpdatesEx", true);
        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        requestLocationUpdatesGeneric(method, fusedLocationProviderClient::requestLocationUpdatesEx, json,
                callback);
    }

    // @ExposedMethod
    public void removeLocationUpdates(final String id, final HMSCallback callback) {
        LocationCallback locationCallback = locationCallbackMap.get(id);
        HMSMethod method = new HMSMethod("removeLocationUpdates", true);

        if (locationCallback == null) {
            Log.i(TAG, "removeLocationUpdates callback is null");
            callback.error(Exceptions.toErrorJSON(ERR_EMPTY_CALLBACK));
            return;
        }

        HMSLogger.getInstance(getActivity()).startMethodExecutionTimer(method.getName());
        fusedLocationProviderClient
                .removeLocationUpdates(locationCallback)
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));
    }

    // @ExposedMethod
    public void requestPermission(HMSCallback callback) {
        PermissionUtils.requestLocationPermission(this);
        permissionResultCallback = callback;
    }

    // @ExposedMethod
    public void hasPermission(final HMSCallback callback) {
        boolean result = PermissionUtils.hasLocationPermission(this);
        callback.success(PlatformUtils.keyValPair("hasPermission", result));
    }

    public void handleResult(Location location) {
        JSONObject params = LocationUtils.FROM_LOCATION_TO_JSON_OBJECT.map(location);
        getEventSender().send(Constants.Event.SCANNING_RESULT.getVal(), params);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        JSONObject json = PermissionUtils.HANDLE_PERMISSION_REQUEST_RESULT.map(requestCode, permissions, grantResults);
        if (permissionResultCallback != null) {
            permissionResultCallback.success(json);
        } else {
            Log.e(TAG, "onRequestPermissionResult() :: permissionResultCallback is null");
        }
    }

    private void requestLocationUpdatesGeneric(HMSMethod method, TriMapper<LocationRequest, LocationCallback, Looper,
            Task<Void>> requestMethod, final JSONObject json, final HMSCallback callback) {
        Log.i(TAG, "requestLocationUpdatesWithCallback start");

        if (LocationUtils.checkForObstacles(this, fusedLocationProviderClient, callback)) {
            return;
        }

        final String id = json.optString("id");
        final LocationRequest locationRequest = LocationUtils.FROM_JSON_OBJECT_TO_LOCATION_REQUEST.map(json);

        if (locationCallbackMap.get(id) != null) {
            Log.i(TAG, "requestLocationUpdatesWithCallback: this callback id already exists");
            callback.error(Exceptions.toErrorJSON(ERR_DUPLICATE_ID));
            return;
        }

        // Create locationCallback
        LocationCallback locationCallback = new LocationCallbackWithHandler(this);
        locationCallbackMap.put(id, locationCallback);

        requestMethod.map(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnSuccessListener(PlatformUtils.successListener(method, getActivity(), callback,
                        PlatformUtils.keyValPair("requestCode", id)))
                .addOnFailureListener(PlatformUtils.failureListener(method, getActivity(), callback));

        Log.i(TAG, "call requestLocationUpdatesWithCallback success.");
    }
}
