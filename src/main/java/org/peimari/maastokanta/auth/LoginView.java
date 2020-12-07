package org.peimari.maastokanta.auth;

import com.google.gson.Gson;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;
import java.io.IOException;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.Person;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author Matti Tahvonen
 */
@SpringComponent
@UIScope
public class LoginView extends MVerticalLayout implements RequestHandler {

    @Autowired
    Environment environment;
    @Autowired
    AppService appService;
    @Autowired
    Repository repo;
    @Autowired
    GroupsView groupsView;

    Link loginLink;
    private String gpluskey;
    private String gplussecret;

    private OAuthService service;
    private String redirectUrl;

    public LoginView() {
    }

    @Override
    public void attach() {
        super.attach();
        
        if(appService.getPerson() != null) {
            add(groupsView);
            return;
        }

        redirectUrl = Page.getCurrent().getLocation().toString();

        gpluskey = environment.getProperty("gpluskey");
        gplussecret = environment.getProperty("gplussecret");

        service = createService();
        String url = service.getAuthorizationUrl(null);

        loginLink = new Link("Login with Google", new ExternalResource(url));
        loginLink.addStyleName(ValoTheme.LINK_LARGE);

        setCaption("Login");
        add(loginLink);

        VaadinSession.getCurrent().addRequestHandler(this);

    }

    private OAuthService createService() {
        ServiceBuilder sb = new ServiceBuilder();
        sb.provider(Google2Api.class);
        sb.apiKey(gpluskey);
        sb.apiSecret(gplussecret);
        sb.scope("openid email");
        String callBackUrl = Page.getCurrent().getLocation().toString();
        if (callBackUrl.contains("#")) {
            callBackUrl = callBackUrl.substring(0, callBackUrl.indexOf("#"));
        }
        sb.callback(callBackUrl);
        return sb.build();
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        if (request.getParameter("code") != null) {
            String code = request.getParameter("code");
            Verifier v = new Verifier(code);
            Token t = service.getAccessToken(null, v);

            OAuthRequest r = new OAuthRequest(Verb.GET,
                    "https://openidconnect.googleapis.com/v1/userinfo");
            
            service.signRequest(t, r);
            Response resp = r.send();
            
            
            String body = resp.getBody();

            UserInfoResponse answer = new Gson().fromJson(body,
                    UserInfoResponse.class);

            setUser(answer.email);

            VaadinSession.getCurrent().removeRequestHandler(this);

            ((VaadinServletResponse) response).getHttpServletResponse().
                    sendRedirect(redirectUrl);
            return true;
        }

        return false;
    }

    public void setUser(String email) {
        Person person = repo.getPerson(email);
        if (person == null) {
            person = new Person();
            person.setEmail(email);
//            person.setDisplayName(displayName);
            repo.persist(person);
        }
        appService.setPerson(person);
    }

}
