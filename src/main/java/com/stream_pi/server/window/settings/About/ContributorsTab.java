package com.stream_pi.server.window.settings.About;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ContributorsTab extends VBox
{

    public ContributorsTab()
    {
        getStyleClass().add("about_license_contributors_vbox");

        TableView<Contributor> tableView = new TableView<>();
        tableView.getStyleClass().add("about_license_contributors_table_view");

        TableColumn<Contributor, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setReorderable(false);
        descriptionColumn.setPrefWidth(250);
        descriptionColumn.setResizable(false);
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Contributor, String> nameColumn = new TableColumn<>("Name (GitHub)");
        nameColumn.setReorderable(false);
        nameColumn.setPrefWidth(220);
        nameColumn.setResizable(false);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Contributor, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setReorderable(false);
        emailColumn.setPrefWidth(200);
        emailColumn.setResizable(false);
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Contributor, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setReorderable(false);
        locationColumn.setPrefWidth(100);
        locationColumn.setResizable(false);
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));


        tableView.getColumns().addAll(descriptionColumn, nameColumn, emailColumn, locationColumn);

        tableView.setPrefWidth(descriptionColumn.getPrefWidth() + nameColumn.getPrefWidth() + emailColumn.getPrefWidth());


        tableView.getItems().addAll(
                new Contributor("Debayan Sutradhar (rnayabed)",
                        "debayansutradhar3@gmail.com",
                        "Founder, Original Author, Maintainer",
                        "India"),
                new Contributor("Samuel Quiñones (SamuelQuinones)",
                        "sdquinones1@gmail.com",
                        "Founder",
                        "United States"),
                new Contributor("Abhinay Agarwal (abhinayagarwal)",
                        "abhinay_agarwal@live.com",
                        "Refactoring, Fixes",
                        "India"),
                new Contributor("Jordan Duabe (j4ckofalltrades)",
                        "jordan.duabe@gmail.com",
                        "Minor Fix (#0dafac9)",
                        "Australia")
        );

        Label disclaimerLabel = new Label("This contributor list shows only those who have contributed " +
                "to the Server Source code.\nTo know about the contributors of Action API, Theme API, Util, " +
                "visit the respective repositories. If you want to know about the Core Team instead, please visit the website.");

        disclaimerLabel.getStyleClass().add("about_license_contributors_disclaimer_label");

        disclaimerLabel.setWrapText(true);

        getChildren().addAll(tableView, disclaimerLabel);
    }
}
