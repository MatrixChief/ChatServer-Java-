import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Types of messages
    static final int MESSAGE = 0, LOGOUT = 1, DM = 2, LIST = 3, TICTACTOE = 4;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private int type;
    private String msg;
    private String recipient;

    /// constructor utilised for DM and TTT
    public ChatMessage(int type, String msg, String recipient){
        this.type=type;
        this.msg=msg;
        this.recipient=recipient;
    }

    // constructor utilised for broadcast
    public ChatMessage(int type, String msg){
        this.type=type;
        this.msg=msg;
    }

    // constructor utilised for logout or list
    public ChatMessage(int type){
        this.type=type;
    }

    //getters

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}