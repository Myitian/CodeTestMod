package net.myitian.codetest;

import net.minecraft.world.effect.MobEffectInstance;

public enum EffectInstanceComparator implements java.util.Comparator<MobEffectInstance> {
    INSTANCE;

    @Override
    public int compare(MobEffectInstance o1, MobEffectInstance o2) {
        int i = o1.getEffect().value().getCategory().compareTo(o2.getEffect().value().getCategory());
        return i != 0 ? i : o2.compareTo(o1);
    }
}
