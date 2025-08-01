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

package com.ccsidd.rtone.view.advrecyclerview.expandable;

public interface ExpandableItemConstants {
    /**
     * State flag for the {@link ExpandableItemViewHolder#setExpandStateFlags(int)} and {@link ExpandableItemViewHolder#getExpandStateFlags()} methods.
     * Indicates that this ViewHolder is associated to group item.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    int STATE_FLAG_IS_GROUP = (1 << 0);

    /**
     * State flag for the {@link ExpandableItemViewHolder#setExpandStateFlags(int)} and {@link ExpandableItemViewHolder#getExpandStateFlags()} methods.
     * Indicates that this ViewHolder is associated to child item.
     */
    int STATE_FLAG_IS_CHILD = (1 << 1);

    /**
     * State flag for the {@link ExpandableItemViewHolder#setExpandStateFlags(int)} and {@link ExpandableItemViewHolder#getExpandStateFlags()} methods.
     * Indicates that this is an expanded group item.
     */
    int STATE_FLAG_IS_EXPANDED = (1 << 2);

    /**
     * State flag for the {@link ExpandableItemViewHolder#setExpandStateFlags(int)} and {@link ExpandableItemViewHolder#getExpandStateFlags()} methods.
     * If this flag is set, the {@link #STATE_FLAG_IS_EXPANDED} flag has changed.
     */
    int STATE_FLAG_HAS_EXPANDED_STATE_CHANGED = (1 << 3);

    /**
     * State flag for the {@link ExpandableItemViewHolder#setExpandStateFlags(int)} and {@link ExpandableItemViewHolder#getExpandStateFlags()} methods.
     * If this flag is set, some other flags are changed and require to apply.
     */
    int STATE_FLAG_IS_UPDATED = (1 << 31);
    // ---
}
