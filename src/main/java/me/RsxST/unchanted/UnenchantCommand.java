package me.RsxST.unchanted;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record UnenchantCommand(Unchanted plugin) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.format(plugin.getConfig().getString("player_only", "This command can only be executed by a player")));
            return true;
        }

        if (!player.hasPermission("unenchant.use")) {
            player.sendMessage(plugin.format(plugin.getConfig().getString("no_permission", "You do not have permission to use this command")));
            return true;
        }

        if (args.length == 0) {
            // No arguments: list enchants to remove one at a time
            plugin.removeEnchantments(player, false);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            // Remove all enchantments
            plugin.removeEnchantments(player, true);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String enchantName = args[1].toLowerCase();

            Registry<@NotNull Enchantment> enchantRegistry = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT);
            Enchantment enchant = enchantRegistry.get(NamespacedKey.minecraft(enchantName));

            if (enchant == null) {
                player.sendMessage(plugin.format(plugin.getConfig().getString("unknown_enchant", "&cUnknown enchantment: &e{enchant}")
                        .replace("{enchant}", enchantName)));
                return true;
            }

            var item = player.getInventory().getItemInMainHand();
            if (!item.containsEnchantment(enchant)) {
                player.sendMessage(plugin.format(plugin.getConfig().getString("no_item_enchant", "&cYour item does not have that enchantment!")));
                return true;
            }

            int level = item.getEnchantmentLevel(enchant);
            boolean allowOverLimit = plugin.getConfig().getBoolean("allow-overlimit-books", true);
            plugin.giveBook(player, enchant, level, allowOverLimit);
            item.removeEnchantment(enchant);

            player.sendMessage(plugin.format(plugin.getConfig().getString("removed_enchant", "&aRemoved &e{enchant} &afrom your item!")
                    .replace("{enchant}", enchant.getKey().getKey())));
            return true;
        }

        // Fallback usage message
        player.sendMessage(plugin.format(plugin.getConfig().getString("usage", "&cUsage: /unenchant [all] or click an enchantment to remove it")));
        return true;
    }
}
