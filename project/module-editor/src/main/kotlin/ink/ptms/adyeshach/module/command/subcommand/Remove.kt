@file:Suppress("DuplicatedCode")

package ink.ptms.adyeshach.module.command.subcommand

import ink.ptms.adyeshach.core.entity.EntityInstance
import ink.ptms.adyeshach.core.util.sendLang
import ink.ptms.adyeshach.module.command.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggestUncheck

private const val STANDARD_REMOVE_TRACKER = "remove"

/**
 * npc remove (id)? (action)?
 *
 * npc remove 1
 * npc remove 1 a
 */
val removeSubCommand = subCommand {
    dynamic("id") {
        suggestEntityList()
        dynamic("action") {
            suggestUncheck { listOf("a", "all") }
            execute<CommandSender> { sender, ctx, _ ->
                val npcList = Command.finder.getEntitiesFromIdOrUniqueId(ctx["id"], if (sender is Player) sender else null)
                if (npcList.isEmpty()) {
                    sender.sendLang("command-find-empty")
                    return@execute
                }
                // 删除单位
                npcList.forEach {
                    // 是否锁定
                    if (it.hasPersistentTag("PUBLIC_LOCK")) {
                        sender.sendMessage("§c[Adyeshach] §7Entity is locked.")
                        sender.sendMessage("§c[Adyeshach] §7Use §8/adyeshach lock ${it.id}§7 to unlock.")
                        return@forEach
                    }
                    it.remove()
                }
                // 打印追踪器
                EntityTracker.check(sender, STANDARD_REMOVE_TRACKER, npcList.first())
                // 提示信息
                when (ctx.self()) {
                    // 删除全部
                    "a", "all" -> sender.sendLang("command-remove-success-all", ctx["id"], npcList.first().uniqueId)
                    // 删除单个
                    "c" -> sender.sendLang("command-remove-success", npcList.first().id, npcList.first().uniqueId)
                }
            }
        }
        // 定向删除
        execute<CommandSender> { sender, ctx, _ ->
            multiControl<RemoveEntitySource>(sender, ctx.self(), STANDARD_REMOVE_TRACKER) {
                // 是否锁定
                if (it.hasPersistentTag("PUBLIC_LOCK")) {
                    sender.sendMessage("§c[Adyeshach] §7Entity is locked.")
                    sender.sendMessage("§c[Adyeshach] §7Use §8/adyeshach lock ${it.id}§7 to unlock.")
                    return@execute
                }
                it.remove()
                sender.sendLang("command-remove-success", it.id, it.uniqueId)
            }
        }
    }
    // 就近删除
    execute<Player> { sender, _, _ ->
        multiControl<RemoveEntitySource>(sender, STANDARD_REMOVE_TRACKER) {
            // 是否锁定
            if (it.hasPersistentTag("PUBLIC_LOCK")) {
                sender.sendMessage("§c[Adyeshach] §7Entity is locked.")
                sender.sendMessage("§c[Adyeshach] §7Use §8/adyeshach lock ${it.id}§7 to unlock.")
                return@execute
            }
            it.remove()
            sender.sendLang("command-remove-success", it.id, it.uniqueId)
        }
    }
}

class RemoveEntitySource(elements: MutableList<EntityInstance>) : EntitySource(elements) {

    override fun isUpdated(entity: EntityInstance): Boolean {
        return entity.isRemoved
    }

    override fun extraArgs(entity: EntityInstance): Array<Any> {
        return emptyArray()
    }
}