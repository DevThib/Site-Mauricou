package fr.devthib.botgraven.commands.loupgarou;

public class Role {

    int ID;

    String roleAsString;

    public Role(int RoleID){
        this.ID = RoleID;

        if(ID == 1){
            roleAsString = "innocent";
        }else{
            if (ID == 2) {
                roleAsString = "imposteur";
            }else{
                roleAsString = " ERROR no role";
            }
        }

    }

    public int getRoleAsID(){
        return this.ID;
    }

    public String getRoleAsString(){
        return this.roleAsString;
    }

    public boolean isImpostor(){

        if(ID == 2){
            return true;
        }
        return false;
    }

    public boolean isInnocent(){

        if(ID == 2){
            return true;
        }
        return false;
    }

}
