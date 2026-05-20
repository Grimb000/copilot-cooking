package com.zizto.medcarts;

import android.net.Uri;

public class PatientContract {
    public static final String AUTHORITY = "com.zizto.medcarts.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/patients");

    public static final String PATH_PATIENTS = "patients";
    public static final String PATH_STATS = "stats";

    public static final String _ID = "id";
    public static final String COLUMN_FIO = "fio";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_CARD = "card_number";
    public static final String COLUMN_DIAGNOSIS = "diagnosis";
    public static final String COLUMN_BLOOD_GROUP = "blood_group";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zizto.patients";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zizto.patients";
}