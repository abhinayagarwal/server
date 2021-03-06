package com.stream_pi.server.window.settings.About;

public class Contributor
{
    private String name = null;
    private String email = null;
    private String description = null;
    private String location = null;

    public Contributor(String name, String email, String description, String location)
    {
        this.name = name;
        this.email = email;
        this.description = description;
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}