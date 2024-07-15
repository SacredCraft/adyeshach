package ink.ptms.adyeshach.impl.manager

import ink.ptms.adyeshach.core.AdyeshachSettings
import ink.ptms.adyeshach.core.entity.EntityInstance
import ink.ptms.adyeshach.core.serializer.UnknownWorldException
import org.bukkit.Bukkit
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.io.newFolder
import taboolib.common.platform.function.getDataFolder
import taboolib.module.chat.uncolored
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.manager.LocalPersistentManager
 *
 * @author 坏黑
 * @since 2022/12/29 15:37
 */
open class LocalPersistentManager : DefaultManager() {

    val hash = ConcurrentHashMap<String, String>()

    override fun onEnable() {
        activeEntity.clear()
        newFolder(getDataFolder(), "npc").listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            try {
                loadEntityFromFile(file)
            } catch (ex: UnknownWorldException) {
                if (AdyeshachSettings.isAutoDeleteWorld(ex.world)) {
                    file.delete()
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
        // 对 TraitSit 的暴力回收
        activeEntity.forEach { entity ->
            if (entity.getCustomName().uncolored() == "trait_sit_npc") {
                if (entity.getPassengers().isEmpty()) {
                    entity.remove()
                }
            }
        }
    }

    override fun onSave() {
        activeEntity.forEach { entity ->
            // 锁定的实体不保存
            if (entity.hasPersistentTag("PUBLIC_LOCK")) {
                return@forEach
            }
            val json = entity.toJson()
            val jsonHash = json.digest("sha-1")
            if (hash[entity.uniqueId] != jsonHash) {
                hash[entity.uniqueId] = jsonHash
                newFile(getDataFolder(), "npc/${entity.entityType}-${entity.id}.json").writeText(json)
            }
        }
    }

    override fun remove(entityInstance: EntityInstance) {
        super.remove(entityInstance)
        val name = "${entityInstance.entityType}-${entityInstance.id}"
        val file = newFile(getDataFolder(), "npc/$name.json")
        if (file.exists()) {
            // 写入垃圾桶，且不重复
            var i = 0
            while (true) {
                val newFile = File(getDataFolder(), "npc/trash/${name}_${System.currentTimeMillis() + i}.json")
                if (newFile.exists()) {
                    i++
                    continue
                }
                newFile.writeText(entityInstance.toJson())
                break
            }
            // 删除源文件
            file.delete()
        }
        hash.remove(entityInstance.uniqueId)
    }

    override fun isTemporary(): Boolean {
        return false
    }

    override fun loadEntityFromFile(file: File): EntityInstance {
        val entity = super.loadEntityFromFile(file)
        if (entity.visibleAfterLoaded) {
            Bukkit.getOnlinePlayers().forEach { p -> entity.addViewer(p) }
        }
        return entity
    }
}