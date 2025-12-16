package me.RsxST.unchanted;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;

public final class Unchanted extends JavaPlugin {

    public final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        getLogger().info("UnEnchant has been enabled!");
        Objects.requireNonNull(this.getCommand("unenchant")).setExecutor(new UnenchantCommand(this)); // Register the command executor
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("UnEnchant has been disabled!");
    }

    public void removeEnchantments(Player player, boolean all) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage(format(getConfig().getString("hold_item")));
            return;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            player.sendMessage(format(getConfig().getString("no_enchants")));
            return;
        }

        if (all) {
            // Remove all enchantments
            enchantments.forEach((enchant, level) -> giveBook(player, enchant, level));
            enchantments.keySet().forEach(item::removeEnchantment);
            player.sendMessage(format(getConfig().getString("unenchant_success")));
        } else {
            // List enchantments for player to pick
            player.sendMessage(format("&eClick an enchantment to remove it:"));
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Component clickable = Component.text(entry.getKey().getKey().getKey() + " " + entry.getValue(), NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/unenchant remove " + entry.getKey().getKey().getKey()));
                player.sendMessage(clickable);
            }
        }
    }

    public void giveBook(Player player, Enchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta != null) {
            meta.addStoredEnchant(enchant, level, true);
            book.setItemMeta(meta);
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(format(getConfig().getString("inventory_full")));
        } else {
            player.getInventory().addItem(book);
        }
    }

    public Component format(String input) {
        if (input == null || input.isBlank()) return Component.empty();

        // Convert legacy & codes to MiniMessage-compatible string
        String miniMessageInput = LegacyComponentSerializer.legacyAmpersand().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(input)
        );

        return miniMessage.deserialize(miniMessageInput);
    }
}
