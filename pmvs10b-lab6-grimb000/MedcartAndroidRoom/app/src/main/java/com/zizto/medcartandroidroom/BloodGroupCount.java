package com.zizto.medcartandroidroom;

import androidx.room.ColumnInfo;

public class BloodGroupCount {
    @ColumnInfo(name = "blood_group")
    public String bloodGroup;

    @ColumnInfo(name = "count")
    public int count;
}