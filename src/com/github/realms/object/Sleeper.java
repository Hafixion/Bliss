package com.github.realms.object;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class Sleeper {
    private UUID uuid;
    private String display;
    private Location location;
    private Inventory inventory;
    private ItemStack[] armor;
    private ItemStack[] hands = new ItemStack[2];

    public Sleeper(Player player) {
        Villager entity = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        PlayerDisguise disguise = new PlayerDisguise(player.getName());
        display = player.getDisplayName();
        uuid = player.getUniqueId();

        // Create the inventory, which will be given upon the entity's death.
        // Not serialized, and deserialized using PlayerData, which unfortunately doesn't include armor.
        // Armor and hands are stored separately, due to bukkit serialization limitations
        inventory = Bukkit.createInventory(null, 54);
        inventory.setContents(player.getInventory().getContents());
        armor = player.getInventory().getArmorContents();
        hands[0] = player.getInventory().getItemInMainHand();
        hands[1] = player.getInventory().getItemInOffHand();


        location = player.getLocation();

        disguise.setEntity(entity);
        entity.setAI(false);
        entity.setRemoveWhenFarAway(false);
        disguise.getWatcher().setSleeping(true);

        // Set gear and hands.
        disguise.getWatcher().setArmor(armor);
        disguise.getWatcher().setItemInMainHand(hands[0]);
        disguise.getWatcher().setItemInOffHand(hands[1]);

        disguise.startDisguise();
    }

    public Sleeper(JSONObject json) {
        try {
            // Get the three bases
            uuid = UUID.fromString(json.getString("uuid"));
            display = json.getString("display");
            location = new Location(Bukkit.getWorld(UUID.fromString(json.getString("world"))),
                    json.getDouble("x"), json.getDouble("y"), json.getDouble("z"));

            inventory = Serializer.fromBase64(json.getString("inventory"));
            armor = Serializer.itemStackArrayFromBase64(json.getString("armor"));
            hands = Serializer.itemStackArrayFromBase64(json.getString("hands"));

            // Create the entity.
            Villager entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
            PlayerDisguise disguise = new PlayerDisguise(display);

            disguise.setEntity(entity);
            entity.setAI(false);
            entity.setRemoveWhenFarAway(false);
            entity.setProfession(Villager.Profession.NITWIT);
            disguise.getWatcher().setSleeping(true);

            // Set gear and hands.
            disguise.getWatcher().setArmor(armor);
            disguise.getWatcher().setItemInMainHand(hands[0]);
            disguise.getWatcher().setItemInOffHand(hands[1]);

            disguise.startDisguise();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();

            // Add the 3 Base Variables
            json.put("uuid", uuid.toString());
            json.put("display", display);
            json.put("world", location.getWorld().getUID().toString());
            json.put("x", location.getX());
            json.put("y", location.getY());
            json.put("z", location.getZ());

            // Inventories
            json.put("inventory", Serializer.toBase64(inventory));
            json.put("armor", Serializer.itemStackArrayToBase64(armor));
            json.put("hands", Serializer.itemStackArrayToBase64(hands));

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public Location getLocation() {
        return location;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getHands() {
        return hands;
    }

    public String getDisplay() {
        return display;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sleeper sleeper = (Sleeper) o;
        return Objects.equals(uuid, sleeper.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
