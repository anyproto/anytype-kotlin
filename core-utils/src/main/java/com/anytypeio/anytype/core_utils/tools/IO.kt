package com.anytypeio.anytype.core_utils.tools

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipDirectory(sourceDir: File, zipFile: File) {
    ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val entryName = sourceDir.toURI().relativize(file.toURI()).path
                zipOut.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }
}