package sample;

import java.io.IOException;

import centers.AmbulanceCentreAgent;
import centers.FireStationAgent;
import centers.PoliceOfficeAgent;
import newAgents.AmbulanceAgent;
import newAgents.FireAgent;
import newAgents.PoliceAgent;
import rescuecore.objects.AmbulanceCenter;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.Constants;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

/**
   Launcher for sample agents. This will launch as many instances of each of the sample agents as possible, all using one connction.
 */
public final class LaunchSampleAgents {
    private static final String FIRE_BRIGADE_FLAG = "-fb";
    private static final String POLICE_FORCE_FLAG = "-pf";
    private static final String AMBULANCE_TEAM_FLAG = "-at";
    private static final String CIVILIAN_FLAG = "-cv";

    private LaunchSampleAgents() {}

    /**
       Launch 'em!
       @param args The following arguments are understood: -p <port>, -h <hostname>, -fb <fire brigades>, -pf <police forces>, -at <ambulance teams>
    */
    public static void main(String[] args) {
        Logger.setLogContext("sample");
        try {
            Registry.SYSTEM_REGISTRY.registerEntityFactory(StandardEntityFactory.INSTANCE);
            Registry.SYSTEM_REGISTRY.registerMessageFactory(StandardMessageFactory.INSTANCE);
            Registry.SYSTEM_REGISTRY.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
            Config config = new Config();
            // args = CommandLineOptions.processArgs(args, config);
            int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY, Constants.DEFAULT_KERNEL_PORT_NUMBER);
            String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY, Constants.DEFAULT_KERNEL_HOST_NAME);
            
            int fb = 20;
            int pf = 20;
            int at = 20; 

            /*
            // CHECKSTYLE:OFF:ModifiedControlVariable
            for (int i = 0; i < args.length; ++i) {
                if (args[i].equals(FIRE_BRIGADE_FLAG)) {
                    fb = Integer.parseInt(args[++i]);
                }
                else if (args[i].equals(POLICE_FORCE_FLAG)) {
                    pf = Integer.parseInt(args[++i]);
                }
                else if (args[i].equals(AMBULANCE_TEAM_FLAG)) {
                    at = Integer.parseInt(args[++i]);
                }
                else {
                    Logger.warn("Unrecognised option: " + args[i]);
                }
            } */
            // CHECKSTYLE:ON:ModifiedControlVariable
            ComponentLauncher launcher = new TCPComponentLauncher(host, port, config);
            //System.out.println("Chegou ate aqui ... "+host+"\t"+port);
            connect(launcher, fb, pf, at, config);
        }/*
        catch (IOException e) {
            Logger.error("Error connecting agents", e);
        } /*
        catch (ConfigException e) {
            Logger.error("Configuration error", e);
        } */
        catch (ConnectionException e) {
            Logger.error("Error connecting agents", e);
        }
        catch (InterruptedException e) {
            Logger.error("Error connecting agents", e);
        }
    }

    private static void connect(ComponentLauncher launcher, int fb, int pf, int at, Config config) throws InterruptedException, ConnectionException {
        int i = 0;
        int ac = 1; // Ambulance Centre
        int po = 1; // Police Office
        int fs = 1; // Fire Station
        try {
            while (fb-- != 0) {
            	System.out.println("Connecting fire brigade " + (i++) + "...");
           //     Logger.info("Connecting fire brigade " + (i++) + "...");
                //System.out.println(i);
                //SampleFireBrigade f = new SampleFireBrigade();
            	FireAgent f = new FireAgent();
                //f.setId(i);
                launcher.connect(f);
                //System.out.println("(F)i->" + i + " ID->" + f.getId());
                Logger.info("success");
            }
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
        }
        try {
            while (pf-- != 0) {
            	System.out.println("Connecting police force " + (i++) + "...");
            	//Logger.info("Connecting police force " + (i++) + "...");
            	//SamplePoliceForce p = new SamplePoliceForce();
                //p.setId(i);
            	PoliceAgent p = new PoliceAgent();
            	launcher.connect(p);
            	//System.out.println("(P)i->" + i + " ID->" + p.getId());
                Logger.info("success");
            }
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
        }
        try {
            while (at-- != 0) {
            	System.out.println("Connecting ambulance team " + (i++) + "...");
                //Logger.info("Connecting ambulance team " + (i++) + "...");
                // SampleAmbulanceTeam a = new SampleAmbulanceTeam();
            	AmbulanceAgent a = new AmbulanceAgent();
                // a.setId(i);
                launcher.connect(a);
                // System.out.println("(A)i->" + i + " ID->" + a.getId());
                Logger.info("success");
            }
            i = 0;
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
        }
        try {
            while (ac-- != 0) {
                Logger.info("Connecting centre " + (i++) + "...");
                System.out.println("Connecting sample centre " + (i) + "...");
                launcher.connect(new AmbulanceCentreAgent());
                Logger.info("success");
            }
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
            System.out.println("Failed Connecting sample centre " + (i) + "...");
        }
        try {
            while (po-- != 0) {
                Logger.info("Connecting centre " + (i++) + "...");
                System.out.println("Connecting sample centre " + (i) + "...");
                launcher.connect(new PoliceOfficeAgent());
                Logger.info("success");
            }
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
            System.out.println("Failed Connecting sample centre " + (i) + "...");
        }
        try {
            while (fs-- != 0) {
                Logger.info("Connecting centre " + (i++) + "...");
                System.out.println("Connecting sample centre " + (i) + "...");
                launcher.connect(new FireStationAgent());
                Logger.info("success");
            }
        }
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
            System.out.println("Failed Connecting sample centre " + (i) + "...");
        }
      /*try {
            while (true) {
                Logger.info("Connecting dummy agent " + (i++) + "...");
                launcher.connect(new DummyAgent());
                Logger.info("success");
            }
        } 
       
        catch (ComponentConnectionException e) {
            Logger.info("failed: " + e.getMessage());
        } */
        i = 0;
        try {
            while (true) {
                Logger.info("Connecting civilian agent " + (i++) + "...");
                System.out.println("Connecting sample civilian " + (i) + "...");
                launcher.connect(new SampleCivilian());
                Logger.info("success");
            }
        }
       
        catch (ComponentConnectionException e) {
        	 System.out.println("Failed Connecting sample civilian " + (i) + "..." + e.getMessage());
            Logger.info("failed: " + e.getMessage());
        }
    }
}
