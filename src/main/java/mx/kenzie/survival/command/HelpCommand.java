package mx.kenzie.survival.command;

import mx.kenzie.centurion.*;
import mx.kenzie.clockwork.collection.ClockList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class HelpCommand extends MinecraftCommand {
    public static final TypedArgument<MinecraftCommand> COMMAND = new CommandArgument();
    private static final ArgumentArgument LOCAL_ARGUMENT = new ArgumentArgument() {
        private boolean setup;

        private void setup() {
            if (setup) return;
            this.setup = true;
            this.defaultArguments.clear();
            final ClockList<MinecraftCommand> commands = HelpCommand.getCommands();
            final ClockList<String> strings = new ClockList<>();
            for (MinecraftCommand command : commands) {
                for (ArgumentContainer container : command.arguments()) {
                    for (Argument<?> argument : container.arguments()) {
                        if (argument.literal() && argument.description() == null) continue;
                        this.registerDefault(argument);
                        strings.add(argument.label());
                    }
                }
            }
            this.possibilities = strings.toArray();
        }

        @Override
        protected Map<String, Argument<?>> getArguments() {
            this.setup();
            return defaultArguments;
        }

        @Override
        public String[] possibilities() {
            this.setup();
            return possibilities;
        }

    };
    protected final Map<MinecraftCommand, Map<String, Argument<?>>> arguments = new HashMap<>();

    public HelpCommand() {
        super("Get some help.");
    }

    protected static ClockList<MinecraftCommand> getCommands() {
        final ClockList<MinecraftCommand> list = new ClockList<>();
        final Collection<org.bukkit.command.Command> commands = Bukkit.getCommandMap().getKnownCommands().values();
        for (org.bukkit.command.Command stub : commands) {
            if (!(stub instanceof PluginCommand plugin)) continue;
            if (!(plugin.getExecutor() instanceof MinecraftCommand command)) continue;
            list.add(command);
        }
        return list;
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("help")
                .arg("commands", this::listCommands)
                .arg("command", COMMAND.described("The command to inspect."), this::viewCommand)
                .arg("command", COMMAND.described("The command to inspect."), LOCAL_ARGUMENT.described("The argument type to inspect."), this::helpCommandArgument)
                .arg("argument", LOCAL_ARGUMENT.described("The argument type to inspect."), this::helpArgument);
    }

    protected Argument<?> getArgument(MinecraftCommand command, Argument<?> argument) {
        if (!arguments.containsKey(command)) {
            final Map<String, Argument<?>> map = new HashMap<>();
            for (ArgumentContainer container : command.arguments()) {
                for (Argument<?> thing : container.arguments()) map.put(thing.label(), thing);
            }
            this.arguments.put(command, map);
        }
        return arguments.get(command).getOrDefault(argument.label(), argument);
    }

    protected Component print(Argument<?> argument) {
        return super.print(argument, 0);
    }

    protected Component print(ArgumentContainer container) {
        return super.print(container, 0);
    }

    protected void printHelp(Argument<?> argument, CommandSender sender) {
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        if (argument.description() != null) sender.sendMessage(Component.textOfChildren(
                text("Description: ", profile.dark()),
                text(argument.description(), profile.light()))
        );
        else sender.sendMessage(text("No description provided.", profile.dark()));
        if (argument instanceof TypedArgument<?> typed) sender.sendMessage(Component.textOfChildren(
                text("Accepts a ", profile.dark()),
                text(typed.type.getSimpleName(), profile.highlight()),
                text(".", profile.dark())
        ));
        if (argument.optional()) {
            if (argument instanceof TypedArgument<?> typed && typed.lapse() != null) {
                sender.sendMessage(Component.textOfChildren(
                        text("Defaults to ", profile.dark()),
                        text(String.valueOf(typed.lapse()), profile.highlight()),
                        text(".", profile.dark())
                ));
            } else sender.sendMessage(text("Optional.", profile.dark()));
        }
        if (argument instanceof CompoundArgument<?> compound) {
            sender.sendMessage(text("Accepted inputs: ", profile.dark()));
            for (ArgumentContainer container : compound.arguments()) sender.sendMessage(this.print(container));
        }
        //</editor-fold>
    }

    protected CommandResult helpCommandArgument(CommandSender sender, Arguments arguments) {
        final MinecraftCommand command = arguments.get(0);
        final Argument<?> argument = this.getArgument(command, arguments.get(1));
        if (argument == null) return CommandResult.WRONG_INPUT;
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Help for ", profile.dark()),
                text("/", profile.pop()),
                text(command.label(), profile.highlight()),
                text("'s ", profile.dark()),
                this.print(argument)));
        this.printHelp(argument, sender);
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult helpArgument(CommandSender sender, Arguments arguments) {
        final Argument<?> argument = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Help for ", profile.dark()),
                this.print(argument)));
        this.printHelp(argument, sender);
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult viewCommand(CommandSender sender, Arguments arguments) {
        final MinecraftCommand command = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Options for ", profile.dark()),
                text("/", profile.pop()),
                text(command.label(), profile.highlight()),
                text(".", profile.dark())
        ));
        //</editor-fold>
        for (ArgumentContainer container : command.arguments()) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            final Component hover;
            final ClickEvent click;
            if (container.hasInput()) {
                hover = Component.text("Click to Suggest");
                final StringBuilder text = new StringBuilder("/" + command.label());
                for (Argument<?> argument : container.arguments()) {
                    if (!argument.literal()) break;
                    text.append(' ').append(argument.label());
                }
                click = ClickEvent.suggestCommand(text.toString());
            } else {
                hover = Component.text("Click to Run");
                click = ClickEvent.runCommand("/" + command.label() + container);
            }
            sender.sendMessage(Component.textOfChildren(
                    Component.space(),
                    text("/", profile.pop()),
                    text(command.label(), profile.highlight()),
                    this.print(container)
            ).hoverEvent(hover).clickEvent(click));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    protected CommandResult listCommands(CommandSender sender, Arguments arguments) {
        final Collection<org.bukkit.command.Command> commands = Bukkit.getCommandMap().getKnownCommands().values();
        final ColorProfile profile = this.getProfile();
        sender.sendMessage(text("List of Commands:", profile.dark()));
        for (org.bukkit.command.Command stub : commands) {
            if (!(stub instanceof PluginCommand plugin)) continue;
            if (!(plugin.getExecutor() instanceof MinecraftCommand)) continue;
            //<editor-fold desc="Message" defaultstate="collapsed">
            final HoverEvent<?> hover = HoverEvent.showText(text("Click to View."));
            final ClickEvent click = ClickEvent.suggestCommand("/help command /" + plugin.getLabel());
            sender.sendMessage(Component.textOfChildren(
                    Component.space(),
                    text("/", profile.pop()),
                    text(stub.getLabel(), profile.highlight()),
                    text(": ", profile.pop()),
                    text(plugin.getDescription(), profile.light())
            ).hoverEvent(hover).clickEvent(click));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

}
