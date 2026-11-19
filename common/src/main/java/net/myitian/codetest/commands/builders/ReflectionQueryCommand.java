package net.myitian.codetest.commands.builders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.myitian.codetest.CodeTest;
import net.myitian.codetest.commands.CommandBuilder;
import net.myitian.codetest.commands.CommandFeedback;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Member;
import java.util.function.Function;

public record ReflectionQueryCommand<S>(CommandBuilder<S> builder) {
    private int ancestors(CommandContext<S> context) {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        String typeName = StringArgumentType.getString(context, "typeName");
        try {
            Class<?> $class = ClassUtils.getClass(typeName);
            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
            CodeTest.printAncestors(feedback, $class);
        } catch (ClassNotFoundException ex) {
            feedback.sendError(CodeTest.throwableToComponent(ex));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    public LiteralArgumentBuilder<S> build() {
        return builder.literal("reflection-query")
            .then(builder.literal("ancestors")
                .then(builder.argument("typeName", StringArgumentType.word())
                    .executes(this::ancestors)))
            .then(builder.literal("methods")
                .then(builder.argument("typeName", StringArgumentType.word())
                    .executes(this::methods)))
            .then(builder.literal("fields")
                .then(builder.argument("typeName", StringArgumentType.word())
                    .executes(this::fields)))
            .then(builder.literal("constructors")
                .then(builder.argument("typeName", StringArgumentType.word())
                    .executes(this::constructors)));
    }

    private int constructors(CommandContext<S> context) {
        return members(context, Class::getConstructors);
    }

    private int fields(CommandContext<S> context) {
        return members(context, Class::getDeclaredFields);
    }

    private int members(CommandContext<S> context, Function<Class<?>, Member[]> memberExtractor) {
        CommandFeedback feedback = builder.feedback().apply(context.getSource());
        String typeName = StringArgumentType.getString(context, "typeName");
        try {
            Class<?> $class = ClassUtils.getClass(typeName);
            feedback.sendFeedback(CodeTest.getClassNameComponent($class));
            CodeTest.printMembers(feedback, memberExtractor.apply($class));
        } catch (ClassNotFoundException ex) {
            feedback.sendError(CodeTest.throwableToComponent(ex));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private int methods(CommandContext<S> context) {
        return members(context, Class::getDeclaredMethods);
    }
}