package fr.devthib.botgraven.utils.commands;

import fr.devthib.botgraven.Main;
import fr.devthib.botgraven.commands.loupgarou.Commandgame;
import fr.devthib.botgraven.commands.Commandtest;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class MessageManager {

    private static CommandRegistry registry = new CommandRegistry();

    static{

        registry.addCommand(new Command(
                "test",
                "commande dans lequel je fais mes tests",
                new Commandtest(),
                "test"
        ));

        registry.addCommand(new Command(
                "lg",
                "lance une partie de loup garou",
                new Commandgame(),
                "lg","launchgame"
        ));


    }

    private static final String PREFIX = Main.getConfigManager().getToml().getString("bot.prefix");

    public static void create(MessageCreateEvent event) {

        if (event.getMessageContent().startsWith(PREFIX)) {

            String[] args = event.getMessageContent().split(" ");
            String CommandName = args[0].substring(PREFIX.length());
            args = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 0, args.length);

            String[] finalArgs = args;

            String ar = "";

            for (int i = 1; i < finalArgs.length; i++) {
                ar += " " + finalArgs[i];
            }

            try {
                System.out.println(event.getMessageAuthor().getName() + "-----" + event.getServer().get().getName() + "-----> $" + CommandName + ar);
            } catch (NoSuchElementException e) {
            }

            registry.getByAlias(CommandName).ifPresent((cmd) -> {

                cmd.getExecutor().run(event, cmd, finalArgs);

            });


        }


    }


}
