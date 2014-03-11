/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

import com.google.gson.Gson;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import javax.annotation.PostConstruct;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.OAuthPopupOpener;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.label.Header;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

/**
 *
 */
@UIScope
@VaadinComponent
public class LoginView extends MVerticalLayout implements OAuthListener {

    private final Header loginHeader = new Header("Login with Google+");

    private final Button gplusLoginButton = new MButton("Login");

    private String gpluskey;
    private String gplussecret;

    @Autowired
    Environment environment;

    @Override
    public void attach() {
        super.attach();
        gpluskey = environment.getProperty("gpluskey");
        gplussecret = environment.getProperty("gplussecret");

        addComponents(loginHeader, gplusLoginButton);
        OAuthPopupOpener opener = new OAuthPopupOpener(
                Google2Api.class, gpluskey, gplussecret);
        opener.setScope("email");
        opener.extend(gplusLoginButton);
        opener.addOAuthListener(this);
    }

    @Override
    public void authSuccessful(String accessToken, String accessTokenSecret) {

        final Token token = new Token(accessToken, accessTokenSecret);

        Notification.show("authSuccessful: " + accessToken + " " + accessTokenSecret);

        OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/plus/v1/people/me");
        createService().signRequest(token, request);
        Response resp = request.send();

        GooglePlusAnswer answer = new Gson().fromJson(resp.getBody(), GooglePlusAnswer.class);

        AuthenticationUI.get().setUser(answer.emails[0].value, answer.displayName);

    }

    @Override
    public void authDenied(String reason) {
        Notification.show("authDenied:" + reason, Type.ERROR_MESSAGE);
    }

    private OAuthService createService() {
        ServiceBuilder sb = new ServiceBuilder();
        sb.provider(Google2Api.class);
        sb.apiKey(gpluskey);
        sb.apiSecret(gplussecret);
        sb.callback("oob");
        return sb.build();
    }

}
