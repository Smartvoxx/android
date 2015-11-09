package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 30/08/15.
 */
public class ConfirmationEvent {

    private String path;
    private String message;

    public ConfirmationEvent(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
