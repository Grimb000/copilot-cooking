package com.example.task73

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.task73.proto.AppSettingsProto
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettingsProto> {
    override val defaultValue: AppSettingsProto = AppSettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettingsProto {
        try {
            return AppSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read app settings proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppSettingsProto, output: OutputStream) {
        t.writeTo(output)
    }
}
