package com.stream_pi.server.window.settings;

import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.client.ClientTheme;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.SpaceFiller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientsSettings extends VBox
{
    private VBox clientsSettingsVBox;
    private Button saveButton;

    private ServerListener serverListener;

    private Logger logger;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public ClientsSettings(ExceptionAndAlertHandler exceptionAndAlertHandler, ServerListener serverListener)
    {   
        getStyleClass().add("clients_settings");
        this.serverListener = serverListener;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        clientSettingsVBoxArrayList = new ArrayList<>();

        logger = Logger.getLogger(ClientsSettings.class.getName());

        clientsSettingsVBox = new VBox();
        clientsSettingsVBox.getStyleClass().add("clients_settings_vbox");
        clientsSettingsVBox.setAlignment(Pos.TOP_CENTER);

        setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("clients_settings_scroll_pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));

        clientsSettingsVBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));
        scrollPane.setContent(clientsSettingsVBox);

        saveButton = new Button("Save");
        saveButton.setOnAction(event -> onSaveButtonClicked());

        HBox hBox = new HBox(saveButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(scrollPane, hBox);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void onSaveButtonClicked()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    Platform.runLater(()->saveButton.setDisable(true));
                    StringBuilder finalErrors = new StringBuilder();

                    for(ClientSettingsVBox clientSettingsVBox : clientSettingsVBoxArrayList)
                    {
                        StringBuilder errors = new StringBuilder();

                        if(clientSettingsVBox.getNickname().isBlank())
                            errors.append("    Cannot have blank nickname. \n");


                        for(ClientProfileVBox clientProfileVBox : clientSettingsVBox.getClientProfileVBoxes())
                        {
                            StringBuilder errors2 = new StringBuilder();

                            if(clientProfileVBox.getName().isBlank())
                                errors2.append("        cannot have blank nickname. \n");

                            try {
                                Integer.parseInt(clientProfileVBox.getActionSize());
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer action Size. \n");
                            }


                            try {
                                Integer.parseInt(clientProfileVBox.getActionGap());
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer action Gap. \n");
                            }

                            if(!errors2.toString().isEmpty())
                            {
                                errors.append("    ")
                                        .append(clientProfileVBox.getRealName())
                                        .append("\n")
                                        .append(errors2)
                                        .append("\n");
                            }
                        }


                        if(!errors.toString().isEmpty())
                        {
                            finalErrors.append("* ")
                                    .append(clientSettingsVBox.getRealNickName())
                                    .append("\n")
                                    .append(errors)
                                    .append("\n");
                        }



                    }

                    if(!finalErrors.toString().isEmpty())
                        throw new MinorException("You made form mistakes",
                                "Please fix the following issues : \n"+finalErrors);



                    //save details and values
                    for(ClientSettingsVBox clientSettingsVBox : clientSettingsVBoxArrayList)
                    {
                        clientSettingsVBox.saveClientAndProfileDetails();
                    }

                    loadData();
                    serverListener.clearTemp();
                }
                catch (MinorException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleMinorException(e);
                }
                catch (SevereException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleSevereException(e);
                }
                catch (CloneNotSupportedException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleSevereException(new SevereException(
                            e.getMessage()
                    ));
                }
                finally
                {
                    Platform.runLater(()->saveButton.setDisable(false));
                }
                return null;
            }
        }).start();
    }

    private ArrayList<ClientSettingsVBox> clientSettingsVBoxArrayList;

    public void loadData()
    {
        logger.info("Loading client data into ClientsSettings ...");

        Platform.runLater(()-> clientsSettingsVBox.getChildren().clear());
        clientSettingsVBoxArrayList.clear();

        List<ClientConnection> clientConnections = ClientConnections.getInstance().getConnections();

        if(clientConnections.size() == 0)
        {
            Platform.runLater(()->{
                clientsSettingsVBox.getChildren().add(new Label("No Clients Connected."));
                saveButton.setVisible(false);
            });
        }
        else
        {
            Platform.runLater(()->saveButton.setVisible(true));
            for (ClientConnection clientConnection : clientConnections) {
                ClientSettingsVBox clientSettingsVBox = new ClientSettingsVBox(clientConnection);

                clientSettingsVBoxArrayList.add(clientSettingsVBox);
                Platform.runLater(()->clientsSettingsVBox.getChildren().add(clientSettingsVBox));
            }
        }

        logger.info("... Done!");
    }

    public class ClientSettingsVBox extends VBox
    {
        private ComboBox<ClientProfile> profilesComboBox;

        private ComboBox<ClientTheme> themesComboBox;

        private Client client;

        public void setClient(Client client)
        {
            this.client = client;
        }

        public double getDisplayHeight() {
            return client.getDisplayHeight();
        }

        public double getDisplayWidth() {
            return client.getDisplayWidth();
        }

        private TextField nicknameTextField;

        public String getNickname() {
            return nicknameTextField.getText();
        }

        private Label nickNameLabel;

        private Label versionLabel;

        public String getRealNickName()
        {
            return nickNameLabel.getText();
        }

        private com.stream_pi.util.platform.Platform platform;

        public com.stream_pi.util.platform.Platform getPlatform() {
            return platform;
        }

        private Label socketConnectionLabel;

        private ClientConnection connection;

        private Accordion profilesAccordion;

        private ArrayList<ClientProfileVBox> clientProfileVBoxes;

        private Label platformLabel;

        public ArrayList<ClientProfileVBox> getClientProfileVBoxes() {
            return clientProfileVBoxes;
        }

        public ClientSettingsVBox(ClientConnection connection)
        {
            this.connection = connection;
            this.platform = connection.getClient().getPlatform();

            clientProfileVBoxes = new ArrayList<>();

            getStyleClass().add("clients_settings_each_client_box");

            initUI();
            loadValues();
        }

        public ClientConnection getConnection()
        {
            return connection;
        }

        public void saveClientAndProfileDetails() throws SevereException, CloneNotSupportedException, MinorException {

            getConnection().saveClientDetails(
                    nicknameTextField.getText(),
                    profilesComboBox.getSelectionModel().getSelectedItem().getID(),
                    themesComboBox.getSelectionModel().getSelectedItem().getThemeFullName()
            );

            for(ClientProfileVBox clientProfileVBox : clientProfileVBoxes)
            {
                getConnection().saveProfileDetails(clientProfileVBox.getClientProfile());
            }


            //remove deleted client profiles
            for(ClientProfile clientProfile : connection.getClient().getAllClientProfiles())
            {
                boolean found = false;
                for(ClientProfileVBox clientProfileVBox : clientProfileVBoxes)
                {
                    if(clientProfileVBox.getClientProfile().getID().equals(clientProfile.getID()))
                    {
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    connection.getClient().removeProfileFromID(clientProfile.getID());
                    connection.deleteProfile(clientProfile.getID());
                }
            }


        }

        public void initUI()
        {
            profilesComboBox = new ComboBox<>();
            Callback<ListView<ClientProfile>, ListCell<ClientProfile>> profilesComboBoxFactory = new Callback<>() {
                @Override
                public ListCell<ClientProfile> call(ListView<ClientProfile> clientConnectionListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ClientProfile clientProfile, boolean b) {
                            super.updateItem(clientProfile, b);

                            if(clientProfile == null)
                            {
                                setText(null);
                            }
                            else
                            {
                                setText(clientProfile.getName());
                            }
                        }
                    };
                }
            };
            profilesComboBox.setCellFactory(profilesComboBoxFactory);
            profilesComboBox.setButtonCell(profilesComboBoxFactory.call(null));

            themesComboBox = new ComboBox<>();
            Callback<ListView<ClientTheme>, ListCell<ClientTheme>> themesComboBoxFactory = new Callback<>() {
                @Override
                public ListCell<ClientTheme> call(ListView<ClientTheme> clientConnectionListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ClientTheme clientTheme, boolean b) {
                            super.updateItem(clientTheme, b);

                            if(clientTheme == null)
                            {
                                setText(null);
                            }
                            else
                            {
                                setText(clientTheme.getShortName());
                            }
                        }
                    };
                }
            };
            themesComboBox.setCellFactory(themesComboBoxFactory);
            themesComboBox.setButtonCell(themesComboBoxFactory.call(null));

            platformLabel = new Label();
            platformLabel.getStyleClass().add("client_settings_each_client_platform_label");

            socketConnectionLabel = new Label();
            socketConnectionLabel.getStyleClass().add("client_settings_each_client_socket_connection_label");

            nicknameTextField = new TextField();

            nickNameLabel = new Label();
            nickNameLabel.getStyleClass().add("client_settings_each_client_nick_name_label");

            versionLabel = new Label();
            versionLabel.getStyleClass().add("client_settings_each_client_version_label");

            profilesAccordion = new Accordion();
            profilesAccordion.getStyleClass().add("client_settings_each_client_profiles_accordion");
            VBox.setMargin(profilesAccordion, new Insets(0,0,20,0));


            Button addNewProfileButton = new Button("Add new Profile");
            addNewProfileButton.setOnAction(event -> onNewProfileButtonClicked());

            setSpacing(10.0);

            getStyleClass().add("settings_clients_each_client");



            this.getChildren().addAll(
                    nickNameLabel,
                    socketConnectionLabel,
                    platformLabel,
                    versionLabel,
                    new HBoxInputBox("Nickname",nicknameTextField),
                    new HBox(
                        new Label("Theme"),
                        SpaceFiller.horizontal(),
                        themesComboBox
                    ),

                    new HBox(new Label("Startup Profile"),
                            SpaceFiller.horizontal(),
                            profilesComboBox),

                    addNewProfileButton,

                    profilesAccordion);
        }

        public void loadValues()
        {
            Client client = connection.getClient();

            profilesComboBox.setItems(FXCollections.observableList(client.getAllClientProfiles()));
            profilesComboBox.getSelectionModel().select(
                    client.getProfileByID(client.getDefaultProfileID())
            );


            themesComboBox.setItems(FXCollections.observableList(client.getThemes()));
            themesComboBox.getSelectionModel().select(
                    client.getThemeByFullName(
                            client.getDefaultThemeFullName()
                    )
            );

            nicknameTextField.setText(client.getNickName());

            platformLabel.setText("Platform : "+client.getPlatform().getUIName());

            setClient(client);

            socketConnectionLabel.setText(client.getRemoteSocketAddress().toString().substring(1)); //substring removes the `/`

            nickNameLabel.setText(client.getNickName());

            versionLabel.setText(client.getReleaseStatus().getUIName()+" "+client.getVersion().getText());

            //add profiles
            for(ClientProfile clientProfile : client.getAllClientProfiles())
            {
                TitledPane titledPane = new TitledPane();
                titledPane.getStyleClass().add("client_settings_each_client_accordion_each_titled_pane");
                titledPane.setText(clientProfile.getName());

                ClientProfileVBox clientProfileVBox = new ClientProfileVBox(clientProfile);

                clientProfileVBox.getRemoveButton().setOnAction(event -> onProfileDeleteButtonClicked(clientProfileVBox, titledPane));

                titledPane.setContent(clientProfileVBox);

                clientProfileVBoxes.add(clientProfileVBox);

                profilesAccordion.getPanes().add(titledPane);
            }
        }

        public void onNewProfileButtonClicked()
        {
            ClientProfile clientProfile = new ClientProfile(
                    "Untitled Profile",
                    3,
                    3,
                    100,
                    5
            );


            ClientProfileVBox clientProfileVBox = new ClientProfileVBox(clientProfile);
            TitledPane titledPane = new TitledPane();
            titledPane.setContent(clientProfileVBox);
            titledPane.setText(clientProfile.getName());

            clientProfileVBox.getRemoveButton().setOnAction(event -> onProfileDeleteButtonClicked(clientProfileVBox, titledPane));

            clientProfileVBoxes.add(clientProfileVBox);

            profilesAccordion.getPanes().add(titledPane);
        }

        public void onProfileDeleteButtonClicked(ClientProfileVBox clientProfileVBox, TitledPane titledPane)
        {
            if(clientProfileVBoxes.size() == 1)
            {
                exceptionAndAlertHandler.handleMinorException(new MinorException("Only one",
                        "You cannot delete all profiles"));
            }
            else
            {
                if(profilesComboBox.getSelectionModel().getSelectedItem().getID().equals(clientProfileVBox.getClientProfile().getID()))
                {
                    exceptionAndAlertHandler.handleMinorException(new MinorException("Default",
                            "You cannot delete default profile. Change to another one to delete this."));
                }
                else
                {
                    clientProfileVBoxes.remove(clientProfileVBox);
                    profilesComboBox.getItems().remove(clientProfileVBox.getClientProfile());

                    profilesAccordion.getPanes().remove(titledPane);
                }
            }
        }
    }

    public class ClientProfileVBox extends VBox
    {
        private TextField nameTextField;

        public String getName()
        {
            return nameTextField.getText();
        }

        private TextField rowsTextField;

        public String getRows()
        {
            return rowsTextField.getText();
        }

        private TextField colsTextField;

        public String getCols()
        {
            return colsTextField.getText();
        }

        private TextField actionSizeTextField;

        public String getActionSize()
        {
            return actionSizeTextField.getText();
        }

        private TextField actionGapTextField;

        public String getActionGap()
        {
            return actionGapTextField.getText();
        }

        private Button removeButton;

        private ClientProfile clientProfile;

        public String getRealName()
        {
            return clientProfile.getName();
        }

        public ClientProfileVBox(ClientProfile clientProfile)
        {
            this.clientProfile = clientProfile;

            getStyleClass().add("client_settings_each_client_accordion_each_profile_box");
            initUI();
            loadValues(clientProfile);
        }

        public void initUI()
        {
            setPadding(new Insets(5.0));
            setSpacing(10.0);

            nameTextField = new TextField();
            rowsTextField = new TextField();
            colsTextField = new TextField();
            actionSizeTextField = new TextField();
            actionGapTextField = new TextField();

            removeButton = new Button("Remove");

            HBox hBox = new HBox(removeButton);
            hBox.setAlignment(Pos.CENTER_RIGHT);


            getChildren().addAll(
                    new HBoxInputBox("Name ", nameTextField),
                    new HBoxInputBox("Rows", rowsTextField),
                    new HBoxInputBox("Columns", colsTextField),
                    new HBoxInputBox("Action Size", actionSizeTextField),
                    new HBoxInputBox("Action Gap", actionGapTextField),
                    hBox
            );
        }

        public Button getRemoveButton()
        {
            return removeButton;
        }

        public void loadValues(ClientProfile clientProfile)
        {
            nameTextField.setText(clientProfile.getName());

            rowsTextField.setText(clientProfile.getRows()+"");
            colsTextField.setText(clientProfile.getCols()+"");

            actionSizeTextField.setText(clientProfile.getActionSize()+"");
            actionGapTextField.setText(clientProfile.getActionGap()+"");
        }

        public ClientProfile getClientProfile()
        {
            clientProfile.setActionGap(Integer.parseInt(actionGapTextField.getText()));
            clientProfile.setActionSize(Integer.parseInt(actionSizeTextField.getText()));
            clientProfile.setRows(Integer.parseInt(rowsTextField.getText()));
            clientProfile.setCols(Integer.parseInt(colsTextField.getText()));
            clientProfile.setName(nameTextField.getText());

            return clientProfile;
        }
    }


}
