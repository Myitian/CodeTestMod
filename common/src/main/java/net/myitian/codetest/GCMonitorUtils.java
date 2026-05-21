package net.myitian.codetest;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GCMonitorUtils {
    private static final MutableComponent HEADER_PART = Component.literal("=======")
        .withStyle(ChatFormatting.WHITE);
    private static final MutableComponent HEADER = HEADER_PART.copy()
        .append(Component.literal(" GC Status Report ").withStyle(ChatFormatting.YELLOW))
        .append(HEADER_PART);
    private static final MutableComponent FOOTER = Component.literal("-------")
        .withStyle(ChatFormatting.GRAY)
        .append(Component.literal(" #").withStyle(ChatFormatting.DARK_GRAY));
    private static final MutableComponent COUNT = Component.literal("- Collection Count: ")
        .withStyle(ChatFormatting.WHITE);
    private static final MutableComponent TIME = Component.literal("- Collection Time: ")
        .withStyle(ChatFormatting.WHITE)
        .append(CommonComponents.EMPTY)
        .append(" ms");
    private static final MutableComponent ID = Component.literal("- Last GC ID: ")
        .withStyle(ChatFormatting.WHITE);
    private static final MutableComponent DURATION = Component.literal("- Last GC Duration: ")
        .withStyle(ChatFormatting.WHITE)
        .append(CommonComponents.EMPTY) // 0
        .append(" ms");
    private static final MutableComponent MEMORY_USAGE_CHANGES = Component.literal("- Memory Usage Changes:")
        .withStyle(ChatFormatting.WHITE);
    private static final MutableComponent MEMORY_USAGE_CHANGES_ITEM = Component.literal("  [")
        .withStyle(ChatFormatting.DARK_GRAY)
        .append(CommonComponents.EMPTY) // 0
        .append("]")
        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
        .append(CommonComponents.EMPTY) // 3
        .append(Component.literal(" -> ").withStyle(ChatFormatting.WHITE))
        .append(CommonComponents.EMPTY) // 5
        .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
        .append(Component.literal("Max").withStyle(ChatFormatting.WHITE))
        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
        .append(CommonComponents.EMPTY) // 9
        .append(Component.literal(" )").withStyle(ChatFormatting.GRAY));
    private static final Component COMMA = Component.literal(",").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component ZERO = Component.literal("0").withStyle(ChatFormatting.AQUA);
    private static final Component MIN_VALUE = Component.literal("-9").withStyle(ChatFormatting.AQUA)
        .append(COMMA).append("223")
        .append(COMMA).append("372")
        .append(COMMA).append("036")
        .append(COMMA).append("854")
        .append(COMMA).append("775")
        .append(COMMA).append("808");

    public static void printGCStats(CommandFeedback feedback) {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        feedback.sendFeedback(HEADER);
        for (int i = 0, gcBeansSize = gcBeans.size(); i < gcBeansSize; i++) {
            GarbageCollectorMXBean bean = gcBeans.get(i);
            feedback.sendFeedback(Component.literal(bean.getName()).withStyle(ChatFormatting.GREEN));
            feedback.sendFeedback(COUNT.copy()
                .append(Component.literal(String.valueOf(bean.getCollectionCount()))
                    .withStyle(ChatFormatting.AQUA)));
            MutableComponent time = TIME.copy();
            time.getSiblings().set(0, Component.literal(String.valueOf(bean.getCollectionCount()))
                .withStyle(ChatFormatting.AQUA));
            feedback.sendFeedback(time);
            try {
                printExtendedStats(bean, feedback);
            } catch (Exception ignored) {
            }
            feedback.sendFeedback(FOOTER.copy()
                .append(Component.literal(String.valueOf(i + 1)).withStyle(ChatFormatting.BLUE)));
        }
    }

    private static void printExtendedStats(GarbageCollectorMXBean bean, CommandFeedback feedback) {
        if (!(bean instanceof com.sun.management.GarbageCollectorMXBean sunBean)) {
            return;
        }
        com.sun.management.GcInfo gcInfo = sunBean.getLastGcInfo();
        if (gcInfo == null) {
            return;
        }
        feedback.sendFeedback(ID.copy()
            .append(Component.literal(String.valueOf(gcInfo.getId()))
                .withStyle(ChatFormatting.AQUA)));
        MutableComponent duration = DURATION.copy();
        duration.getSiblings().set(0, Component.literal(String.valueOf(gcInfo.getDuration()))
            .withStyle(ChatFormatting.AQUA));
        feedback.sendFeedback(duration);
        feedback.sendFeedback(MEMORY_USAGE_CHANGES);
        for (Map.Entry<String, MemoryUsage> entry : gcInfo.getMemoryUsageBeforeGc().entrySet()) {
            String poolName = entry.getKey();
            MemoryUsage after = gcInfo.getMemoryUsageAfterGc().get(poolName);
            if (after != null) {
                MutableComponent item = MEMORY_USAGE_CHANGES_ITEM.copy();
                List<Component> siblings = item.getSiblings();
                siblings.set(0, Component.literal(poolName)
                    .withStyle(ChatFormatting.WHITE));
                siblings.set(3, formatGroupedNumber(entry.getValue().getUsed()));
                siblings.set(5, formatGroupedNumber(after.getUsed()));
                siblings.set(9, formatGroupedNumber(after.getMax()));
                feedback.sendFeedback(item);
            }
        }
    }

    private static Component formatGroupedNumber(long number) {
        if (number == 0) {
            return ZERO;
        } else if (number == Long.MIN_VALUE) {
            return MIN_VALUE;
        }
        boolean isNegative = number < 0;
        number = Math.abs(number);
        char[] buffer = new char[3];
        List<Component> parts = new ArrayList<>(6);
        while (number >= 1000) {
            buffer[2] = (char) ((number % 10) | '0');
            buffer[1] = (char) ((number / 10 % 10) | '0');
            buffer[0] = (char) ((number / 100 % 10) | '0');
            parts.add(Component.literal(String.copyValueOf(buffer)));
            number /= 1000;
        }
        MutableComponent result = Component.literal(String.valueOf(isNegative ? -number : number))
            .withStyle(ChatFormatting.AQUA);
        for (int i = parts.size(); i-- > 0; ) {
            result.append(COMMA).append(parts.get(i));
        }
        return result;
    }
}