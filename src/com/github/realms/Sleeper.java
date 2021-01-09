package com.github.realms;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.simple.JSONObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Sleeper {
    private UUID uuid;
    private String display;
    private Location location;
    public Villager entity;
    public int entityid;
    private Inventory inventory;
    private ItemStack[] armor;
    private ItemStack[] hands = new ItemStack[2];

    public Sleeper(Player player) {
        Villager entity = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        this.entity = entity;

        PlayerDisguise disguise = new PlayerDisguise(player.getName());
        display = player.getDisplayName();
        uuid = player.getUniqueId();
        entityid = entity.getEntityId();

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
        entity.setCanPickupItems(false);
        entity.setRemoveWhenFarAway(false);
        disguise.getWatcher().setSleeping(true);
        entity.setCanPickupItems(false);

        // Set gear and hands.
        disguise.getWatcher().setArmor(armor);
        disguise.getWatcher().setItemInMainHand(hands[0]);
        disguise.getWatcher().setItemInOffHand(hands[1]);

        disguise.startDisguise();

        // Add it to the Cache.
        Bliss.getInstance().addtoCache(this, this.getUuid());
    }

    public Sleeper(JSONObject json) {
        try {
            // Get the three bases
            uuid = UUID.fromString((String) json.get("uuid"));
            display = (String) json.get("display");
            location = new Location(Bukkit.getWorld(UUID.fromString((String) json.get("world"))),
                    (Double) json.get("x"),(Double) json.get("y"),(Double) json.get("z"));

            inventory = Serializer.fromBase64((String) json.get("inventory"));
            armor = Serializer.itemStackArrayFromBase64((String) json.get("armor"));
            hands = Serializer.itemStackArrayFromBase64((String) json.get("hands"));

            // Create the entity.
            Villager entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
            this.entity = entity;

            PlayerDisguise disguise = new PlayerDisguise(display);
            entityid = entity.getEntityId();

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

            // Add it to the Cache.
            Bliss.getInstance().addtoCache(this, this.getUuid());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() {
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

    // Loot Method, used to get drops
    public List<ItemStack> getDrops() {
        return new ArrayList<>(Arrays.asList(inventory.getContents()));
    }

    /**
     * @author graywolf336
     * https://gist.github.com/graywolf336/8153678
     */
    public static class Serializer {

        /**
         * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
         *
         * @param playerInventory to turn into an array of strings.
         * @return Array of strings: [ main content, armor content ]
         * @throws IllegalStateException
         */
        public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
            //get the main content part, this doesn't return the armor
            String content = toBase64(playerInventory);
            String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

            return new String[] { content, armor };
        }

        /**
         *
         * A method to serialize an {@link ItemStack} array to Base64 String.
         *
         * <p />
         *
         * Based off of {@link #toBase64(Inventory)}.
         *
         * @param items to turn into a Base64 String.
         * @return Base64 string of the items.
         * @throws IllegalStateException
         */
        public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                // Write the size of the inventory
                dataOutput.writeInt(items.length);

                // Save every element in the list
                for (ItemStack item : items) {
                    dataOutput.writeObject(item);
                }

                // Serialize that array
                dataOutput.close();
                return Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (Exception e) {
                throw new IllegalStateException("Unable to save item stacks.", e);
            }
        }

        /**
         * A method to serialize an inventory to Base64 string.
         *
         * <p />
         *
         * Special thanks to Comphenix in the Bukkit forums or also known
         * as aadnk on GitHub.
         *
         * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
         *
         * @param inventory to serialize
         * @return Base64 string of the provided inventory
         * @throws IllegalStateException
         */
        public static String toBase64(Inventory inventory) throws IllegalStateException {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                // Write the size of the inventory
                dataOutput.writeInt(inventory.getSize());

                // Save every element in the list
                for (int i = 0; i < inventory.getSize(); i++) {
                    dataOutput.writeObject(inventory.getItem(i));
                }

                // Serialize that array
                dataOutput.close();
                return Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (Exception e) {
                throw new IllegalStateException("Unable to save item stacks.", e);
            }
        }

        /**
         *
         * A method to get an {@link Inventory} from an encoded, Base64, string.
         *
         * <p />
         *
         * Special thanks to Comphenix in the Bukkit forums or also known
         * as aadnk on GitHub.
         *
         * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
         *
         * @param data Base64 string of data containing an inventory.
         * @return Inventory created from the Base64 string.
         * @throws IOException
         */
        public static Inventory fromBase64(String data) throws IOException {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

                // Read the serialized inventory
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, (ItemStack) dataInput.readObject());
                }

                dataInput.close();
                return inventory;
            } catch (ClassNotFoundException e) {
                throw new IOException("Unable to decode class type.", e);
            }
        }

        /**
         * Gets an array of ItemStacks from Base64 string.
         *
         * <p />
         *
         * Base off of {@link #fromBase64(String)}.
         *
         * @param data Base64 string to convert to ItemStack array.
         * @return ItemStack array created from the Base64 string.
         * @throws IOException
         */
        public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack[] items = new ItemStack[dataInput.readInt()];

                // Read the serialized inventory
                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }

                dataInput.close();
                return items;
            } catch (ClassNotFoundException e) {
                throw new IOException("Unable to decode class type.", e);
            }
        }
    }
}
