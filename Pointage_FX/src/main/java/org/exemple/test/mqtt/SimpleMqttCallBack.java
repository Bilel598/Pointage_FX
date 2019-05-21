package org.exemple.test.mqtt;

import java.util.LinkedList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleMqttCallBack implements MqttCallback {
	public static String tst;
	public static LinkedList<String> s = new LinkedList<String>();
	public static boolean detect=false;
	public static int nbmessage=0;

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {

		detect=true;
		nbmessage++;
		tst= new String (message.getPayload());
		s.add (tst);
		if(s.size()==5) 
			s.removeFirst();
		Thread.sleep(1500);
		detect=false;
//		System.out.println(s);

	}
	 public void test(){
		 if (detect==true)
				 detect=false;
	 }

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("OK");
	}

}
