/*
 *    Copyright (C) 2015 Haruki Hasegawa
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

package com.ccsidd.rtone.view.advrecyclerview.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ccsidd.rtone.view.advrecyclerview.expandable.ExpandableItemViewHolder;
import com.ccsidd.rtone.view.advrecyclerview.expandable.annotation.ExpandableItemStateFlags;

public abstract class AbstractExpandableItemViewHolder extends RecyclerView.ViewHolder implements ExpandableItemViewHolder {
    @ExpandableItemStateFlags
    private int mExpandStateFlags;

    public AbstractExpandableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setExpandStateFlags(@ExpandableItemStateFlags int flags) {
        mExpandStateFlags = flags;
    }

    @Override
    @ExpandableItemStateFlags
    public int getExpandStateFlags() {
        return mExpandStateFlags;
    }
}
