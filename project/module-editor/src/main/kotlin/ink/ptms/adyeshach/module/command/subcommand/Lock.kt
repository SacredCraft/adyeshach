@file:Suppress("DuplicatedCode")

package ink.ptms.adyeshach.module.command.subcommand

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.entity.EntityInstance
import ink.ptms.adyeshach.core.entity.manager.Manager
import ink.ptms.adyeshach.core.entity.manager.ManagerType
import ink.ptms.adyeshach.core.util.plus
import ink.ptms.adyeshach.core.util.safeDistance
import org.bukkit.Particle
import org.bukkit.command.CommandSender
import taboolib.common.io.newFile
import taboolib.common.platform.Schedule
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.function.getDataFolder
import taboolib.platform.util.onlinePlayers

private const val STANDARD_LOCK_TRACKER = "lock"

private val manager: Manager
    get() = Adyeshach.api().getPublicEntityManager(ManagerType.PERSISTENT)

/**
 * npc look (id)? (method) (...)?
 *
 * npc look 1 here
 * npc look 1 to world 0 0 0 —— 移动到指定位置
 */
val lockSubCommand = subCommand {
    dynamic("id") {
        suggest { manager.getEntities { it.id.startsWith("public_") }.map { it.id } }
        exec<CommandSender> {
            val entity = manager.getEntityById(ctx["id"]).first()
            // 已锁定
            if (entity.hasPersistentTag("PUBLIC_LOCK")) {
                entity.removePersistentTag("PUBLIC_LOCK")
                sender.sendMessage("§c[Adyeshach] §7Entity is unlocked.")
            } else {
                // 设置锁定标签
                entity.setPersistentTag("PUBLIC_LOCK", sender.name)
                // 写入文件
                newFile(getDataFolder(), "npc/${entity.entityType}-${entity.id}.json").writeText(entity.toJson())
                sender.sendMessage("§c[Adyeshach] §7Entity is locked and saved.")
            }
        }
    }
}

@Schedule(period = 20, async = true)
private fun alert() {
    onlinePlayers.forEach { player ->
        manager.getEntities { it.getLocation().safeDistance(player.location) < 32 && isPublicButNotLocked(it) }.forEach { entity ->
            player.spawnParticle(Particle.END_ROD, entity.getEyeLocation().plus(y = 1.0), 5, 0.1, 0.1, 0.1, 0.0)
        }
    }
}

private fun isPublicButNotLocked(entity: EntityInstance): Boolean {
    return entity.id.startsWith("public_") && !entity.hasPersistentTag("PUBLIC_LOCK")
}