package top.ntutn.sevenzip.storage

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.div
import okio.FileSystem
import okio.Path.Companion.toPath

class GlobalSettingDataStore {
    private val db = DataStoreFactory.create(storage = OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = GlobalSettingModel.Serializer,
        producePath = {
            (FileKit.databasesDir / "globalSetting.json").toString().toPath()
        }
    ))

    fun settingData() = db.data

    suspend fun updateDensity(density: Float, fontScale: Float) = db.updateData {
        it.copy(density = density, fontScale = fontScale)
    }

    suspend fun updateUseSystemIcon(useSystemIcon: Boolean) = db.updateData {
        it.copy(tryUseSystemIcon = useSystemIcon)
    }
}