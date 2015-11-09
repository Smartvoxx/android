package net.noratek.smartvoxx.common.model;

/**
 * Created by eloudsa on 06/09/15.
 */
public class Link {

    private String href;
    private String rel;
    private String title;


    public Link(String href, String rel, String title) {
        this.href = href;
        this.rel = rel;
        this.title = title;
    }

    // Getters and Setters
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
