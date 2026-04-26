package com.sigterm.module;

public enum Category {
    FAVORITES("Favorites", 0xFFFFDD00),
    COMBAT("Combat", 0xFFFF4444),
    MOVEMENT("Movement", 0xFF44FF44),
    RENDER("Render", 0xFF4488FF),
    PLAYER("Player", 0xFFFFAA00),
    MISC("Misc", 0xFFAAAAAA);

    public final String displayName;
    public final int color;
    Category(String n, int c) { displayName = n; color = c; }
}
