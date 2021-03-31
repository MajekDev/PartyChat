package dev.majek.pc.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides utilities for a more complex text component formatting system.
 */
public class TextUtils {
    private static final char VALUE_MARKER = '%';
    private static final char COLOR_CHAR = '&';
    private static final char SECTION_START = '{';
    private static final char FUNCTION_CHAR = '$';

    private static final List<String> formats = Arrays.asList(
            "obfuscated", "bold", "strikethrough", "underline", "italic"
    );

    /**
     * Parses the given input text and substitutes in the given values and sends the result to the given command sender.
     *
     * @param sender the recipient of the formatted message.
     * @param input  the input text.
     * @param values the values to substitute in.
     */
    public static void sendFormatted(CommandSender sender, String input, Object... values) {
        sender.spigot().sendMessage(format(input, values));
    }

    /**
     * Parses the given input text and substitutes in the given values and sends the result to the given command sender.
     *
     * @param sender the recipient of the formatted message.
     * @param perm   the permission of the sender (whether or not they can use all of parseExpression)
     * @param input  the input text.
     * @param values the values to substitute in.
     */
    public static void sendFormatted(CommandSender sender, boolean perm, String input, Object... values) {
        if (!perm)
            sender.spigot().sendMessage(format(input, false, values));
        else
            sender.spigot().sendMessage(format(input, values));
    }

    /**
     * Parses the given input text and substitutes in the given values and sends the result to the given player as the
     * given message type.
     *
     * @param player the recipient of the formatted message.
     * @param type   the message type.
     * @param input  the input text.
     * @param values the values to substitute in.
     */
    public static void sendFormatted(Player player, ChatMessageType type, String input, Object... values) {
        player.spigot().sendMessage(type, format(input, values));
    }

    /**
     * Parses the given input text and substitutes in the given values and returns the result as an array of base
     * components.
     *
     * @param input  the input text.
     * @param values the values to substitute in.
     * @return the parsed input text as an array of base components.
     */
    public static BaseComponent[] format(String input, Object... values) {
        return parseExpression(new Pair<>(ChatColor.WHITE, new ArrayList<>()), true, insertValues(input, values), values);
    }

    public static BaseComponent[] format(String input, boolean perm, Object... values) {
        return parseExpression(new Pair<>(ChatColor.WHITE, new ArrayList<>()), perm, insertValues(input, values), values);
    }

    /**
     * Parses the given input text and returns the result as an array of base components.
     *
     * @param input the input text.
     * @return the parsed input text as an array of base components.
     */
    public static BaseComponent[] format(String input) {
        return parseExpression(new Pair<>(ChatColor.WHITE, new ArrayList<>()), true, input);
    }

    /**
     * Inserts the given values into the raw string where the sequence %x occurs where x is the hexadecimal index of the
     * value to insert.
     *
     * @param raw    the raw string.
     * @param values the values to insert.
     * @return the string with the values inserted.
     */
    public static String insertValues(String raw, Object... values) {
        if (values.length == 0)
            return raw;

        StringBuilder sb = new StringBuilder(raw.length());
        char[] chars = raw.toCharArray();
        char cur, next;

        for (int i = 0; i < chars.length; ++i) {
            cur = chars[i];
            next = i < chars.length - 1 ? chars[i + 1] : '\0';

            // Ignore escaped characters
            if (cur == '\\' && next == VALUE_MARKER) {
                sb.append(VALUE_MARKER);
                ++i;
            }
            // Insert a value
            else if (cur == VALUE_MARKER) {
                // Use hex for more indices
                int index = Character.digit(next, 16);
                if (index < values.length)
                    sb.append(values[index]);
                ++i;
            }
            // Just append the next character
            else sb.append(cur);
        }

        return sb.toString();
    }

    /**
     * Escape all function chars so they can be printed as their literal
     *
     * @param input the string to escape
     * @return the escaped string
     */
    public static String escapeExpression(String input) {
        if (input.isEmpty())
            return input;
        // Escaped Expression
        StringBuilder escapedExpression = new StringBuilder();
        // Parsed expression
        char[] chars = input.toCharArray();
        char cur;

        escapedExpression.append("{");
        for (int i = 0; i < chars.length; ++i) {
            cur = chars[i];
            // Escape special characters
            if (cur == '\\' || cur == COLOR_CHAR || cur == FUNCTION_CHAR || cur == SECTION_START || cur == VALUE_MARKER)
                escapedExpression.append("\\");
            escapedExpression.append(cur);
        }
        escapedExpression.append("}");
        return escapedExpression.toString();
    }

    /**
     * Parses the given expression. The format variable is as follows: text color, additive format colors (such as bold,
     * italic, etc.). The values variable is the list of values potentially substituted into the text, and is used by
     * the inflecting function.
     *
     * @param format the format scope (color, formats).
     * @param input  the input string to parse.
     * @param values the object insertions (used by the inflect function).
     * @return a base component array resulting from the given expression.
     */
    private static BaseComponent[] parseExpression(Pair<ChatColor, List<ChatColor>> format, boolean perm, String input,
                                                   Object... values) {
        // Current component text
        StringBuilder component = new StringBuilder();
        // Parsed expression
        List<BaseComponent> expr = new ArrayList<>();
        char[] chars = input.toCharArray();
        char cur, next;

        for (int i = 0; i < chars.length; ++i) {
            cur = chars[i];
            next = i + 1 < chars.length ? chars[i + 1] : '\0';

            // Escape special characters
            if (cur == '\\' && (next == '\\' || next == COLOR_CHAR || next == FUNCTION_CHAR || next == SECTION_START || next == VALUE_MARKER)) {
                component.append(next);
                ++i;
                continue;
            }

            // ChatColor fix
            if (
                    i + 13 < chars.length && cur == '§' && next == 'x' &&
                    chars[i +  2] == '§' && chars[i +  4] == '§' && chars[i +  6] == '§' &&
                    chars[i +  8] == '§' && chars[i + 10] == '§' && chars[i + 12] == '§'
            ) {
                // Finish off the current component if it was started
                if (component.length() > 0) {
                    expr.add(makeComponent(format, component.toString()));
                    component.setLength(0);
                }

                format.setFirst(ChatColor.of("#" +
                        chars[i +  3] + chars[i +  5] + chars[i +  7] +
                        chars[i +  9] + chars[i + 11] + chars[i + 13]
                ));
                i += 13;
                continue;
            }

            switch (cur) {
                // Update the format colors in this expression, causes the creation of a new component
                case COLOR_CHAR: {
                    // Finish off the current component if it was started
                    if (component.length() > 0) {
                        expr.add(makeComponent(format, component.toString()));
                        component.setLength(0);
                    }

                    // Get the color arguments
                    Pair<String, Integer> args = getEnclosed(i + 1, input);
                    if (args.getFirst() == null)
                        throw new SyntaxException("Bracket mismatch", i, input);

                    // Move the character index to the end of the arguments
                    i = args.getSecond() - 1;

                    // Parse the color arguments
                    for (String colorText : args.getFirst().split(",")) {
                        // This removes formats
                        boolean negated = colorText.startsWith("!");
                        ChatColor color = Utils.safeValueOf(ChatColor::of, (negated ? colorText.substring(1)
                                : colorText).toUpperCase());

                        if (color == null)
                            throw new SyntaxException("Invalid color code: " + colorText);

                        if (formats.contains(color.getName())) {
                            if (negated)
                                format.getSecond().remove(color);
                            else if (!format.getSecond().contains(color))
                                format.getSecond().add(color);
                        } else
                            format.setFirst(color);
                    }

                    continue;
                }

                // Start a new expression
                case SECTION_START: {
                    // Finish off the current component if it was started
                    if (component.length() > 0) {
                        expr.add(makeComponent(format, component.toString()));
                        component.setLength(0);
                    }

                    // Get the expression text
                    Pair<String, Integer> section = getEnclosed(i, input);
                    if (section.getFirst() == null)
                        throw new SyntaxException("Bracket mismatch", i, input);

                    // Move the character index to the end of the expression
                    i = section.getSecond() - 1;

                    // Transfer the current formatting the the next expression in a scope-like manner
                    BaseComponent[] expression = parseExpression(
                            new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                            section.getFirst(),
                            values
                    );
                    expr.addAll(Arrays.asList(expression));

                    continue;
                }

                // Functions
                case FUNCTION_CHAR: {
                    // Get the args
                    Pair<String, Integer> rawArgs = getEnclosed(i + 1, input);
                    if (rawArgs.getFirst() == null)
                        throw new SyntaxException("Bracket mismatch", i, input);
                    List<String> args = new ArrayList<>();

                    // Build the args list
                    StringBuilder currentArg = new StringBuilder();
                    char[] cs = rawArgs.getFirst().toCharArray();
                    int depthCurly = 0, depthRound = 0;
                    for (char c : cs) {
                        // We're in the outermost scope and the delimiter was reached
                        if (depthCurly == 0 && depthRound == 0 && c == ',') {
                            args.add(currentArg.toString());
                            currentArg.setLength(0);
                        } else {
                            // Update scope values
                            switch (c) {
                                case '(':
                                    ++depthRound;
                                    break;
                                case '{':
                                    ++depthCurly;
                                    break;
                                case ')':
                                    --depthRound;
                                    break;
                                case '}':
                                    --depthCurly;
                                    break;
                            }

                            currentArg.append(c);
                        }
                    }

                    // Check for syntax errors
                    if (depthCurly > 0 || depthRound > 0)
                        throw new SyntaxException("Bracket mismatch in function arguments", i, input);

                    // Add the last argument
                    args.add(currentArg.toString());

                    // Move the cursor to the end of the function text
                    i = rawArgs.getSecond() - 1;

                    // Finish off the current component if it was started and we're not inflecting a word
                    if (!"inflect".equalsIgnoreCase(args.get(0)) && component.length() > 0) {
                        expr.add(makeComponent(format, component.toString()));
                        component.setLength(0);
                    }

                    // Args: link, text
                    if ("link".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 3)
                            throw new SyntaxException("Link function usage: $(link,url,text)");

                        // Parse the text
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );

                        // Apply the link
                        for (BaseComponent bc : text)
                            bc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, args.get(1)));

                        expr.addAll(Arrays.asList(text));
                    }
                    // Args: hover text, base text
                    else if ("hover".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 3)
                            throw new SyntaxException("Hover function usage: $(hover,hoverText,text)");

                        // Parse both texts
                        BaseComponent[] hover = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(1).startsWith("{") ? getEnclosed(0, args.get(1)).getFirst()
                                        : args.get(1), values
                        );
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );

                        // Apply the hover text
                        for (BaseComponent bc : text)
                            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));

                        expr.addAll(Arrays.asList(text));
                    }
                    // Args: json item, base text
                    else if ("item".equalsIgnoreCase(args.get(0))) {
                        if (args.size() < 3)
                            throw new SyntaxException("Item function usage: $(item,jsonItem,text)");

                        // Parse the base test
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );
                        // Parse the item json
                        BaseComponent[] hoverEventComponents = new BaseComponent[]{
                                new TextComponent(args.get(1)) // The only element of the hover events basecomponents is the item json
                        };

                        // Apply the item tool tip
                        for (BaseComponent bc : text)
                            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));

                        expr.addAll(Arrays.asList(text));
                    }
                    // Args: value index, word
                    else if ("inflect".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 4)
                            throw new SyntaxException("Conjugate function usage: $(inflect,noun|verb,argIndex,word)");

                        // Whether or not we're inflecting a noun or verb
                        boolean noun = "noun".equalsIgnoreCase(args.get(1));

                        // Parse the value index
                        int index;
                        try {
                            index = Integer.parseInt(args.get(2));
                        } catch (NumberFormatException ex) {
                            throw new SyntaxException("Invalid index: " + args.get(2));
                        }

                        // Check the index and value
                        if (index > values.length || index < 0)
                            throw new SyntaxException("Index is out of bounds: " + index);
                        if (!(values[index] instanceof Number))
                            throw new SyntaxException("The value at the index provided is not a number.");

                        // Apply the "s" if needed
                        component.append(args.get(3));
                        int count = ((Number) values[index]).intValue();
                        if ((noun && count != 1) || (!noun && count == 1))
                            component.append('s');
                    }
                    // Args: command, text
                    else if ("command".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 3)
                            throw new SyntaxException("Command function usage: $(command,command,text)");

                        // Parse the text
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(),
                                        new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );

                        // Apply the command
                        for (BaseComponent bc : text)
                            bc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, args.get(1)));

                        expr.addAll(Arrays.asList(text));
                    }
                    // Args: command, hover text, base text
                    else if ("hovercmd".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 4)
                            throw new SyntaxException("Hover-command function usage: " +
                                    "$(hovercmd,command,hoverText,text)");

                        // Parse both texts
                        BaseComponent[] hover = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(3).startsWith("{") ? getEnclosed(0, args.get(3)).getFirst()
                                        : args.get(3), values
                        );

                        // Apply the command and hover text
                        for (BaseComponent bc : text) {
                            bc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, args.get(1)));
                            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                        }

                        expr.addAll(Arrays.asList(text));
                    }
                    // Args: link, hover text, base text
                    else if ("hoverlink".equalsIgnoreCase(args.get(0)) && perm) {
                        if (args.size() < 4)
                            throw new SyntaxException("Hover-link function usage: $(hoverlink,url,hoverText,text)");

                        // Parse both texts
                        BaseComponent[] hover = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(2).startsWith("{") ? getEnclosed(0, args.get(2)).getFirst()
                                        : args.get(2), values
                        );
                        BaseComponent[] text = parseExpression(
                                new Pair<>(format.getFirst(), new ArrayList<>(format.getSecond())), perm,
                                args.get(3).startsWith("{") ? getEnclosed(0, args.get(3)).getFirst()
                                        : args.get(3), values
                        );

                        // Apply the link and hover text
                        for (BaseComponent bc : text) {
                            bc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, args.get(1)));
                            bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                        }

                        expr.addAll(Arrays.asList(text));
                    } else {
                        if (perm)
                            throw new SyntaxException("Invalid function: " + args.get(0));
                        else {
                            expr.add(makeComponent(format, "$("+rawArgs.getFirst()+")"));
                            return expr.toArray(new BaseComponent[0]);
                        }
                    }
                    continue;
                }

                // Normal text
                default:
                    component.append(cur);
            }

        }

        // Get the last component
        if (component.length() > 0)
            expr.add(makeComponent(format, component.toString()));

        return expr.toArray(new BaseComponent[0]);
    }

    /**
     * Create a component with the given color and formatting and text
     *
     * @param format the formatting for the component (color, formats).
     * @param text   the component text.
     * @return the constructed component.
     */
    private static TextComponent makeComponent(Pair<ChatColor, List<ChatColor>> format, String text) {
        TextComponent tc = new TextComponent(text);

        // Color
        tc.setColor(format.getFirst());

        // Formats
        tc.setBold(format.getSecond().contains(ChatColor.BOLD));
        tc.setItalic(format.getSecond().contains(ChatColor.ITALIC));
        tc.setUnderlined(format.getSecond().contains(ChatColor.UNDERLINE));
        tc.setStrikethrough(format.getSecond().contains(ChatColor.STRIKETHROUGH));
        tc.setObfuscated(format.getSecond().contains(ChatColor.MAGIC));

        // Events
        tc.setClickEvent(null);
        tc.setHoverEvent(null);

        return tc;
    }

    /**
     * Gets the text enclosed by curved brackets or curly brackets, and returns the text inside the brackets and the
     * index of the character after the last bracket. If the end of the string is encountered before the bracket is
     * closed off, then {null, -1} is returned.
     *
     * @param start  the start index.
     * @param string the string to get the enclosed value.
     * @return a pair, where the first value is the enclosed string and the second value is the index of the character
     * after the closing bracket of the enclosed string.
     */
    private static Pair<String, Integer> getEnclosed(int start, String string) {
        boolean curved = string.charAt(start) == '('; // ()s or {}s
        int depth = 1, i = start + 1;

        // Exits when there are no pairs of open brackets
        while (depth > 0) {
            // Avoid index out of bound errors
            if (i == string.length())
                return new Pair<>(null, -1);

            char c = string.charAt(i++);

            // We've closed off a pair
            if (c == (curved ? ')' : '}')) {
                --depth;
            }
            // We've started a pair
            else if (c == (curved ? '(' : '{')) {
                ++depth;
            }
        }

        // Return the stuff inside the brackets, and the index of the char after the last bracket
        return new Pair<>(string.substring(start + 1, i - 1), i);
    }

    /**
     * Represents an exception encountered while parsing the syntax supported by this utility class.
     */
    public static class SyntaxException extends RuntimeException {
        public SyntaxException(String msg, int index, String input) {
            super(msg + " near \"" + input.substring(index, Math.min(index + 8, input.length())) + "\"...");
        }

        public SyntaxException(String msg) {
            super(msg);
        }
    }
}
