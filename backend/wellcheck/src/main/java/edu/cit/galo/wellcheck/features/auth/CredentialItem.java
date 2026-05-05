package edu.cit.galo.wellcheck.features.auth;

public class CredentialItem {
    private String title;
    private String year;

    public CredentialItem() {}

    public CredentialItem(String title, String year) {
        this.title = title;
        this.year = year;
    }

    /** Parse from stored format "Title|Year" */
    public static CredentialItem fromEntry(String entry) {
        if (entry == null || entry.isBlank()) return new CredentialItem("", "");
        String[] parts = entry.split("\\|", 2);
        return new CredentialItem(parts[0], parts.length > 1 ? parts[1] : "");
    }

    /** Serialize to stored format "Title|Year" */
    public String toEntry() {
        return title + "|" + (year != null ? year : "");
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}