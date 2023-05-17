package com.ravingarinc.biomachina.api;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class Argument {
    private final Type type;
    private final CommandSender sender;
    private final String[] args;


    /**
     *
     * @param type   The given type
     * @param sender The sender applying the argument
     * @param args   It is expected that this contains all arguments after the preceding
     *               --arg (as specified by prefix) but up to the next --arg
     */
    public Argument(final Type type, final CommandSender sender, final String[] args) {
        this.type = type;
        this.sender = sender;
        this.args = args;
    }


    /**
     * Consume the value only if its length is equal to or exceeds minArgs.
     *
     * @param value The value
     * @return The final applied argument string for storing in a database without the prefix
     */
    @Nullable
    public String consume(final Object value) {
        return type.consumer.apply(sender, value, args);
    }

    public String getPrefix() {
        return type.prefix;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(type.prefix);
        for (final String arg : args) {
            builder.append(" ");
            builder.append(arg);
        }
        return builder.toString();
    }

    public static class InvalidArgumentException extends Exception {
        public InvalidArgumentException() {
            super("Incorrect amount of arguments!");
        }
    }

    public static class Type {
        private final String prefix;
        private final int minArgs;

        private final Function<String[], List<String>> tabCompletions;
        private final TriFunction<CommandSender, Object, String[], String> consumer;

        /**
         * An argument for a command. This will transform a given type of object based on the command.
         *
         * @param prefix   The prefix in the format '--'
         * @param minArgs  The passed args length must be equal to or greater than this value
         * @param consumer The consumer of the object. This may be executed async or sync.
         */
        public Type(final String prefix, final int minArgs, final @Nullable Function<String[], List<String>> tabCompletions, final TriFunction<CommandSender, Object, String[], String> consumer) {
            this.prefix = prefix;
            this.minArgs = minArgs;
            this.consumer = consumer;
            this.tabCompletions = tabCompletions;
        }

        /**
         * Creates a filled argument.
         *
         * @param args It is expected this contains all arguments after the preceding
         *             --arg (as specified by prefix) but up to the next --arg
         * @return The filled argument
         */
        public Argument createArgument(final CommandSender sender, final String[] args) throws InvalidArgumentException {
            if (args.length < minArgs) {
                throw new InvalidArgumentException();
            }
            return new Argument(this, sender, args);
        }

        @Nullable
        public List<String> getTabCompletions(String[] args) {
            if (tabCompletions == null) {
                return null;
            }
            return tabCompletions.apply(args);
        }
    }
}
