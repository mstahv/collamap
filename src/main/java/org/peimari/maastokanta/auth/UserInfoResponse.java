/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

/**
 *
 * @author mstahv
 * 
 * "{ "sub": "12345", "picture":
 * "https://lh3.googleusercontent.com/a-/AOh14Gj-e_SJWHy3r7UfKUyG7_-LKfEAPkdDsdJbYJVIJw\u003ds96-c",
 * "email": "matti.tahvonen@gmail.com", "email_verified": true }"
 */
public class UserInfoResponse {
    String sub;
    String picture;
    String email;
    boolean email_verified;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }
    
    
}
