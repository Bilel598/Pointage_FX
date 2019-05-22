package org.exemple.test.javatest;



import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.exemple.test.mqtt.SimpleMqttCallBack;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class App extends Application implements Runnable  {
	//Attributs
	private Button connectButton;
	private TextField urlTextField;
	private TextField topicTextField;
	private TextField brokerTextField1,brokerTextField2,brokerTextField3,brokerTextField4;
	private TextArea logTextArea;
	private boolean isSignedIn=false;
	private String url,topic,heure_arrivee,date_arrivee,heurelue,heure_depart,datelue,UIDlue,date_depart;
	private String[] UID= new String[] {"335A791B0B", "BB0A5D0DE1"};
	private int nb_carte=UID.length;
	private MqttClient client;
	private boolean[] active = new boolean[nb_carte];
	private String[] pointage = new String[nb_carte*2];
	private List<String> cartes= new ArrayList<String>();

	@Override
	public void start(Stage stage) {
		BorderPane root= new BorderPane();
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(10));
		gp.setHgap(5);
		gp.setVgap(5);
		connectButton= new Button("Connexion");
		gp.add( connectButton, 1, 3);
		gp.add(new Label ("URL"), 0, 0);
		gp.add(new Label ("Topic"), 0, 1);
		urlTextField = new TextField("tcp://192.168.1.52:1883");
		topicTextField = new TextField("iot_data");
		gp.add(urlTextField, 1, 0);
		gp.add(topicTextField, 1, 1);
		root.setLeft(gp);
		GridPane gpcenter = new GridPane();
		gpcenter.setPadding(new Insets(10));
		gpcenter.setHgap(5);
		gpcenter.setVgap(5);
		brokerTextField1 = new TextField("Premier pointage");
		brokerTextField1.setEditable(false);
		brokerTextField2 = new TextField("Pointage 2");
		brokerTextField2.setEditable(false);
		brokerTextField3 = new TextField("Premier pointage, autre carte");
		brokerTextField3.setEditable(false);
		brokerTextField4 = new TextField("Pointage 2");
		brokerTextField4.setEditable(false);
		gpcenter.add(brokerTextField1, 0, 5);
		gpcenter.add(brokerTextField2, 0,6);
		gpcenter.add(brokerTextField3, 0, 8);
		gpcenter.add(brokerTextField4, 0,9);
		brokerTextField1.setPrefSize(500,24);
		root.setCenter(gpcenter);
		logTextArea = new TextArea("Derniers enregistrements: \n");
		logTextArea.setEditable(false);
		root.setBottom(logTextArea);
		this.adjustControls();
		connectButton.setOnAction((event) ->{
			this.adjustControls();
			if (isSignedIn == false)
			{
				signIn();
			}
			else 
			{
				signOut();
			}
		});
		stage.setOnCloseRequest((WindowEvent event1) ->{
			System.out.println("Close window event");
			if(this.isSignedIn) this.signOut();
			event1.consume();
			System.exit(0);			
		});
		stage.setTitle("Pointage");
		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();
	}
	private void signIn(){
		System.out.println("Vous êtes dans la méthode signIn.");
		isSignedIn=true;
		url=urlTextField.getText();
		topic=topicTextField.getText();

		System.out.println("== START SUBSCRIBER ==");

		try {
			client=new MqttClient(url, MqttClient.generateClientId());
			client.setCallback( new SimpleMqttCallBack() );
			client.connect();
			client.subscribe(topic);
		}
		catch (Exception e)
		{
			System.out.println("Exception dans la méthode signIn()");	
		}
		adjustControls();
		Thread t = new Thread(this);
		t.start();
	}
	public void signOut() {
		System.out.println("Vous êtes dans la méthode signOut.");
		isSignedIn=false;
		try {
			System.out.println("== STOP SUBSCRIBER ==");
			client.disconnect();
		}
		catch (Exception e)
		{
			System.out.println("Exception dans la méthode signOut()");	
		}
		adjustControls();
	}
	public void adjustControls() {
		if(this.isSignedIn)
		{
			urlTextField.setDisable(true);
			topicTextField.setDisable(true);
			connectButton.setText("Deconnexion");
			brokerTextField1.setDisable(false);
			brokerTextField2.setDisable(false);
			brokerTextField3.setDisable(false);
			brokerTextField4.setDisable(false);
			logTextArea.setDisable(false);
		}
		else {
			urlTextField.setDisable(false);
			topicTextField.setDisable(false);
			connectButton.setText("Connexion");
			brokerTextField1.setDisable(true);
			brokerTextField2.setDisable(true);
			brokerTextField3.setDisable(true);
			brokerTextField4.setDisable(true);
			logTextArea.setDisable(true);
		}

	}
	public void run() {
		cartes.add("335A791B0B");
		cartes.add("BB0A5D0DE1");
		while(this.isSignedIn) {
			try {
				if(SimpleMqttCallBack.detect==true && SimpleMqttCallBack.s.isEmpty()!=true) { 	//Qd message arrive et que la pile n'est pas vide
					System.out.println("Ok");
					// Division of the message
					String[] tab = SimpleMqttCallBack.s.getLast().split("T");
					datelue = tab[0];
					heurelue=tab[1];
					UIDlue=tab[2];
					
					//Looking for the UID read in the stored UID
					if(cartes.contains(UIDlue)) {
						System.out.println(cartes.indexOf(UIDlue));
						//If the card hasn't been read yet
						if (active[cartes.indexOf(UIDlue)]==false) {
							date_arrivee=datelue;
							heure_arrivee=heurelue;
							pointage[0]=UIDlue+" est arrivé à "+ heure_arrivee + " le "+date_arrivee+ "\n";
							brokerTextField1.setText(pointage[0]);
							brokerTextField2.setText("En attente");
							enregistrer(pointage[0]);
							active[cartes.indexOf(UIDlue)]=true;
						}
						else {
							heure_depart=heurelue;
							date_depart=datelue;
							pointage[1]=UIDlue+" est parti à "+ heure_depart + " le "+date_depart+ "\n";
							brokerTextField2.setText(pointage[1]);
							enregistrer(pointage[1]);
							active[cartes.indexOf(UIDlue)]=false;
						}
					}
					else System.out.println("Carte inconnue!");	
				}
				Thread.sleep(1000);
			}
			catch(Exception e) {
				System.out.println("Erreur: "+e);
			}
		}

	}


	private void enregistrer(String p) {

		logTextArea.appendText(p);
		System.out.println(SimpleMqttCallBack.s);

	}


	public static void main( String[] args ) throws MqttException
	{
		Application.launch(args);
	}
}