package top.ntutn.sevenzip

import org.jetbrains.compose.resources.DrawableResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.icon_apk
import sevenzip.composeapp.generated.resources.icon_file
import sevenzip.composeapp.generated.resources.icon_files
import sevenzip.composeapp.generated.resources.icon_image
import sevenzip.composeapp.generated.resources.icon_json
import sevenzip.composeapp.generated.resources.icon_txt
import sevenzip.composeapp.generated.resources.icon_xml

/**
 * Utility class for file operations
 */
object FileIconUtils {


    /**
     * Get the appropriate icon path for a file based on its extension
     */
    fun getIconPath(isDir: Boolean, name: String): DrawableResource {
        if (isDir) {
            return Res.drawable.icon_files
        }
        val index = name.lastIndexOf(".")
        if (index == -1) {
            return Res.drawable.icon_file
        }
        return when (name.substring(index + 1)) {
            "apk" -> Res.drawable.icon_apk
            "json" -> Res.drawable.icon_json
            "png", "jpg", "jpeg" -> Res.drawable.icon_image
            "txt" -> Res.drawable.icon_txt
            "xml" -> Res.drawable.icon_xml
            else -> Res.drawable.icon_file
        }
    }

}