package de.take_weiland.mods.commons.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Utilities for ItemStacks.</p>
 *
 * @see net.minecraft.item.ItemStack
 */
@ParametersAreNonnullByDefault
public final class ItemStacks {

    /**
     * <p>Clone the given ItemStack, or return null if it is null.</p>
     *
     * @param stack the stack to clone
     * @return a copy of the ItemStack
     */
    public static ItemStack clone(@Nullable ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    /**
     * <p>Determine if the given ItemStacks are equal.</p>
     * <p>This method checks the Item, damage value and NBT data of the stack, it does not check stack sizes.</p>
     *
     * @param a an ItemStack
     * @param b an ItemStack
     * @return true if the ItemStacks are equal
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean equal(@Nullable ItemStack a, @Nullable ItemStack b) {
        return a == b || (a != null && b != null && equalsImpl(a, b));
    }

    private static boolean equalsImpl(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata()
                && Objects.equal(a.stackTagCompound, b.stackTagCompound);
    }

    /**
     * <p>Determine if the given ItemStacks are identical.</p>
     * <p>This method checks the Item, damage value, NBT data and stack size of the stacks.</p>
     * @param a an ItemStack
     * @param b an ItemStack
     * @return true if the ItemStacks are identical
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean identical(@Nullable ItemStack a, @Nullable ItemStack b) {
        return a == b || (a != null && b != null && equalsImpl(a, b) && a.stackSize == b.stackSize);
    }

    /**
     * <p>Compute a hash code for the given ItemStack.</p>
     *
     * @param stack the ItemStack, may be null
     * @return a hash code
     */
    public static int hash(@Nullable ItemStack stack) {
        if (stack == null) {
            return 0;
        } else {
            int result = stack.getItem().hashCode();
            result = 31 * result + (stack.getMetadata() << 16);
            result = 31 * result + stack.stackSize;
            result = 31 * result + (stack.stackTagCompound != null ? stack.stackTagCompound.hashCode() : 0);
            return result;
        }
    }

    /**
     * <p>Thrown by {@link #parse(String)} in case of invalid input.</p>
     */
    public static final class InvalidStackDefinition extends Exception {

        InvalidStackDefinition(String message) {
            super(message);
        }

        InvalidStackDefinition(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * <p>Parse a textual definition of an ItemStack from e.g. a configuration file. The format is
     * {@code 13xminecraft:stone@4} for an ItemStack of stone with stack size 13 and metadata 4. Metadata and stack size
     * are optional, defaulting to 0 and 1 respectively. If the domain (usually Mod ID) is "minecraft" it can be left out.</p>
     *
     * @param definition the definition
     * @return the parsed ItemStack
     * @throws InvalidStackDefinition if the definition is invalid
     */
    public static ItemStack parse(String definition) throws InvalidStackDefinition {
        Item item;
        int meta;
        int stackSize;

        String withoutLeadingDigits = CharMatcher.DIGIT.trimLeadingFrom(definition);
        boolean hasStackSize = !withoutLeadingDigits.contentEquals(definition);
        if (hasStackSize) {
            if (withoutLeadingDigits.charAt(0) == 'x') {
                withoutLeadingDigits = withoutLeadingDigits.substring(1);
            } else {
                throw new InvalidStackDefinition("Starts with digits but missing x afterwards");
            }
            String stackSizeString = definition.substring(0, definition.length() - withoutLeadingDigits.length() - 1);
            try {
                stackSize = Integer.parseInt(stackSizeString);
            } catch (NumberFormatException e) {
                throw new InvalidStackDefinition(String.format("Invalid stackSize %s", stackSizeString));
            }
        } else {
            stackSize = 1;
        }

        int atIdx = withoutLeadingDigits.indexOf('@');
        if (atIdx < 0) {
            item = tryParseItem(withoutLeadingDigits);
            meta = 0;
        } else {
            item = tryParseItem(withoutLeadingDigits.substring(0, atIdx));
            meta = tryParseMetadata(withoutLeadingDigits.substring(atIdx + 1));
        }

        return new ItemStack(item, stackSize, meta);
    }

    private static Item tryParseItem(String s) throws InvalidStackDefinition {
        Item item = (Item) Item.itemRegistry.getObject(s);
        if (item == null) {
            throw new InvalidStackDefinition(String.format("Unknown Item %s", s));
        }
        return item;
    }

    private static int tryParseMetadata(String s) throws InvalidStackDefinition {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidStackDefinition(String.format("Invalid metadata %s", s), e);
        }
    }

    /**
     * <p>Tests if the first ItemStack can be fully merged into the second one.</p>
     *
     * @param from the ItemStack to merge, may be null
     * @param into the ItemStack to merge into, may be null
     * @return true if the first ItemStack can be fully merged into the second one
     */
    public static boolean fitsInto(@Nullable ItemStack from, @Nullable ItemStack into) {
        return from == null || into == null || fitsIntoImpl(from, into);
    }

    private static boolean fitsIntoImpl(ItemStack from, ItemStack into) {
        return equalsImpl(from, into) && from.stackSize + into.stackSize <= into.getMaxStackSize();
    }


    /**
     * <p>Tries to merge the two ItemStacks if they are {@linkplain #equal(ItemStack, ItemStack) equal}. See {@link #merge(ItemStack, ItemStack, boolean)} for details.</p>
     * @param from the the ItemStack to transfer from
     * @param into the ItemStack to transfer into
     * @return the resulting ItemStack
     */
    public static ItemStack merge(@Nullable ItemStack from, @Nullable ItemStack into) {
        return merge(from, into, false);
    }

    /**
     * <p>Tries to merge the two ItemStacks.</p>
     * <p><ul>
     *     <li>If {@code from} is null, returns {@code into}.</li>
     *     <li>If {@code into} is null, sets {@code from}'s stackSize to 0 and returns a copy of the original {@code from}.</li>
     *     <li>If neither {@code from} nor {@code into} are null and {@code force} is true or {@link #equal(ItemStack, ItemStack)}
     *     returns true for {@code from} and {@code into} determines the number of items to transfer by {@code min(into.maxStackSize - into.stackSize, from.stackSize}.
     *     Then increases {@code into.stackSize} by the number of items to transfer and decreases {@code from.stackSize} by the number of items to transfer. Then returns
     *     {@code into}.</li>
     *     <li>Otherwise does nothing and returns {@code into}.</li>
     * </ul></p>
     * @param from the ItemStack to transfer from
     * @param into the ItemStack to transfer into
     * @param force whether to force the transfer even if {@link #equal(ItemStack, ItemStack)} returns false for the two ItemStacks
     *
     * @return the resulting ItemStack
     */
    public static ItemStack merge(@Nullable ItemStack from, @Nullable ItemStack into, boolean force) {
        if (from == null) {
            return into;
        }

        if (into == null) {
            ItemStack result = from.copy();
            from.stackSize = 0;
            return result;
        }

        if (force || equalsImpl(from, into)) {
            int transferCount = Math.min(into.getMaxStackSize() - into.stackSize, from.stackSize);
            from.stackSize -= transferCount;
            into.stackSize += transferCount;
        }
        return into;
    }

    /**
     * <p>Utility method to get the Block associated with the Item in the given ItemStack.</p>
     * @param stack the ItemStack
     * @return the Block associated with the ItemStack's Item or null if the Item has no associated Block
     */
    public static Block getBlock(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem());
    }

    /**
     * <p>Converts an empty ItemStack to {@code null}. Leaves all other ItemStacks untouched.</p>
     * @param stack the ItemStack or null
     * @return the ItemStack
     */
    @Contract("null -> null")
    public static ItemStack emptyToNull(@Nullable ItemStack stack) {
        return stack == null || stack.stackSize <= 0 ? null : stack;
    }

    /**
     * <p>Get the NBTTagCompound associated with the given ItemStack and initializes it if necessary.</p>
     * @param stack the ItemStack
     * @return the NBTTagCompound associated with the ItemStack
     */
    public static NBTTagCompound getNbt(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        return stack.stackTagCompound;
    }

    /**
     * <p>Get the NBTTagCompound with the given key from the NBTTagCompound associated with the given ItemStack and initializes
     * both if necessary.</p>
     * @param stack the ItemStack
     * @param key the key
     * @return the NBTTagCompound
     */
    public static NBTTagCompound getNbt(ItemStack stack, String key) {
        return NBT.getOrCreateCompound(getNbt(stack), key);
    }

    /**
     * <p>Check if the given ItemStack contains the given Item.</p>
     * @param stack the ItemStack or null
     * @param item the Item
     * @return true if the ItemStack is not null and contains the given Item
     */
    public static boolean is(@Nullable ItemStack stack, Item item) {
        return stack != null && stack.getItem() == item;
    }

    /**
     * <p>Check if the given ItemStack contains the given Item with the given metadata.</p>
     * @param stack the ItemStack
     * @param item the Item
     * @param meta the metadata
     * @return true if the ItemStack is not null and contains the given Item with the given metadata
     */
    public static boolean is(@Nullable ItemStack stack, Item item, int meta) {
        return stack != null && stack.getItem() == item && stack.getMetadata() == meta;
    }

    /**
     * <p>Check if the given ItemStack contains the given Block.</p>
     * @param stack the ItemStack
     * @param block the Block
     * @return true if the ItemStack is not null and contains the given Block
     */
    public static boolean is(@Nullable ItemStack stack, Block block) {
        return stack != null && getBlock(stack) == block;
    }

    /**
     * <p>Check if the given ItemStack contains the given Block with the given metadata.</p>
     * @param stack the ItemStack
     * @param block the Block
     * @param meta the metadata
     * @return true if the ItemStack is not null and contains the given Block with the given metadata
     */
    public static boolean is(@Nullable ItemStack stack, Block block, int meta) {
        return stack != null && getBlock(stack) == block && stack.getMetadata() == meta;
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum<T> & Subtype> void registerSubstacks(String baseName, Item item) {
        MetadataProperty<T> prop = ((HasSubtypes<T>) item).subtypeProperty();
        for (T type : prop.values()) {
            ItemStack stack = new ItemStack(item);
            stack.setMetadata(prop.toMeta(type, 0));

            String name = baseName + "." + type.subtypeName();
            GameRegistry.registerCustomItemStack(name, stack);
        }
    }

    private ItemStacks() {
    }
}
