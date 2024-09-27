package com.minigit.util;

import lombok.Data;

import java.util.List;

@Data
public class TreeEntry {
    private String path;
    private String hash;
    private EntryType entryType;
    public TreeEntry() {
    }

    public TreeEntry(String path, String hash, EntryType entryType) {
        this.path = path;
        this.hash = hash;
        this.entryType = entryType;
    }
    public enum EntryType{
        blob, tree
    }
}
