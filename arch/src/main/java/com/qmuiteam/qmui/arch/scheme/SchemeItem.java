/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qmuiteam.qmui.arch.scheme;

import android.app.Activity;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.QMUILog;

import java.util.HashMap;
import java.util.Map;

public abstract class SchemeItem {
    @Nullable
    private ArrayMap<String, String> mRequired;
    @Nullable
    private String[] mKeysForInt;
    @Nullable
    private String[] mKeysForBool;
    @Nullable
    private String[] mKeysForLong;
    @Nullable
    private String[] mKeysForFloat;
    @Nullable
    private String[] mKeysForDouble;

    public SchemeItem(@Nullable ArrayMap<String, String> required,
                      @Nullable String[] keysForInt,
                      @Nullable String[] keysForBool,
                      @Nullable String[] keysForLong,
                      @Nullable String[] keysForFloat,
                      @Nullable String[] keysForDouble) {
        mRequired = required;
        mKeysForInt = keysForInt;
        mKeysForBool = keysForBool;
        mKeysForLong = keysForLong;
        mKeysForFloat = keysForFloat;
        mKeysForDouble = keysForDouble;
    }

    @Nullable
    public Map<String, SchemeValue> convertFrom(@Nullable Map<String, String> schemeParams) {
        if (schemeParams == null || schemeParams.isEmpty()) {
            return null;
        }

        Map<String, SchemeValue> queryMap = new HashMap<>();
        for(Map.Entry<String, String> param: schemeParams.entrySet()){
            String name = param.getKey();
            String value = param.getValue();
            if(name == null || name.isEmpty()){
                continue;
            }
            try {
                if (contains(mKeysForInt, name)) {
                    queryMap.put(name, new SchemeValue(value, Integer.valueOf(value), Integer.TYPE));
                } else if (QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY.equals(name) || contains(mKeysForBool, name)) {
                    boolean isFalse = "0".equals(value) || "false".equals(value.toLowerCase());
                    queryMap.put(name, new SchemeValue(value, !isFalse, Boolean.TYPE));
                } else if (contains(mKeysForLong, name)) {
                    queryMap.put(name, new SchemeValue(value, Long.valueOf(value), Long.TYPE));
                } else if (contains(mKeysForFloat, name)) {
                    queryMap.put(name, new SchemeValue(value, Float.valueOf(value), Float.TYPE));
                } else if (contains(mKeysForDouble, name)) {
                    queryMap.put(name, new SchemeValue(value, Double.valueOf(value), Double.TYPE));
                } else {
                    queryMap.put(name, new SchemeValue(value, value, String.class));
                }
            } catch (Exception e) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                        "error to parse scheme param: %s = %s", name, value);
            }
        }
        return queryMap;
    }

    private static boolean contains(@Nullable String[] array, @NonNull String key) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (key.equals(array[i])) {
                return true;
            }
        }
        return false;
    }

    // used by generated code(SchemeMapImpl)
    boolean match(@Nullable Map<String, String> scheme) {
        if (mRequired == null || mRequired.isEmpty()) {
            return true;
        }
        if (scheme == null || scheme.isEmpty()) {
            return false;
        }
        for (int i = 0; i < mRequired.size(); i++) {
            String key = mRequired.keyAt(i);
            if(!scheme.containsKey(key)){
                return false;
            }
            String value = mRequired.valueAt(i);
            if(value == null){
                // if no value. that means scheme must provide this key.
                continue;
            }
            String actual = scheme.get(key);
            if (actual == null || !actual.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean handle(@NonNull QMUISchemeHandler handler,
                                   @NonNull Activity activity,
                                   @Nullable Map<String, SchemeValue> scheme);
}
