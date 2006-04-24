package net.jsunit.model;

public enum BrowserType {

    INTERNET_EXPLORER("images/logo_ie.gif", "Internet Explorer", "iexplore"),
    FIREFOX("images/logo_firefox.gif", "Firefox", "firefox"),
    NETSCAPE("images/logo_netscape.gif", "Netscape", "netscape"),
    MOZILLA("images/logo_mozilla.gif", "Mozilla", "mozilla"),
    OPERA("images/logo_opera.gif", "Opera", "opera"),
    SAFARI("images/logo_safari.gif", "Safari", "safari");

    private String logoPath;
    private String displayName;
    private String fileNameSubstring;

    BrowserType(String logoPath, String displayName, String fileNameSubstring) {
        this.logoPath = logoPath;
        this.displayName = displayName;
        this.fileNameSubstring = fileNameSubstring;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BrowserType resolve(String fileName) {
        for (BrowserType type : values()) {
            if (fileName.toLowerCase().indexOf(type.fileNameSubstring.toLowerCase()) != -1)
                return type;
        }
        return null;
    }

}