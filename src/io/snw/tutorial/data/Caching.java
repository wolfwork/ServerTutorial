
package io.snw.tutorial.data;

import io.snw.tutorial.MapPlayerTutorial;
import io.snw.tutorial.PlayerData;
import io.snw.tutorial.ServerTutorial;
import io.snw.tutorial.Tutorial;
import io.snw.tutorial.TutorialConfigs;
import io.snw.tutorial.TutorialView;
import io.snw.tutorial.enums.MessageType;
import io.snw.tutorial.enums.ViewType;
import io.snw.tutorial.util.TutorialUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

public class Caching {
	
    private ArrayList<String> tutorialNames = new ArrayList<String>();
    private HashMap<String, Tutorial> tutorials = new HashMap<String, Tutorial>();
    private HashMap<String, TutorialConfigs> configs = new HashMap<String, TutorialConfigs>();
    //player name, tutorial name
    private HashMap<String, String> currentTutorial = new HashMap<String, String>();
    private HashMap<String, Integer> currentTutorialView = new HashMap<String, Integer>();
    private ArrayList<String> playerInTutorial = new ArrayList<String>();
    private Map<String, UUID> response = null;
    private HashMap<String, PlayerData> playerDataMap = new HashMap<String, PlayerData>();
    private static Caching instance;

    public void casheAllData() {
        if (DataLoading.getDataLoading().getData().getString("tutorials") == null) {
            return;
        }
        for (String tutorialName : DataLoading.getDataLoading().getData().getConfigurationSection("tutorials").getKeys(false)) {
            this.tutorialNames.add(tutorialName.toLowerCase());
            HashMap<Integer, TutorialView> tutorialViews = new HashMap<Integer, TutorialView>();
            if (DataLoading.getDataLoading().getData().getConfigurationSection("tutorials." + tutorialName + ".views") != null) {
                for (String vID : DataLoading.getDataLoading().getData().getConfigurationSection("tutorials." + tutorialName + ".views").getKeys(false)) {
                    int viewID = Integer.parseInt(vID);
                    MessageType messageType = MessageType.valueOf(DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".views." + viewID + ".messagetype", "META"));
                    TutorialView view = new TutorialView(viewID, DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".views." + viewID + ".message", "No message written"), TutorialUtils.getTutorialUtils().getLocation(tutorialName, viewID), messageType);
                    tutorialViews.put(viewID, view);
                }
            }
            ViewType viewType = ViewType.valueOf(DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".viewtype", "CLICK"));
            String timeLengthS = DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".timelength", "10");
            int timeLength = Integer.parseInt(timeLengthS);
            String endMessage = DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".endmessage", "Sample end message");
            Material item = Material.matchMaterial(DataLoading.getDataLoading().getData().getString("tutorials." + tutorialName + ".item", "stick"));
            Tutorial tutorial = new Tutorial(tutorialName, tutorialViews, viewType, timeLength, endMessage, item);
            Setters.getSetters().addTutorial(tutorialName, tutorial);
        }
    }
    
    public void cachePlayerData() {
        if (DataLoading.getDataLoading().getPlayerData().getString("players") != null) {
            for (String uuid : DataLoading.getDataLoading().getPlayerData().getConfigurationSection("players").getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuid);
                boolean seenOnServer = Boolean.valueOf(DataLoading.getDataLoading().getData().getString("players." + uuid + ".seen"));
                HashMap<String, MapPlayerTutorial> playerTutorials = new HashMap<String, MapPlayerTutorial>();
                if (DataLoading.getDataLoading().getPlayerData().getConfigurationSection("players." + uuid + ".tutorials") != null) {
                    for (String playerTutorial : DataLoading.getDataLoading().getPlayerData().getConfigurationSection("players." + uuid + ".tutorials").getKeys(false)) {
                        String playerTutorialName = playerTutorial;
                        boolean seen = Boolean.valueOf(DataLoading.getDataLoading().getPlayerData().getString("players." + uuid + ".tutorials." + playerTutorialName));
                    MapPlayerTutorial mapPlayerTutorial = new MapPlayerTutorial(playerTutorialName, seen);
                    playerTutorials.put(playerTutorialName, mapPlayerTutorial);
                    }
                }
                PlayerData playerData = new PlayerData(playerUUID, seenOnServer,  playerTutorials);
                this.playerDataMap.put(ServerTutorial.getInstance().getServer().getOfflinePlayer(playerUUID).getName(), playerData);
            }
        }
    }

    public ArrayList<String> tutorialNames() {
        return this.tutorialNames;
    }

    public HashMap<String, Tutorial> tutorial() {
        return this.tutorials;
    }

    public HashMap<String, String> currentTutorial() {
        return this.currentTutorial;
    }

    public HashMap<String, Integer> currentTutorialView() {
        return this.currentTutorialView;
    }

    public HashMap<String, TutorialConfigs> configs() {
        return this.configs;
    }
    
    public HashMap<String, PlayerData> playerDataMap() {
        return this.playerDataMap;
    }

    public ArrayList<String> playerInTutorial() {
        return playerInTutorial;
    }

    public Map<String, UUID> getResponse() {
        return this.response;
    }

    public void cacheConfigs() {
        TutorialConfigs configOptions = new TutorialConfigs(ServerTutorial.getInstance().getConfig().getBoolean("auto-update"), ServerTutorial.getInstance().getConfig().getBoolean("metrics"), ServerTutorial.getInstance().getConfig().getString("sign"), ServerTutorial.getInstance().getConfig().getBoolean("first_join"), ServerTutorial.getInstance().getConfig().getString("first_join_tutorial"), 
        ServerTutorial.getInstance().getConfig().getBoolean("rewards"), ServerTutorial.getInstance().getConfig().getBoolean("exp_countdown"), ServerTutorial.getInstance().getConfig().getBoolean("view_money"), ServerTutorial.getInstance().getConfig().getBoolean("view_exp"), ServerTutorial.getInstance().getConfig().getBoolean("tutorial_money"), ServerTutorial.getInstance().getConfig().getBoolean("tutorial_exp"), 
        Double.valueOf(ServerTutorial.getInstance().getConfig().getString("per_tutorial_money")), Integer.valueOf(ServerTutorial.getInstance().getConfig().getString("per_tutorial_exp")), Integer.valueOf(ServerTutorial.getInstance().getConfig().getString("per_view_exp")), Double.valueOf(ServerTutorial.getInstance().getConfig().getString("per_view_money")));
        this.addConfig(configOptions);
    }

    public void addConfig(TutorialConfigs configs) {
        this.configs.put("config", configs);
    }

    public void reCasheTutorials() {
        this.tutorials.clear();
        this.tutorialNames.clear();
        casheAllData();
    }

    public void reCacheConfigs() {
        this.configs.clear();
        cacheConfigs();
    }
    
    public void reCachePlayerData() {
        this.playerDataMap().clear();
        cachePlayerData();
    }
    
    public static Caching getCaching() {
        if (instance == null)
            instance = new Caching();
        return instance;
    }
}
