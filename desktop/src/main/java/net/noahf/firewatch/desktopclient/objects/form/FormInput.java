package net.noahf.firewatch.desktopclient.objects.form;

import javafx.scene.Node;

public abstract class FormInput<I> {

    protected String name;
    protected I input;

    public void name(String newName) { this.name = newName; }

    public abstract Node get();

}
