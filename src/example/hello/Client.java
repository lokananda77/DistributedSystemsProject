package example.hello;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private Client() {}

    public static void main(String[] args) {

        //String host = (args.length < 1) ? null : args[0];
    	String host = "169.254.11.189";
        try {
            Registry registry = LocateRegistry.getRegistry(host,1099);
            System.out.println(registry.REGISTRY_PORT);
            Hello stub = (Hello) registry.lookup("Hello");
            
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}