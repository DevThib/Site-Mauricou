package fr.devthib.botgraven.commands;

import fr.devthib.botgraven.utils.commands.Command;
import fr.devthib.botgraven.utils.commands.CommandExecutor;
import org.javacord.api.event.message.MessageCreateEvent;

public class Commandtest implements CommandExecutor {
    @Override
    public void run(MessageCreateEvent event, Command command, String[] args) {

       Long l = Long.valueOf(123456);

       event.getChannel().sendMessage(String.valueOf(l));

    }
}
