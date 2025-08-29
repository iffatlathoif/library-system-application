package com.enigma.library_app.enumeration;

public enum Role {
    ADMIN, MEMBER,STAFF;

    public String getAuthority(){
        return name();
    }
}
