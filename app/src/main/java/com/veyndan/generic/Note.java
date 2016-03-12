package com.veyndan.generic;

import java.util.List;

public class Note {
    private String name;
    private String date;
    private String visibility;
    private String pins;
    private String profile;
    private List<Description> descriptions;

    @SuppressWarnings("unused")
    public Note() {
        // empty default constructor, necessary for Firebase to be able to deserialize notes
    }

    public Note(String name, String date, String visiblity, String pins, String profile, List<Description> descriptions) {
        this.name = name;
        this.date = date;
        this.visibility = visiblity;
        this.pins = pins;
        this.profile = profile;
        this.descriptions = descriptions;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getPins() {
        return pins;
    }

    public String getProfile() {
        return profile;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    @Override
    public String toString() {
        return "Note{" +
                "name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", visibility='" + visibility + '\'' +
                ", pins='" + pins + '\'' +
                ", profile='" + profile + '\'' +
                ", descriptions=" + descriptions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (name != null ? !name.equals(note.name) : note.name != null) return false;
        if (date != null ? !date.equals(note.date) : note.date != null) return false;
        if (visibility != null ? !visibility.equals(note.visibility) : note.visibility != null)
            return false;
        if (pins != null ? !pins.equals(note.pins) : note.pins != null) return false;
        if (profile != null ? !profile.equals(note.profile) : note.profile != null) return false;
        return descriptions != null ? descriptions.equals(note.descriptions) : note.descriptions == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + (pins != null ? pins.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (descriptions != null ? descriptions.hashCode() : 0);
        return result;
    }

    public static class Description {
        public static final int TYPE_PARAGRAPH = 0;
        public static final int TYPE_IMAGE = 1;

        private String body;
        private int type;

        @SuppressWarnings("unused")
        public Description() {
            // empty default constructor, necessary for Firebase to be able to deserialize descriptions
        }

        public Description(String body, int type) {
            this.body = body;
            this.type = type;
        }

        public String getBody() {
            return body;
        }

        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Description that = (Description) o;

            if (type != that.type) return false;
            return body != null ? body.equals(that.body) : that.body == null;

        }

        @Override
        public int hashCode() {
            int result = body != null ? body.hashCode() : 0;
            result = 31 * result + type;
            return result;
        }
    }
}
