package net.noratek.smartvoxx.common.model;

/**
 * Created by eloudsa on 28/08/15.
 */
public class Speaker {

    private String uuid;
    private String fullName;
    private String lastName;
    private String firstName;
    private String blog;
    private String twitter;
    private String company;
    private String bio;
    private String avatarURL;
    private String avatarImage;

    private Link link;


    public Speaker() {
    }

    public Speaker(String uuid, String lastName, String firstName) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public Speaker(String uuid, String lastName, String firstName, String blog, String twitter, String company, String bio, String avatarURL, String avatarImage) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.firstName = firstName;
        this.blog = blog;
        this.twitter = twitter;
        this.company = company;
        this.bio = bio;
        this.avatarURL = avatarURL;
        this.avatarImage = avatarImage;
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }


    public String getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(String avatarImage) {
        this.avatarImage = avatarImage;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

}
