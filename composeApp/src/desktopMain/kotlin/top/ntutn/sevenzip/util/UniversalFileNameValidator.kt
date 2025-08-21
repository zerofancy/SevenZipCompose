package top.ntutn.sevenzip.util

import java.nio.file.InvalidPathException
import java.nio.file.Paths

object UniversalFileNameValidator {
    // 所有平台都不允许的字符集
    private val forbiddenChars = setOf(
        '/', '\\', ':', '*', '?', '"', '<', '>', '|', '\u0000'
    )

    // Windows保留名称（在所有平台都视为无效，确保跨平台兼容性）
    private val windowsReservedNames = setOf(
        "con", "prn", "aux", "nul",
        "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
        "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    )

    /**
     * 检查文件名是否在所有主流平台（Windows、Linux、macOS）都合法
     */
    fun isFileNameValidForAllPlatforms(fileName: String): Boolean {
        // 检查空字符串或仅空白字符
        if (fileName.isBlank()) {
            return false
        }

        // 检查特殊目录符号
        if (fileName == "." || fileName == "..") {
            return false
        }

        // 检查是否包含任何平台的非法字符
        if (fileName.any { it in forbiddenChars }) {
            return false
        }

        // 检查是否为Windows保留名称（即使在其他平台也视为无效）
        val baseName = fileName.substringBeforeLast('.', fileName).lowercase()
        if (windowsReservedNames.contains(baseName)) {
            return false
        }

        // 检查文件名长度（取各平台最小限制，UTF-8编码下不超过255字节）
        if (fileName.toByteArray(Charsets.UTF_8).size > 255) {
            return false
        }

        // 检查Windows特有的结尾限制（即使在其他平台也遵守）
        if (fileName.endsWith(' ') || fileName.endsWith('.')) {
            return false
        }

        // 额外检查各平台路径合法性
        return try {
            // 检查Windows路径合法性
            Paths.get("C:\\", fileName)
            // 检查Unix路径合法性
            Paths.get("/tmp", fileName)
            true
        } catch (_: InvalidPathException) {
            false
        }
    }

    /**
     * 将文件名清理为所有平台都接受的形式
     */
    fun sanitizeFileNameForAllPlatforms(
        fileName: String,
        replacement: Char = '_'
    ): String {
        if (fileName.isBlank()) {
            return "untitled"
        }

        // 替换所有非法字符
        var sanitized = fileName.replace(Regex("[${forbiddenChars.joinToString("") { Regex.escape(it.toString()) }}]"), replacement.toString())

        // 处理保留名称
        val baseName = sanitized.substringBeforeLast('.', sanitized).lowercase()
        if (windowsReservedNames.contains(baseName)) {
            sanitized = "${sanitized}$replacement"
        }

        // 处理特殊目录符号
        if (sanitized == "." || sanitized == "..") {
            sanitized = "$sanitized$replacement"
        }

        // 处理结尾的空格或点
        if (sanitized.endsWith(' ') || sanitized.endsWith('.')) {
            sanitized = sanitized.trimEnd('.', ' ') + replacement
        }

        // 处理长度限制（确保UTF-8字节数不超过255）
        val bytes = sanitized.toByteArray(Charsets.UTF_8)
        if (bytes.size > 255) {
            // 截断到255字节以内
            var truncated = Charsets.UTF_8.decode(java.nio.ByteBuffer.wrap(bytes, 0, 255)).toString()
            // 确保不会截断半个Unicode字符
            while (truncated.toByteArray(Charsets.UTF_8).size > 255) {
                truncated = truncated.substring(0, truncated.length - 1)
            }
            sanitized = truncated
        }

        return sanitized
    }
}

// 使用示例
fun main() {
    val testNames = listOf(
        "valid_name", "with.period", "with spaces",
        "con", "prn.txt", ".", "..",
        "invalid/name", "invalid?name", "a".repeat(300),
        "trailing space ", "trailing.dot.", "包含中文和符号#￥%",
        "com1", "lpt9.log", "my:file"
    )

    testNames.forEach { name ->
        val isValid = UniversalFileNameValidator.isFileNameValidForAllPlatforms(name)
        val sanitized = UniversalFileNameValidator.sanitizeFileNameForAllPlatforms(name)

        println("原始名称: '$name'")
        println("是否全平台合法: ${if (isValid) "是" else "否"}")
        println("清理后名称: '$sanitized'")
        println("---")
    }
}
