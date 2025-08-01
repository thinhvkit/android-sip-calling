/*
 *    Copyright (C) 2016 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ccsidd.rtone.view.advrecyclerview.utils.annotation;

import android.support.annotation.IntDef;

import com.ccsidd.rtone.view.advrecyclerview.utils.DebugWrapperAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = true, value = {
        DebugWrapperAdapter.FLAG_VERIFY_WRAP_POSITION,
        DebugWrapperAdapter.FLAG_VERIFY_UNWRAP_POSITION,
})
@Retention(RetentionPolicy.SOURCE)
public @interface DebugWrapperAdapterSettingFlags {
}
