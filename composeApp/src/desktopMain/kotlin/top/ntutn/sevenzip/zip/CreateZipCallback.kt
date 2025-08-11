package top.ntutn.sevenzip.zip

import net.sf.sevenzipjbinding.IOutCreateCallback
import net.sf.sevenzipjbinding.IOutItemZip
import net.sf.sevenzipjbinding.ISequentialInStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.impl.OutItemFactory
import net.sf.sevenzipjbinding.util.ByteArrayStream
import java.io.File

class CreateZipCallback(private val baseFile: File, private val files: List<File>): IOutCreateCallback<IOutItemZip> {
    override fun setOperationResult(operationResultOk: Boolean) {
    }

    override fun getItemInformation(
        index: Int,
        outItemFactory: OutItemFactory<IOutItemZip>
    ): IOutItemZip? {
        var attr = PropID.AttributesBitMask.FILE_ATTRIBUTE_UNIX_EXTENSION
        val item = outItemFactory.createOutItem()
        val currentFile = files[index]
        if (currentFile.isDirectory) {
            item.propertyIsDir = true
            attr = attr or PropID.AttributesBitMask.FILE_ATTRIBUTE_DIRECTORY
            attr = attr or (0x81ED shl 16) // permissions: drwxr-xr-x
        } else {
            item.dataSize = currentFile.length()
            attr = attr or (0x81a4 shl 16)
        }
        item.propertyPath = currentFile.toRelativeString(baseFile)
        item.propertyAttributes = attr
        return item
    }

    override fun getStream(index: Int): ISequentialInStream? {
        val currentFile = files[index]
        if (!currentFile.exists() || !currentFile.isFile || !currentFile.canRead()) {
            return null
        }
        return ByteArrayStream(currentFile.readBytes(), false)
    }

    override fun setTotal(total: Long) {
    }

    override fun setCompleted(complete: Long) {
    }
}