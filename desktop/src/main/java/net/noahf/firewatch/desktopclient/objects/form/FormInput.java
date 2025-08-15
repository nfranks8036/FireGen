package net.noahf.firewatch.desktopclient.objects.form;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import net.noahf.firewatch.desktopclient.utils.SupplierUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class FormInput<T extends Node> {

    public static FormInput<Button> button() {
        return new FormInput<>() {
            @Override
            public Button findOriginalNode() {
                Button button = new Button(this.name);
                button.setMnemonicParsing(false);
                GridPane.setHalignment(button, HPos.CENTER);
                GridPane.setValignment(button, VPos.CENTER);
                return button;
            }
        };
    }

    public static <R> FormInput<ChoiceBox<R>> choices() {
        return new FormInput<>() {
            @Override
            public ChoiceBox<R> findOriginalNode() {
                ChoiceBox<R> choices = new ChoiceBox<>();
//                choices.setPrefWidth(176.0);
//                choices.setPrefHeight(26.0);
                GridPane.setValignment(choices, VPos.CENTER);
                GridPane.setHalignment(choices, HPos.CENTER);
                return choices;
            }
        };
    }

    public static <R> FormInput<ComboBox<R>> combo(boolean searchable) {
        return new FormInput<>() {
            @SuppressWarnings("unchecked")
            @Override
            public ComboBox<R> findOriginalNode() {
                ComboBox<R> combo = new ComboBox<>();
//                combo.setPrefWidth(176.0);
//                combo.setPrefHeight(26.0);
                GridPane.setValignment(combo, VPos.CENTER);
                GridPane.setHalignment(combo, HPos.CENTER);

                if (searchable) {
                    combo.setEditable(true);
                    combo.getEditor().textProperty().addListener((obs, old, query) -> {
                        ObservableList<R> originalList = (ObservableList<R>) combo.getProperties().getOrDefault("original", null);
                        if (originalList == null) {
                            combo.getProperties().put("original", combo.getItems());
                            originalList = combo.getItems();
                        }

                        if (!combo.isShowing()) combo.show();

                        ObservableList<R> filtered = FXCollections.observableArrayList();
                        for (R search : originalList) {
                            if (search.toString().toLowerCase().contains(query.toLowerCase())) {
                                filtered.add(search);
                            }
                        }

                        combo.setItems(filtered);
                    });
                    combo.setOnAction((e) -> {
                        if (!combo.getProperties().containsKey("original")) {
                            return;
                        }

                        Platform.runLater(() ->{
                            R selected = combo.getSelectionModel().getSelectedItem();
                            try {
                                combo.setItems((ObservableList<R>) combo.getProperties().getOrDefault("original", combo.getItems()));
                            } catch (Exception ex) {
                                ex.printStackTrace(System.err);
                                // ignored, if it can't do it- then oh well, user needs to refresh
                            }
                            combo.getSelectionModel().select(selected);
                        });
                    });
                }
                return combo;
            }
        };
    }

    public static FormInput<TextField> text(@Nullable Supplier<String> prefilled) {
        return new FormInput<>() {
            @Override
            public TextField findOriginalNode() {
                TextField text = new TextField(SupplierUtils.tryGet(prefilled));
                text.setPromptText(this.name);
                text.setPrefWidth(226.0);
                text.setFont(new Font(14.0));
                GridPane.setMargin(text, new Insets(0.0, 5.0, 0.0, 5.0));
                return text;
            }
        };
    }

    public static <O extends Node> FormInput<O> custom(Supplier<O> supplier) {
        return new FormInput<>() {
            @Override
            public O findOriginalNode() {
                return supplier.get();
            }
        };
    }




    protected String name = null;
    protected T node = null;

    public String name() { return this.name; }
    public abstract T findOriginalNode();
    public T node() {
        this.verifyFields();
        return this.node;
    }

    public FormInput<T> title(String title) {
        this.name = title;
        return this;
    }

    public FormInput<T> update(Function<T, T> function) {
        this.node = function.apply(this.node());
        return this;
    }

    public FormInput<T> update(Consumer<T> consumer) {
        return this.update((field) -> {
            consumer.accept(field);
            return field;
        });
    }

    public FormInput<T> add(int rowId, GridPane pane) {
        this.add((label, obj) -> {
            pane.add(label, 0, rowId);
            pane.add(node, 1, rowId);
        });
        return this;
    }

    public FormInput<T> add(BiConsumer<Label, T> consumers) {
        T node = this.node();

        Label label = new Label(this.name());
        label.setFont(new Font(19.0));
        label.setContentDisplay(ContentDisplay.CENTER);
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setValignment(label, VPos.CENTER);

        consumers.accept(label, node);

        return this;
    }

    private void verifyFields() {
        if (this.name == null) {
            throw new IllegalStateException("Title of form is not set.");
        }
        if (this.node == null) {
            this.node = this.findOriginalNode();
        }
    }
}
