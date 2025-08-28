package com.enigma.library_app.auth.constant;

public enum Role {
    ADMIN, MEMBER,STAFF;

    public String getAuthority(){
        return name();
    }
}
