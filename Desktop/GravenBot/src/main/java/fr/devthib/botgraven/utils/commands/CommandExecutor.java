package fr.devthib.botgraven.utils.commands;

import org.javacord.api.event.message.MessageCreateEvent;

public interface CommandExecutor {

    void run(MessageCreateEvent event,Command command, String[] args);


}
