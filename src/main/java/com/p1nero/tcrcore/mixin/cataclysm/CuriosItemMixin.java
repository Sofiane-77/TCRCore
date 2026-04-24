package com.p1nero.tcrcore.mixin.cataclysm;

import com.github.L_Ender.cataclysm.items.CuriosItem.AttributeContainer;
import com.github.L_Ender.cataclysm.items.CuriosItem.CuriosItem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;
import java.util.function.Function;

@Mixin(CuriosItem.class)
public abstract class CuriosItemMixin extends Item implements ICurioItem {

    @Shadow(remap = false)
    Function<Integer, Multimap<Attribute, AttributeModifier>> attributes;

    @Unique
    private boolean tcr$cached;

    @Shadow(remap = false)
    String attributeSlot;

    public CuriosItemMixin(Properties p_41383_) {
        super(p_41383_);
    }

    @WrapMethod(method = "getAttributeModifiers", remap = false)
    private Multimap<Attribute, AttributeModifier> tcr$getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack, Operation<Multimap<Attribute, AttributeModifier>> original) {
        if(this.getDescriptionId().contains("ring_of_grudged") && !tcr$cached) {
            this.attributes = (index) -> {
                ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                AttributeContainer holder = new AttributeContainer(ALObjects.Attributes.CRIT_DAMAGE.get(), 0.1F, AttributeModifier.Operation.ADDITION);
                String name = String.format("%s_%s", this.attributeSlot, index);
                builder.put(holder.attribute(), holder.createModifier(name));

                return builder.build();
            };
            tcr$cached = true;
        }
        return original.call(slotContext, uuid, stack);
    }

    /**
     * 统一戒指槽位
     */
    @WrapMethod(method = "withAttributes", remap = false)
    private CuriosItem tcr$withAttributes(String slot, AttributeContainer[] attributes, Operation<CuriosItem> original) {
        if(slot.equals("rings")) {
            slot = "ring";
        }
        if(slot.equals("waist")) {
            slot = "belt";
        }
        if(slot.equals("talisman")) {
            slot = "necklace";
        }
        return original.call(slot, attributes);
    }

}
