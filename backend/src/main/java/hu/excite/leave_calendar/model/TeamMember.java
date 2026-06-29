package hu.excite.leave_calendar.model;

public enum TeamMember {
    ALICE("Alice"),
    BOB("Bob"),
    CHARLIE("Charlie"),
    DIANA("Diana");

    private final String displayName;

    TeamMember(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
