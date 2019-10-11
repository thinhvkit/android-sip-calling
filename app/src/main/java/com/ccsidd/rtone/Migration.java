/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ccsidd.rtone;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Example of migrating a Realm file from version 0 (initial version) to its last version (version 3).
 */
public class Migration implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        // During a migration, a DynamicRealm is exposed. A DynamicRealm is an untyped variant of a normal Realm, but
        // with the same object creation and query capabilities.
        // A DynamicRealm uses Strings instead of Class references because the Classes might not even exist or have been
        // renamed.

        // Access the Realm schema in order to create, modify or delete classes and their fields.
        RealmSchema schema = realm.getSchema();
        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            //
            RealmObjectSchema callLogSchema = schema.get("CallLog");
            if (!callLogSchema.hasField("dataUsage")) {
                callLogSchema
                        .addField("dataUsage", String.class, FieldAttribute.REQUIRED);
                callLogSchema.setNullable("dataUsage", true);
                Log.i("com.ccsidd.rtone", "schema version" + oldVersion);
            }
            oldVersion++;
        }
        if (oldVersion == 1) {
            RealmObjectSchema contactBlockListSchema = schema.create("ContactBlockList")
                    .addField("phoneNumbers", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("displayName", String.class, FieldAttribute.REQUIRED)
                    .addField("imageUri", String.class, FieldAttribute.REQUIRED);
            contactBlockListSchema.setNullable("displayName", true);
            contactBlockListSchema.setNullable("imageUri", true);
            oldVersion++;
        }

        if (oldVersion == 2) {
            schema.remove("Contact");
            RealmObjectSchema contactSchema = schema.create("Contact");
            contactSchema.addField("id", int.class, FieldAttribute.PRIMARY_KEY);
            contactSchema.addField("displayName", String.class);
            contactSchema.addField("imageUri", String.class);

            RealmObjectSchema phoneSchema = schema.get("Phone");
            contactSchema.addRealmListField("phoneNumbers", phoneSchema);

            oldVersion++;
        }
        if (oldVersion == 3) {
            RealmObjectSchema contactBlockListSchema = schema.get("ContactBlockList");
            contactBlockListSchema.setNullable("phoneNumbers", true);

            oldVersion++;
        }
        if (oldVersion == 4) {
            RealmObjectSchema messageSchema = schema.create("Message")
                    .addField("phoneNumber", String.class, FieldAttribute.INDEXED)
                    .addField("type", Integer.class, FieldAttribute.REQUIRED)
                    .addField("body", String.class, FieldAttribute.REQUIRED)
                    .addField("time", Long.class, FieldAttribute.REQUIRED)
                    .addField("unRead", Boolean.class, FieldAttribute.REQUIRED);
            messageSchema.setNullable("body", true);

            RealmObjectSchema conversationSchema = schema.create("Conversation")
                    .addField("phoneNumber", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("type", Integer.class, FieldAttribute.REQUIRED)
                    .addField("lastMessage", String.class, FieldAttribute.REQUIRED)
                    .addField("time", Long.class, FieldAttribute.REQUIRED)
                    .addField("unRead", Boolean.class, FieldAttribute.REQUIRED);
            conversationSchema.setNullable("lastMessage", true);

            oldVersion++;
        }
    }



    /*@Override
    public boolean equals(Object o) {
        return super.equals(o);
    }*/
}
