package com.ravingarinc.biomachina.api;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Command argument registry for global access. Behaves as a singleton utility class.
 */
public class Registry {

    private static final Map<String, Argument.Type> ARGUMENT_TYPES = new Hashtable<>();

    public static Optional<Argument.Type> getArgument(final String prefix) {
        return Optional.ofNullable(ARGUMENT_TYPES.get(prefix));
    }
    public static void registerArgument(final String prefix, final int minArgs, final @Nullable Function<String[], List<String>> tabCompletions, final TriFunction<CommandSender, Object, String[], String> consumer) {
        ARGUMENT_TYPES.put(prefix, new Argument.Type(prefix, minArgs, tabCompletions, consumer));
    }

    public static void registerArgument(final String prefix, final int minArgs, final TriFunction<CommandSender, Object, String[], String> consumer) {
        registerArgument(prefix, minArgs, null, consumer);
    }

    public static Argument[] parseArguments(final String[] args, final CommandSender sender) throws Argument.InvalidArgumentException {
        final List<Argument> arguments = new ArrayList<>();
        Argument.Type lastArg = null;
        final List<String> lastStrings = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                if (lastArg != null) {
                    arguments.add(lastArg.createArgument(sender, lastStrings.toArray(new String[0])));
                    lastStrings.clear();
                }
                lastArg = ARGUMENT_TYPES.get(args[i]);
            } else if (lastArg != null) {
                lastStrings.add(args[i]);
            }
            if (i + 1 == args.length && lastArg != null) {
                arguments.add(lastArg.createArgument(sender, lastStrings.toArray(new String[0])));
                lastStrings.clear();
            }
        }
        return arguments.toArray(new Argument[0]);
    }
}
