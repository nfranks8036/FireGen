package net.noahf.firewatch.desktopclient.objects.form;

import javafx.scene.Node;

public abstract class FormInput<I> {

    public static FormButton button() { return new FormButton(); }

    protected String name;
    protected I input;

    public void name(String newName) { this.name = newName; }

    public abstract Node get();

}
