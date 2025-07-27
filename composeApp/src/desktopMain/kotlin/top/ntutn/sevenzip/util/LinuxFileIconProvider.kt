package top.ntutn.sevenzip.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.awt.image.BufferedImage
import java.io.File

// GTK库的JNA接口定义
interface GtkLibrary : Library {
    fun gtk_init(argc: IntArray?, argv: Array<String?>?)
    fun g_file_new_for_path(path: String): Pointer
    fun g_file_query_info(
        file: Pointer,
        attributes: String,
        flags: Int,
        cancellable: Pointer?,
        error: PointerByReference?
    ): Pointer
    fun g_file_info_get_icon(info: Pointer): Pointer
    fun gtk_icon_theme_get_default(): Pointer
    fun gtk_icon_theme_load_icon(
        icon_theme: Pointer,
        icon_name: String,
        size: Int,
        flags: Int,
        error: PointerByReference?
    ): Pointer
    fun gdk_pixbuf_get_width(pixbuf: Pointer): Int
    fun gdk_pixbuf_get_height(pixbuf: Pointer): Int
    fun gdk_pixbuf_get_pixels(pixbuf: Pointer): Pointer
    fun gdk_pixbuf_get_rowstride(pixbuf: Pointer): Int
    fun gdk_pixbuf_get_n_channels(pixbuf: Pointer): Int
    fun gdk_pixbuf_get_has_alpha(pixbuf: Pointer): Boolean
    fun g_object_unref(obj: Pointer)
    fun g_icon_to_string(icon: Pointer): String?
    fun g_error_free(error: Pointer)
    // 新增获取像素格式的方法
    fun gdk_pixbuf_get_bits_per_sample(pixbuf: Pointer): Int
}

object LinuxFileIconProvider {
    // 初始化GTK库
    private val gtk: GtkLibrary by lazy {
        Native.load("gtk-3", GtkLibrary::class.java).apply {
            gtk_init(intArrayOf(0), null)
            println("GTK initialized successfully")
        }
    }

    // 扩展名到图标名称的映射
    private val extensionToIconName = mapOf(
        setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "lzma") to "package-x-generic",
        setOf("txt", "md", "markdown", "rst") to "text-plain",
        setOf("pdf") to "application-pdf",
        setOf("jpg", "jpeg", "png", "gif", "bmp", "svg") to "image-x-generic",
        setOf("java", "kt", "c", "cpp", "h", "py", "js", "html", "css") to "text-x-source",
        setOf("docx", "doc") to "application-msword",
        setOf("xlsx", "xls") to "application-vnd.ms-excel",
        setOf("pptx", "ppt") to "application-vnd.ms-powerpoint"
    )

    /**
     * 以suspend function形式提供的图标获取方法
     */
    fun getFileIcon(filePath: String, size: Int = 48): ImageBitmap?  {
        try {
            println("Trying to get icon for: $filePath (size: $size)")
            val file = File(filePath)

            if (!file.exists()) {
                println("File does not exist: $filePath")
                return getDefaultIconForExtension(file.extension, size)
            }

            if (file.isDirectory) {
                return getDirectoryIcon(size)
            }

            // 先尝试通过文件本身获取图标
            val fileIcon = getIconFromFile(filePath, size)
            if (fileIcon != null) {
                println("Successfully loaded icon for $filePath")
                return fileIcon
            }

            // 如果失败，使用扩展名获取默认图标
            val extension = file.extension.takeIf { it.isNotEmpty() } ?: "unknown"
            println("Falling back to extension-based icon for $extension")
            return getDefaultIconForExtension(extension, size)
        } catch (e: Exception) {
            println("Error getting file icon: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * 获取目录图标
     */
    private fun getDirectoryIcon(size: Int): ImageBitmap? {
        return loadIconByName("folder", size)
    }

    /**
     * 从文件本身获取图标
     */
    private fun getIconFromFile(filePath: String, size: Int): ImageBitmap? {
        val error = PointerByReference()

        // 创建GFile对象
        val file = gtk.g_file_new_for_path(filePath)
        if (file == Pointer.NULL) {
            println("Failed to create GFile for $filePath")
            return null
        }

        try {
            // 查询文件信息
            val info = gtk.g_file_query_info(
                file,
                "standard::icon",
                0, // G_FILE_QUERY_INFO_NONE
                null,
                error
            )

            if (error.value != Pointer.NULL) {
                println("Error querying file info: ${error.value}")
                gtk.g_error_free(error.value)
                error.value = Pointer.NULL
                return null
            }

            if (info == Pointer.NULL) {
                println("File info is NULL for $filePath")
                return null
            }

            try {
                // 获取图标
                val icon = gtk.g_file_info_get_icon(info)
                if (icon == Pointer.NULL) {
                    println("Icon is NULL for $filePath")
                    return null
                }

                val iconName = gtk.g_icon_to_string(icon)
                if (iconName.isNullOrEmpty()) {
                    println("Icon name is empty for $filePath")
                    return null
                }
                println("Found icon name: $iconName for $filePath")

                // 尝试加载图标
                val pixbuf = gtk.gtk_icon_theme_load_icon(
                    gtk.gtk_icon_theme_get_default(),
                    iconName,
                    size,
                    0, // GTK_ICON_LOOKUP_NONE
                    error
                )

                if (error.value != Pointer.NULL) {
                    println("Error loading icon: ${error.value}")
                    gtk.g_error_free(error.value)
                    error.value = Pointer.NULL
                    return null
                }

                if (pixbuf == Pointer.NULL) {
                    println("Pixbuf is NULL for icon name: $iconName")
                    return null
                }

                try {
                    // 验证pixbuf属性
                    val width = gtk.gdk_pixbuf_get_width(pixbuf)
                    val height = gtk.gdk_pixbuf_get_height(pixbuf)
                    val channels = gtk.gdk_pixbuf_get_n_channels(pixbuf)
                    val bitsPerSample = gtk.gdk_pixbuf_get_bits_per_sample(pixbuf)
                    val hasAlpha = gtk.gdk_pixbuf_get_has_alpha(pixbuf)

                    println("Loaded pixbuf: $width x $height, $channels channels, $bitsPerSample bits/sample, alpha: $hasAlpha")

                    if (width <= 0 || height <= 0) {
                        println("Invalid pixbuf dimensions: $width x $height")
                        return null
                    }

                    // 转换为ImageBitmap
                    val imageBitmap = pixbufToImageBitmap(pixbuf)!!
                    if (imageBitmap.width != width || imageBitmap.height != height) {
                        println("ImageBitmap dimensions mismatch: ${imageBitmap.width} x ${imageBitmap.height}")
                    }
                    return imageBitmap
                } finally {
                    gtk.g_object_unref(pixbuf)
                }
            } finally {
                gtk.g_object_unref(info)
            }
        } finally {
            gtk.g_object_unref(file)
        }
    }

    /**
     * 根据文件扩展名获取默认图标
     */
    private fun getDefaultIconForExtension(extension: String, size: Int): ImageBitmap? {
        val iconName = extensionToIconName.firstNotNullOfOrNull { (extensions, name) ->
            if (extensions.contains(extension.lowercase())) name else null
        } ?: "unknown"

        println("Using default icon name: $iconName for extension: $extension")
        return loadIconByName(iconName, size)
    }

    /**
     * 根据图标名称加载图标
     */
    private fun loadIconByName(iconName: String, size: Int): ImageBitmap? {
        val error = PointerByReference()

        try {
            val iconTheme = gtk.gtk_icon_theme_get_default()
            if (iconTheme == Pointer.NULL) {
                println("Icon theme is NULL")
                return null
            }

            val pixbuf = gtk.gtk_icon_theme_load_icon(
                iconTheme,
                iconName,
                size,
                0, // GTK_ICON_LOOKUP_NONE
                error
            )

            if (error.value != Pointer.NULL) {
                println("Error loading icon $iconName: ${error.value}")
                gtk.g_error_free(error.value)
                // 尝试通用 fallback 图标
                if (iconName != "unknown") {
                    return loadIconByName("unknown", size)
                }
                return createFallbackIcon(size)
            }

            if (pixbuf == Pointer.NULL) {
                println("Pixbuf is NULL for icon name: $iconName")
                return createFallbackIcon(size)
            }

            try {
                val width = gtk.gdk_pixbuf_get_width(pixbuf)
                val height = gtk.gdk_pixbuf_get_height(pixbuf)
                println("Loaded icon $iconName: $width x $height")
                return pixbufToImageBitmap(pixbuf)
            } finally {
                gtk.g_object_unref(pixbuf)
            }
        } catch (e: Exception) {
            println("Error loading icon $iconName: ${e.message}")
            e.printStackTrace()
            return createFallbackIcon(size)
        }
    }

    /**
     * 将GdkPixbuf直接转换为Compose的ImageBitmap（优化版）
     */
    private fun pixbufToImageBitmap(pixbuf: Pointer): ImageBitmap? {
        return try {
            val width = gtk.gdk_pixbuf_get_width(pixbuf)
            val height = gtk.gdk_pixbuf_get_height(pixbuf)
            val pixels = gtk.gdk_pixbuf_get_pixels(pixbuf)
            val rowStride = gtk.gdk_pixbuf_get_rowstride(pixbuf)
            val hasAlpha = gtk.gdk_pixbuf_get_has_alpha(pixbuf)
            val channels = gtk.gdk_pixbuf_get_n_channels(pixbuf)

            // 创建缓冲区数组
            val buffer = ByteArray(rowStride * height)
            pixels.read(0, buffer, 0, buffer.size)

            // 根据GTK的像素格式创建BufferedImage (GTK使用RGB或RGBA格式)
            val imageType = if (hasAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
            val image = BufferedImage(width, height, imageType)

            // 处理像素数据 (GTK的像素顺序是BGR或BGRA，需要转换为RGB或RGBA)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pos = y * rowStride + x * channels
                    if (pos + 2 >= buffer.size) break

                    val blue = buffer[pos].toInt() and 0xFF
                    val green = buffer[pos + 1].toInt() and 0xFF
                    val red = buffer[pos + 2].toInt() and 0xFF
                    val alpha = if (hasAlpha && pos + 3 < buffer.size) {
                        buffer[pos + 3].toInt() and 0xFF
                    } else {
                        0xFF
                    }

                    // 转换为ARGB格式
                    val argb = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                    image.setRGB(x, y, argb)
                }
            }

            // 验证图像是否有效
            if (isImageEmpty(image)) {
                println("Converted image is empty")
                return null
            }

            // 转换为Compose的ImageBitmap
            image.toComposeImageBitmap()
        } catch (e: Exception) {
            println("Error converting pixbuf to ImageBitmap: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建一个简单的 fallback 图标，确保不会显示空白
     */
    private fun createFallbackIcon(size: Int): ImageBitmap? {
        try {
            println("Creating fallback icon of size $size")
            val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()

            // 绘制一个简单的文件图标
            g.color = java.awt.Color.LIGHT_GRAY
            g.fillRect(0, 0, size, size)
            g.color = java.awt.Color.DARK_GRAY
            g.drawRect(0, 0, size - 1, size - 1)
            g.fillRect(size / 4, size / 3, size / 2, size / 8)
            g.fillRect(size / 4, size / 2, size * 3 / 8, size / 8)

            g.dispose()
            return image.toComposeImageBitmap()
        } catch (e: Exception) {
            println("Error creating fallback icon: ${e.message}")
            return null
        }
    }

    /**
     * 检查图像是否为空（全透明或全黑）
     */
    private fun isImageEmpty(image: BufferedImage): Boolean {
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                if (argb != 0 && argb != -16777216) { // 不是全透明也不是全黑
                    return false
                }
            }
        }
        return true
    }
}
