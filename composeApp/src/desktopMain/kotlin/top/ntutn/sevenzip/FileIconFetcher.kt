package top.ntutn.sevenzip

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.WinDef.HDC
import com.sun.jna.platform.win32.WinDef.HICON
import com.sun.jna.platform.win32.WinGDI
import com.sun.jna.platform.win32.WinGDI.BITMAP
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.awt.image.BufferedImage

// 定义结构体对齐常量
private const val STRUCTURE_ALIGN_BYTE = 1
private const val STRUCTURE_ALIGN_NONE = 0

// 定义Win32 API所需的类型别名
typealias HICON = Pointer
typealias HANDLE = Pointer

// 定义Shell32接口
interface Shell32 : StdCallLibrary {
    // 定义SHFILEINFO结构体
    class SHFILEINFO(byValue: Boolean = false) : Structure(if (byValue) STRUCTURE_ALIGN_BYTE else STRUCTURE_ALIGN_NONE) {
        @JvmField var hIcon: HICON? = null
        @JvmField var iIcon = 0
        @JvmField var dwAttributes = 0
        @JvmField var szDisplayName = CharArray(260)
        @JvmField var szTypeName = CharArray(80)

        override fun getFieldOrder() = listOf("hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName")
    }
    // 声明SHGetFileInfo函数
    fun SHGetFileInfo(
        pszPath: String?,
        dwFileAttributes: Int,
        psfi: SHFILEINFO,
        cbSizeFileInfo: Int,
        uFlags: Int
    ): HANDLE?

    companion object {
        val INSTANCE: Shell32 = Native.load("shell32", Shell32::class.java, W32APIOptions.DEFAULT_OPTIONS) as Shell32
    }
}

// 定义User32接口
interface User32 : StdCallLibrary {
    fun DestroyIcon(hIcon: HICON?): Boolean
    fun GetDC(hwnd: Pointer?): Pointer
    fun ReleaseDC(hwnd: Pointer?, hdc: Pointer): Int
    fun GetIconInfo(hIcon: HICON?, pIconInfo: WinGDI.ICONINFO): Boolean

    companion object {
        val INSTANCE: User32 = Native.load("user32", User32::class.java, W32APIOptions.DEFAULT_OPTIONS) as User32
    }
}

object FileIconFetcher {
    // Win32 API常量
    private const val SHGFI_ICON = 0x000000100
    private const val SHGFI_SMALLICON = 0x000000001
    private const val SHGFI_LARGEICON = 0x000000000
    private const val SHGFI_USEFILEATTRIBUTES = 0x000000010

    // 文件属性常量
    private const val FILE_ATTRIBUTE_DIRECTORY = 0x00000010
    private const val FILE_ATTRIBUTE_NORMAL = 0x00000080

    /**
     * 获取文件或文件夹的图标
     */
    fun getFileIcon(path: String, isDirectory: Boolean, isLarge: Boolean = false): BufferedImage? {
        val shfi = Shell32.SHFILEINFO(true)
        val flags = SHGFI_ICON or (if (isLarge) SHGFI_LARGEICON else SHGFI_SMALLICON) or SHGFI_USEFILEATTRIBUTES
        val fileAttributes = if (isDirectory) FILE_ATTRIBUTE_DIRECTORY else FILE_ATTRIBUTE_NORMAL

        val result = Shell32.INSTANCE.SHGetFileInfo(
            path,
            fileAttributes,
            shfi,
            shfi.size(),
            flags
        )

        if (result == null || result == Pointer.NULL || shfi.hIcon == null) {
            return null
        }

        // 通过绘制图标到缓冲区来转换HICON为Image
        val image = toImage(shfi.hIcon)

        return image
    }

    private fun toImage(hicon: HICON?): BufferedImage? {
        var deviceContext: Pointer? = null
        val user32 = User32.INSTANCE
        val gdi32 = GDI32.INSTANCE
        val info = WinGDI.ICONINFO()

        try {
            if (!user32.GetIconInfo(hicon, info)) return null

            info.read() // 确保结构体数据从原生内存同步到 Java 对象
            val bitmapHandle = info.hbmColor ?: info.hbmMask
            if (bitmapHandle == null) {
                return null
            }

            val bitmap = BITMAP()
            if (gdi32.GetObject(bitmapHandle, bitmap.size(), bitmap.getPointer()) > 0) {
                bitmap.read()

                val width = bitmap.bmWidth.toInt()
                val height = bitmap.bmHeight.toInt()

                deviceContext = user32.GetDC(null)
                val bitmapInfo = BITMAPINFO()

                bitmapInfo.bmiHeader.biSize = bitmapInfo.bmiHeader.size()
                require(
                    gdi32.GetDIBits(
                        HDC(deviceContext), bitmapHandle, 0, 0, Pointer.NULL, bitmapInfo,
                        WinGDI.DIB_RGB_COLORS
                    ) != 0
                ) { "GetDIBits should not return 0" }

                bitmapInfo.read()

                val pixels = Memory(bitmapInfo.bmiHeader.biSizeImage.toLong())
                bitmapInfo.bmiHeader.biCompression = WinGDI.BI_RGB
                bitmapInfo.bmiHeader.biHeight = -height

                require(
                    gdi32.GetDIBits(
                        HDC(deviceContext),
                        bitmapHandle,
                        0,
                        bitmapInfo.bmiHeader.biHeight,
                        pixels,
                        bitmapInfo,
                        WinGDI.DIB_RGB_COLORS
                    ) != 0
                ) { "GetDIBits should not return 0" }

                val colorArray = pixels.getIntArray(0, width * height)
                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                image.setRGB(0, 0, width, height, colorArray, 0, width)

                return image
            }
        } finally {
            deviceContext?.let { user32.ReleaseDC(null, it) }
            user32.DestroyIcon(hicon)
            info.hbmMask?.let(gdi32::DeleteObject)
            info.hbmColor?.let(gdi32::DeleteObject)
        }

        return null
    }
}