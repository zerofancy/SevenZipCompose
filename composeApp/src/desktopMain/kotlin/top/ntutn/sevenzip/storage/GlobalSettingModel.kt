package top.ntutn.sevenzip.storage

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

@Serializable
data class GlobalSettingModel(val density: Float = 1f, val fontScale: Float = 1f) {
    object Serializer: OkioSerializer<GlobalSettingModel> {
        private val jsonClient = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true  // 自动转换无效值为默认值
            explicitNulls = false     // 不序列化null值字段
        }

        override val defaultValue: GlobalSettingModel = GlobalSettingModel()

        override suspend fun readFrom(source: BufferedSource): GlobalSettingModel {
            return withContext(Dispatchers.Default) {
                jsonClient.decodeFromString(source.readUtf8())
            }
        }

        override suspend fun writeTo(
            t: GlobalSettingModel,
            sink: BufferedSink
        ) {
            withContext(Dispatchers.IO) {
                sink.use {
                    it.writeUtf8(jsonClient.encodeToString(t))
                }
            }
        }
    }
}
