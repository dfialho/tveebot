package dfialho.tveebot.app.repositories

import dfialho.tveebot.app.components.FileStash

interface FileStashRepository {

    fun insert(stashedFile: FileStash.StashedFile)
    fun findByName(name: String): FileStash.StashedFile?
    fun remove(name: String)
    fun list(): List<FileStash.StashedFile>
}
