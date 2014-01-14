package de.kisi.android.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

// This Cookie Store prevents that cookies get stored, because otherwise if the http-library uses
// cookies and the auth_token, the cookie overwrites the auth_token. If this happens switching user 
// accounts don't work
// see:  https://github.com/loopj/android-async-http/issues/177

public class BlackholeCookieStore implements CookieStore {
    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>();
    }

    @Override
    public boolean clearExpired(Date date) {
        return true;
    }

    @Override
    public void clear() {
    }
}; 