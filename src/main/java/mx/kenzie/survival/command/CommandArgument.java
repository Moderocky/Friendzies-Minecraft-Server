package mx.kenzie.survival.command;

import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.centurion.TypedArgument;
import mx.kenzie.clockwork.collection.ClockList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;

public class CommandArgument extends TypedArgument<MinecraftCommand> {

    private int lastHash;
    private MinecraftCommand lastValue;

    public CommandArgument() {
        super(MinecraftCommand.class);
        this.label = "command";
    }

    @Override
    public boolean matches(String s) {
        if (s.charAt(0) != '/') return false;
        this.lastHash = s.hashCode();
        this.lastValue = this.getCommand(s.substring(1));
        return (lastValue != null);
    }

    protected MinecraftCommand getCommand(String string) {
        final Command stub = Bukkit.getCommandMap().getCommand(string);
        if (!(stub instanceof PluginCommand plugin)) return null;
        if (!(plugin.getExecutor() instanceof MinecraftCommand command)) return null;
        return command;
    }

    @Override
    public MinecraftCommand parse(String s) {
        if (s.hashCode() == lastHash && lastValue != null) return lastValue;
        if (this.matches(s)) return lastValue;
        return null;
    }

    @Override
    public String[] possibilities() {
        final ClockList<String> list = new ClockList<>();
        for (Command stub : Bukkit.getCommandMap().getKnownCommands().values()) {
            if (!(stub instanceof PluginCommand plugin)) continue;
            if (!(plugin.getExecutor() instanceof MinecraftCommand)) continue;
            list.add('/' + plugin.getLabel());
        }
        return list.toArray();
    }
}
