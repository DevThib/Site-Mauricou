package fr.devthib.botgraven.commands.loupgarou;

import fr.devthib.botgraven.utils.commands.Command;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.*;

public class Game {

    List<User> players;
    ArrayList<Long> ids = new ArrayList<>();
    HashMap<Long,String> Names = new HashMap<>();
    HashMap<Long,Boolean> accept = new HashMap<>();
    HashMap<Long,Role> roles = new HashMap<>();
    Optional<User> sender;
    HashMap<Long,Boolean> eliminated = new HashMap<>();

    int NumberImpostors = 0;
    int NumberInnocents = 0;

    ServerTextChannel channelGeneral;
    ServerTextChannel channelImposteurs;

    boolean startGame = false;

    public void run(MessageCreateEvent event, Command command, String[] args) {

        /* AJOUTS SUPPLEMENTAIRES

        Les imposteurs peuvent choisir de détruire une maison au lieu de tuer quelqu'un.
         Après cela, il y a une chance sur deux que deux personnes soient tuées

         */





        List<User> players = event.getMessage().getMentionedUsers();
        this.players = players;

        sender = event.getMessageAuthor().asUser();

        if(players.size() >= 4 && players.size() <= 9){

            if(!ContainsBot()) {

                if(!ContainsHim()) {

                    event.getMessage().delete();
                    String m = "";

                    for (int i = 0; i < players.size(); i++) {
                        m += players.get(i).getMentionTag() + ",";
                    }

                    //récupération des ids des joueurs et création de l'hashmap accept
                    for (int i = 0; i < players.size(); i++) {
                        ids.add(players.get(i).getId());
                        accept.put(ids.get(i), false);
                    }
                    //création des eliminated
                    for(int i = 0; i < ids.size()+1; i++){
                        if (i == players.size()) {
                            eliminated.put(sender.get().getId(), false);
                        } else {
                            eliminated.put(ids.get(i), false);
                        }
                    }

                    //récupération des noms
                    for(int i = 0; i < players.size()+1; i++){
                        if(i == players.size()){
                            Names.put(sender.get().getId(),sender.get().getName());
                        }else{
                            Names.put(ids.get(i),players.get(i).getName());
                        }
                    }

                    event.getChannel().sendMessage(m + " vous êtes conviés dans une partie de loup garou par **" + event.getMessageAuthor().getName() + "**, l'acceptez vous ?").thenAccept(msg -> {

                        msg.addReactions("✅", "❌");
                        msg.addReactionAddListener(li -> {

                            long ID = li.getUserId();

                            if (li.getEmoji().equalsEmoji("✅") && ids.contains(ID) && !startGame) {

                                accept.replace(ID, true);

                                if (AllAccepted()) {

                                    startGame = true;

                                    //création de la map roles
                                    for (int i = 0; i < players.size() + 1; i++) {
                                        if (i == players.size()) {
                                            roles.put(sender.get().getId(), new Role(1));
                                        } else {
                                            roles.put(ids.get(i), new Role(1));
                                        }
                                    }

                                    DefRoles();

                                    ServerTextChannelBuilder Channels = new ServerTextChannelBuilder(event.getServer().get());
                                    Channels.setName("salon général");
                                    Channels.create().thenAccept(channel1 -> {

                                        Channels.setName("salon des imposteurs");
                                        Channels.create().thenAccept(channel2 -> {


                                            channel1.addMessageCreateListener(channLi->{
                                                if(eliminated.get(channLi.getMessageAuthor().getId())){
                                                    channLi.getMessage().delete();
                                                }
                                            });

                                            channel2.addMessageCreateListener(channLi->{
                                                if(eliminated.get(channLi.getMessageAuthor().getId())){
                                                    channLi.getMessage().delete();
                                                }
                                            });



                                            channelGeneral = channel1;
                                            channelImposteurs = channel2;

                                            channel1.sendMessage("**Disctuez dans ce salon et éliminez une personne que vous pensez être un traître**");
                                            channel2.sendMessage("**Ce salon est disponible uniquement aux imposteurs\nDiscutez entre vous pour déterminer qui éliminer lors des nuits**");

                                            channel1.sendMessage("Rappel des valeurs attrubuées a chaque particpant :");
                                            channel2.sendMessage("Rappel des valeurs attrubuées a chaque particpant :");

                                            //boucle for qui envoie des rappels des emojis dans les deux salons
                                            String mess = "";

                                                for(int a = 0; a < players.size()+1; a++){

                                                       if(a == players.size()){
                                                           mess += "\n -**"+sender.get().getName()+"** : "+getEmoji(a+1);
                                                       }else{
                                                           mess += "\n -**"+players.get(a).getName()+"** : "+getEmoji(a+1);
                                                       }

                                                }
                                                channel1.sendMessage(mess);
                                                channel2.sendMessage(mess);

                                                channel2.sendMessage("Les imposteurs ont la possiblité de voter 🏡. Ceci détruira une maison du village et aura pour effet d'avoir un chance sur deux de tuer 2 villageois");

                                            Timer t = new Timer();
                                            t.schedule(new TimerTask() {
                                                @Override
                                                public void run() {

                                                    //après 1.5sec, lancer la game (phase nuit)
                                                    EliminerInnocent();

                                                }
                                            },1500);






                                        });

                                    });


                                }

                            }
                            if (li.getEmoji().equalsEmoji("❌") && ids.contains(ID) && !startGame) {
                                startGame = true;
                                msg.edit("**Quelqu'un a refusé la demande de loup garou...**");
                                msg.removeAllReactions();
                            }


                        });

                    });

                }else{
                    event.getChannel().sendMessage("❗ Hummm...Jouer avec vous même est un peu de la triche...");
                }

            }else{
                event.getChannel().sendMessage("❗ Un bot ne peux jouer au loup garou");
            }


        }else{
            event.getChannel().sendMessage("❗ Il faut minimum 5 participants et maximum 10 partcipants");
        }


    }

    public boolean ContainsBot(){

        //méthode qui détermine si il y a un bot parmit les joueurs mentionnés

        for(int i = 0; i < players.size(); i++){
            if(players.get(i).isBot()){
                return true;
            }
        }

        return false;
    }

    public boolean ContainsHim(){

        //méthode qui détermine si la personne ne s'est pas ping elle même

        long SenderID = sender.get().getId();

        for(int i = 0; i < ids.size(); i++){
            if(ids.get(i) == SenderID){
                return  true;
            }
        }

        return false;
    }

    public boolean AllAccepted(){

        //méthode qui détermine si tout le monde a accepté

        int nbAccept = 0;

        for(int i = 0; i < accept.size(); i++){
            if(accept.get(ids.get(i))){
                nbAccept++;
            }
            if(nbAccept == accept.size()){
                return true;
            }
        }


        return false;
    }

    public void DefRoles(){

        //méthode qui définit les rôles

        int NumbrImpostors;

        if(players.size()+1 > 5){
            NumbrImpostors = (players.size()+1)/5;
        }else{
            NumbrImpostors = 1;
        }

        int ImpostorsDef = 0;
        int InnocentDef = 0;

        for(int i = 0; i < players.size()+1; i++){

            if(ImpostorsDef == NumbrImpostors) {

                if (i == players.size()) {
                    sender.get().sendMessage("Vous êtes **innocent**");
                    roles.replace(sender.get().getId(), new Role(1));
                    InnocentDef++;
                } else {
                    players.get(i).sendMessage("Vous êtes **innocent**");
                    roles.put(ids.get(i),new Role(1));
                    InnocentDef++;
                }

            }else{

                Random r = new Random();
                int nb = r.nextInt(players.size()+1);

                if(nb == players.size()){
                    sender.get().sendMessage("Vous êtes **Imposteur**");
                    roles.replace(sender.get().getId(), new Role(2));
                    ImpostorsDef++;
                }else{
                    ImpostorsDef++;
                    roles.replace((ids.get(nb)), new Role(2));
                    players.get(nb).sendMessage("Vous êtes **Imposteur**");
                }

            }
            NumberImpostors = ImpostorsDef;
            NumberInnocents = InnocentDef;

        }




    }

    public boolean CheckWin(){

        //méthode qui détermine si quelqu'un a gagné et envoie les messages

        int EliminatedImpo = 0;
        int EliminatedInno = 0;

        for(int i = 0; i < ids.size()+1; i++){
            if(i == ids.size()){

                if(eliminated.get(sender.get().getId())){
                    if(roles.get(sender.get().getId()).isImpostor()){
                        EliminatedImpo++;
                    }else{
                        if(roles.get(sender.get().getId()).isInnocent()){
                            EliminatedInno++;
                        }
                    }
                }

            }else{

                if(eliminated.get(ids.get(i))){
                    if(roles.get(ids.get(i)).isImpostor()){
                        EliminatedImpo++;
                    }else{
                        if(roles.get(ids.get(i)).isInnocent()){
                            EliminatedInno++;
                        }
                    }
                }

            }
        }

        if(EliminatedImpo == NumberImpostors){


            String ImpoNames = "";

            ids.add(sender.get().getId());

            for(int i = 0; i < roles.size(); i++){

                if(roles.get(ids.get(i)).getRoleAsID() == 2){
                    ImpoNames += ",**"+Names.get(ids.get(i))+"**";
                }

            }

            channelImposteurs.sendMessage("**Les innocents ont gagné, les imposteurs étaient"+ImpoNames+"**");
            channelGeneral.sendMessage("**Les innocents ont gagné, les imposteurs étaient"+ImpoNames+"**");

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                   channelImposteurs.delete();
                   channelGeneral.delete();
                }
            },6000);

            return true;
        }
        if(EliminatedInno == NumberInnocents){


            String ImpoNames = "";

            ids.add(sender.get().getId());

            for(int i = 0; i < roles.size(); i++){

                if(roles.get(ids.get(i)).getRoleAsID() == 2){
                    ImpoNames += ",**"+Names.get(ids.get(i))+"**";
                }

            }

            channelImposteurs.sendMessage("**Les innocents ont gagné, les imposteurs étaient"+ImpoNames+"**");
            channelGeneral.sendMessage("**Les innocents ont gagné, les imposteurs étaient"+ImpoNames+"**");

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    channelImposteurs.delete();
                    channelGeneral.delete();
                }
            },6000);

            return true;
        }
    return false;
    }

    public void EliminerInnocent(){

        ArrayList<Long> IDSVoted = new ArrayList<>();
        ArrayList<Long> PersonVoted = new ArrayList<>();

        channelGeneral.sendMessage("**La nuit tombe...🌃**");
        channelImposteurs.sendMessage("**La nuit tombe...🌃**");

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {

                channelImposteurs.sendMessage("**C'est au tour des loups garous d'éliminer un innocent ou de détruire une maison** \n__***Tout le monde doit voter y comprit les innocents !! Cependant leurs votes ne seront pas pris en compte***__ \n*(sert a brouiller les pistes étant donné que les salons ne sont pas privés)*").thenAccept(msg->{

                    for(int i = 1; i < players.size()+1;i++){
                            msg.addReaction(getEmoji(i));
                    }
                    msg.addReaction("🏡");

                    msg.addReactionAddListener(li->{

                        if(!IDSVoted.contains(li.getUserId())){
                            IDSVoted.add(li.getUserId());
                        }

                        if(roles.get(li.getUserId()).isImpostor()) {

                            if (li.getEmoji().equalsEmoji("1️⃣")) {
                                PersonVoted.add(ids.get(0));
                            }
                            if (li.getEmoji().equalsEmoji("2️⃣")) {
                                PersonVoted.add(ids.get(1));
                            }
                            if (li.getEmoji().equalsEmoji("3️⃣")) {
                                PersonVoted.add(ids.get(2));
                            }
                            if (li.getEmoji().equalsEmoji("4️⃣")) {
                                PersonVoted.add(ids.get(3));
                            }
                            if (li.getEmoji().equalsEmoji("5️⃣")) {
                                PersonVoted.add(ids.get(4));
                            }
                            if (li.getEmoji().equalsEmoji("6️⃣")) {
                                PersonVoted.add(ids.get(5));
                            }
                            if (li.getEmoji().equalsEmoji("7️⃣")) {
                                PersonVoted.add(ids.get(6));
                            }
                            if (li.getEmoji().equalsEmoji("8️⃣")) {
                                PersonVoted.add(ids.get(7));
                            }
                            if (li.getEmoji().equalsEmoji("9️⃣")) {
                                PersonVoted.add(ids.get(8));
                            }
                            if (li.getEmoji().equalsEmoji("🔟")) {
                                PersonVoted.add(sender.get().getId());
                            }
                            if(li.getEmoji().equalsEmoji("🏡")){
                                PersonVoted.add(Long.valueOf(123456));
                            }

                        }

                        if(AllVoted(IDSVoted)){

                            if(getPersonEliminated(PersonVoted).equalsIgnoreCase("maison")){

                                Random r = new Random();
                                int nb = r.nextInt(2);

                                if(nb == 0){

                                    channelGeneral.sendMessage("La nuit est passée et les imposteurs ont brulé une maison.**"+ElimineTwoPerson()+"** ont été tué(e)s");
                                    channelImposteurs.sendMessage("La nuit est passée et les imposteurs ont brulé une maison.**"+ElimineTwoPerson()+"** ont été tué(e)s");

                                }else{
                                    channelGeneral.sendMessage("La nuit est passée et les imposteurs ont brulé une maison. Heuresement personne n'a été bléssé ou tué ! Ouf !");
                                    channelImposteurs.sendMessage("La nuit est passée et les imposteurs ont brulé une maison. Heuresement personne n'a été bléssé ou tué ! Ouf !");
                                }

                                if(!CheckWin()){
                                    EliminerImposteur();
                                }

                            }else{
                                channelImposteurs.sendMessage("La nuit est passée et **"+getPersonEliminated(PersonVoted)+"** a été tué(e).");
                                channelGeneral.sendMessage("La nuit est passée et **"+getPersonEliminated(PersonVoted)+"** a été tué(e).");

                                if(!CheckWin()){
                                    EliminerImposteur();
                                }
                            }

                        }


                    });

                });

            }
        },2000);


    }

    public void EliminerImposteur(){

        ArrayList<Long> IDSVoted = new ArrayList<>();
        ArrayList<Long> PersonVoted = new ArrayList<>();

        channelGeneral.sendMessage("**Le jour se lève...☀**");
        channelImposteurs.sendMessage("**Le jour se lève...☀**");

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {

                channelGeneral.sendMessage("**C'est au tour des innocents d'éliminer celui qu'ils pensent être un traître...** \n__***Tout le monde doit voter y comprit les lups garous !! Cependant leurs votes ne sont pas pris en compte***__ \n*(sert a brouiller les pistes étant donné que les salons ne sont pas privés)*").thenAccept(msg->{

                    for(int i = 1; i < players.size()+1;i++){
                        msg.addReaction(getEmoji(i));
                    }

                    msg.addReactionAddListener(li->{

                        if(!IDSVoted.contains(li.getUserId())){
                            IDSVoted.add(li.getUserId());
                        }

                        if(roles.get(li.getUserId()).isInnocent()) {

                            if (li.getEmoji().equalsEmoji("1️⃣")) {
                                PersonVoted.add(ids.get(0));
                            }
                            if (li.getEmoji().equalsEmoji("2️⃣")) {
                                PersonVoted.add(ids.get(1));
                            }
                            if (li.getEmoji().equalsEmoji("3️⃣")) {
                                PersonVoted.add(ids.get(2));
                            }
                            if (li.getEmoji().equalsEmoji("4️⃣")) {
                                PersonVoted.add(ids.get(3));
                            }
                            if (li.getEmoji().equalsEmoji("5️⃣")) {
                                PersonVoted.add(ids.get(4));
                            }
                            if (li.getEmoji().equalsEmoji("6️⃣")) {
                                PersonVoted.add(ids.get(5));
                            }
                            if (li.getEmoji().equalsEmoji("7️⃣")) {
                                PersonVoted.add(ids.get(6));
                            }
                            if (li.getEmoji().equalsEmoji("8️⃣")) {
                                PersonVoted.add(ids.get(7));
                            }
                            if (li.getEmoji().equalsEmoji("9️⃣")) {
                                PersonVoted.add(ids.get(8));
                            }
                            if (li.getEmoji().equalsEmoji("🔟")) {
                                PersonVoted.add(sender.get().getId());
                            }

                        }

                        if(AllVoted(IDSVoted)){
                            channelGeneral.sendMessage("Le jour est passé et **"+getPersonEliminated(PersonVoted)+"** a été tué(e).");
                            channelImposteurs.sendMessage("Le jour est passé et **"+getPersonEliminated(PersonVoted)+"** a été tué(e).");

                            if(!CheckWin()){
                                EliminerInnocent();
                            }
                        }


                    });

                });

            }
        },2000);

    }

    public String getEmoji(int emoji){

        if(emoji == 1){
            return "1️⃣";
        }
        if(emoji == 2){
            return "2️⃣";
        }
        if(emoji == 3){
            return "3️⃣";
        }
        if(emoji == 4){
            return "4️⃣";
        }
        if(emoji == 5){
            return "5️⃣";
        }
        if(emoji == 6){
            return "6️⃣";
        }
        if(emoji == 7){
            return "7️⃣";
        }
        if(emoji == 8){
            return "8️⃣";
        }
        if(emoji == 9){
            return "9️⃣";
        }
        if(emoji == 10){
            return "🔟";
        }


        return null;
    }

    public boolean AllVoted(ArrayList<Long> voted){

        if(voted.size() == players.size()+1){
            return true;
        }
        return false;
    }

    public String getPersonEliminated(ArrayList<Long> votes){

        int Nombre = 0;
        long Person = 0;

        HashMap<Long,Integer> AllVotes = new HashMap<>();
        ArrayList<Long> AllPerson = new ArrayList<>();

        for(int i = 0; i < votes.size(); i++){

            if(AllVotes.containsKey(votes.get(i))){
                AllVotes.put(votes.get(i),1);
                AllPerson.add(votes.get(i));
            }else{
              AllVotes.replace(votes.get(i),AllVotes.get(votes.get(i))+1);
            }

        }

        //boucle qui détermine celui qui a la plus de voies
        for(int i = 0; i < AllVotes.size(); i++){
            if(AllVotes.get(AllPerson.get(i)) > Nombre){
                Nombre = AllVotes.get(AllPerson.get(i));
                Person = AllPerson.get(i);
            }
        }

        if (eliminated.containsKey(Person)) {
            eliminated.replace(Person,true);
            return Names.get(Person);
        }else{
            if(Person == 123456){
                return "maison";
            }else{
                return "personne";
            }

        }

    }

    public String ElimineTwoPerson(){

        Random r = new Random();
        int nb;
        int NbPersonEliminated = 0;

        String PersonElimi = "";

        ids.add(sender.get().getId());

        while(NbPersonEliminated != 2){

            nb = r.nextInt(eliminated.size());

            if(roles.get(ids.get(nb)).isInnocent() && !eliminated.get(ids.get(nb))){
                eliminated.replace(ids.get(nb),true);
                NbPersonEliminated++;
                PersonElimi += Names.get(ids.get(nb))+",";
            }

        }

        return PersonElimi;
    }
}
