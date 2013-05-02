package com.johan.headtilter;

import com.johan.headtilter.client.headtilter.HeadTilterMode;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
@Theme("headtilter")
public class HeadtilterUI extends UI {

    private VerticalLayout mainLayout;
    private Label hitLabel = new Label();
    private Label recordLabel = new Label();
	private HeadTilter htExt;
	private VerticalLayout loginView;

    @Override
    protected void init(VaadinRequest request) {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        setContent(mainLayout);
        htExt = new HeadTilter();
        htExt.setHeadTilterMode(HeadTilterMode.MOUSE_CURSOR);


         loginView = createLoginView();
        htExt.extend(loginView);
        setView(loginView);
    }

    private void setView(Layout view) {
        mainLayout.removeAllComponents();
        mainLayout.addComponent(view);
        mainLayout.setComponentAlignment(view, Alignment.MIDDLE_CENTER);

    }

    private VerticalLayout createLoginView() {
    	if (loginView != null) return loginView;
        final VerticalLayout layout = new VerticalLayout();
        hitLabel.setWidth(null);
        recordLabel.setWidth(null);
        layout.setWidth("400px");
        layout.setHeight(null);
        layout.setMargin(true);
        layout.setSpacing(true);
        TextField tf = new TextField();
        tf.setWidth("10em");
        PasswordField pwf = new PasswordField();
        pwf.setWidth("10em");
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeUndefined();
        hl.addComponent(new Button("Login", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                VerticalLayout lout = createLoggedInView();
                setView(lout);
            }
        }));

        hl.addComponent(new Button("No pass?"));
        final Button funnyB = new Button("Calibrate!");
        hl.addComponent(funnyB);
        funnyB.setData(new Boolean(false));
        funnyB.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                funnyB.setCaption("Calibrated");
                htExt.updateHeadCalibratedInMiddle();
            }
        });

        Label userNameLabel = new Label("User name");
        userNameLabel.setWidth(null);
        Label pwLabel = new Label("Password");
        pwLabel.setWidth(null);

        layout.addComponent(userNameLabel);
        layout.addComponent(tf);
        layout.setComponentAlignment(userNameLabel, Alignment.BOTTOM_CENTER);
        layout.setComponentAlignment(tf, Alignment.MIDDLE_CENTER);
        layout.addComponent(pwLabel);
        layout.addComponent(pwf);
        layout.setComponentAlignment(pwLabel, Alignment.BOTTOM_CENTER);
        layout.setComponentAlignment(pwf, Alignment.MIDDLE_CENTER);
        layout.addComponent(hl);
        layout.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
        layout.addComponent(hitLabel);
        layout.addComponent(recordLabel);

        return layout;
    }

    private VerticalLayout createLoggedInView() {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        Label l = new Label("Hello, you have now logged in");
        l.setSizeUndefined();

        layout.addComponent(l);
        layout.setComponentAlignment(l, Alignment.MIDDLE_CENTER);
        Button b = new Button("Log out");

        layout.addComponent(b);
        layout.setComponentAlignment(b, Alignment.BOTTOM_CENTER);
        b.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                VerticalLayout lin = createLoginView();
                setView(lin);
            }
        });
        return layout;
    }

}