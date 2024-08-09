package com.crosspaste.paste.item

import com.crosspaste.app.AppFileType
import com.crosspaste.dao.paste.PasteItem
import com.crosspaste.dao.paste.PasteState
import com.crosspaste.dao.paste.PasteType
import com.crosspaste.path.UserDataPathProvider
import com.crosspaste.presist.DesktopOneFilePersist
import com.crosspaste.presist.FileInfoTree
import com.crosspaste.serializer.PathStringRealmListSerializer
import com.crosspaste.serializer.StringRealmListSerializer
import com.crosspaste.utils.DesktopJsonUtils
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import okio.Path
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.nio.file.Paths

@Serializable
@SerialName("images")
class ImagesPasteItem : RealmObject, PasteItem, PasteImages {

    companion object {}

    @PrimaryKey
    @Transient
    override var id: ObjectId = BsonObjectId()

    @Serializable(with = StringRealmListSerializer::class)
    var identifiers: RealmList<String> = realmListOf()

    @Serializable(with = PathStringRealmListSerializer::class)
    override var relativePathList: RealmList<String> = realmListOf()

    var fileInfoTree: String = ""

    @Index
    override var favorite: Boolean = false

    override var count: Long = 0L

    override var basePath: String? = null

    override var size: Long = 0L

    override var md5: String = ""

    @Index
    @Transient
    override var pasteState: Int = PasteState.LOADING

    override var extraInfo: String? = null

    override fun getAppFileType(): AppFileType {
        return AppFileType.IMAGE
    }

    override fun getFilePaths(userDataPathProvider: UserDataPathProvider): List<Path> {
        val basePath = userDataPathProvider.resolve(appFileType = getAppFileType())
        return relativePathList.map { relativePath ->
            userDataPathProvider.resolve(basePath, relativePath, autoCreate = false, isFile = true)
        }
    }

    override fun getFileInfoTreeMap(): Map<String, FileInfoTree> {
        return DesktopJsonUtils.JSON.decodeFromString(fileInfoTree)
    }

    override fun getPasteFiles(userDataPathProvider: UserDataPathProvider): List<PasteFile> {
        val fileTreeMap = getFileInfoTreeMap()
        return getFilePaths(userDataPathProvider).flatMap { path ->
            val fileTree = fileTreeMap[path.name]!!
            fileTree.getPasteFileList(path)
        }
    }

    override fun getIdentifierList(): List<String> {
        return identifiers
    }

    override fun getPasteType(): Int {
        return PasteType.IMAGE
    }

    override fun getSearchContent(): String {
        return relativePathList.joinToString(separator = " ") { path ->
            Paths.get(path).fileName.toString().lowercase()
        }
    }

    override fun update(
        data: Any,
        md5: String,
    ) {}

    override fun clear(
        realm: MutableRealm,
        userDataPathProvider: UserDataPathProvider,
        clearResource: Boolean,
    ) {
        if (clearResource) {
            // Non-reference types need to clean up copied files
            if (basePath == null) {
                for (path in getFilePaths(userDataPathProvider)) {
                    DesktopOneFilePersist(path).delete()
                }
            }
        }
        realm.delete(this)
    }
}
