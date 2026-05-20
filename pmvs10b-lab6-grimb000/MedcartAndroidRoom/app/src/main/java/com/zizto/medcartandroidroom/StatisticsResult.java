package com.zizto.medcartandroidroom;

import androidx.room.ColumnInfo;

public class StatisticsResult {
    @ColumnInfo(name = "total")
    public int total;
    @ColumnInfo(name = "avgWeight")
    public float avgWeight;
    @ColumnInfo(name = "minWeight")
    public float minWeight;
    @ColumnInfo(name = "maxWeight")
    public float maxWeight;
    @ColumnInfo(name = "avgHeight")
    public float avgHeight;
}