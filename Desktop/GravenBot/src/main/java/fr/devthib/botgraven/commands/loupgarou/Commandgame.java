package fr.devthib.botgraven.commands.loupgarou;

import fr.devthib.botgraven.utils.commands.Command;
import fr.devthib.botgraven.utils.commands.CommandExecutor;
import org.javacord.api.event.message.MessageCreateEvent;

public class Commandgame implements CommandExecutor {
    @Override
    public void run(MessageCreateEvent event, Command command, String[] args) {

        Game game = new Game();
        game.run(event, command, args);

    }
}
