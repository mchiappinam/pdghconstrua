package bassintag.pdghconstrua.common.buildZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import bassintag.pdghconstrua.common.BuildMyThing;
import bassintag.pdghconstrua.common.ChatUtil;
import bassintag.pdghconstrua.common.LocationUtil;
import bassintag.pdghconstrua.common.cuboid.CuboidZone;
import bassintag.pdghconstrua.common.tasks.TaskAlert;
import bassintag.pdghconstrua.common.tasks.TaskNextRound;
import bassintag.pdghconstrua.common.tasks.TaskStart;

public class BuildZone implements Listener {
	
	BuildMyThing instance;
	
	private CuboidZone buildzone;
	private Location spectateTP;
	
	private Map<Player, Integer> score = new HashMap<Player, Integer>();
	private Map<Player, Boolean> ready = new HashMap<Player, Boolean>();
	private Map<Player, Integer> hasBeenBuilder = new HashMap<Player, Integer>();
	
	private List<Player> hasFound = new ArrayList<Player>();
	private Player builder;
	
	private int players;
	private int maxplayers = 12;
	private int buildPerPlayer = 2;
	private String name;
	
	private String word;
	
	private int wordHasBeenFound = 3;
	private int playerFound = 0;
	
	private List<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();
	
	private boolean acceptWords = true;
	
	private List<Block> signs = new ArrayList<Block>();
	
	ScoreboardManager manager = Bukkit.getScoreboardManager();
	Scoreboard board = manager.getNewScoreboard();
	Objective objective;

	private boolean usesCustomWords;
	
	public BuildZone(CuboidZone build, Location loc, String name, BuildMyThing instance){
		this.buildzone = build;
		this.spectateTP = loc;
		this.name = name;
		this.instance = instance;
		objective = board.registerNewObjective(this.name + "_points", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("§3§lConstrua");
	}
	
	public void cancelTasks(){
		for(BukkitRunnable r : this.tasks){
			r.cancel();
		}
	}
	
	public void leave(Player player){
		if(this.score.containsKey(player)){
			if(this.instance.started == false){
				if(this.players == this.maxplayers){
					this.sendMessage("§3§l[Construa] §fAlguém deixou o jogo, jogo não será iniciado até que todos estejam prontos");
					this.cancelTasks();
				}
			}
			this.score.remove(player);
			this.ready.remove(player);
			this.hasBeenBuilder.remove(player);
			if(this.hasFound(player)){
				this.playerFound--;
			}
			this.hasFound.remove(player);
			player.setAllowFlight(false);
			this.board.resetScores(player);
			player.setScoreboard(manager.getMainScoreboard());
			this.players--;
			player.teleport(LocationUtil.StringToLoc(player.getMetadata("oldLoc").get(0).asString()));
			player.removeMetadata("oldLoc", instance);
			player.removeMetadata("inbmt", instance);
			//ChatUtil.send(player, "§3§l[Construa] §fVocê saiu do jogo!");
			if(this.players > 1){
				this.sendMessage("§3§l[Construa] §f"+player.getName()+" saiu do jogo!");
			} else if(this.instance.isStarted()){
				this.cancelTasks();
				this.sendMessage("§3§l[Construa] §fDesculpe. A quantidade de pessoas não é suficiente. Parando o jogo...");
				this.sendMessage("§3§l[Construa] §f"+player.getName()+" saiu do jogo!");
				this.stop();
			}
		}
	}
	
	public void join(Player player){
		if(!instance.isStarted()){
			if(!this.score.containsKey(player)){
				player.setMetadata("oldLoc", new FixedMetadataValue(instance, LocationUtil.LocationToString(player.getLocation())));
				player.teleport(this.spectateTP);
				if(this.players < this.maxplayers){
					player.setFoodLevel(20);
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
					player.getInventory().clear();
					player.setGameMode(GameMode.ADVENTURE);
					player.setMetadata("inbmt", new FixedMetadataValue(instance, this.getName()));
					player.setScoreboard(board);
					this.score.put(player, 0);
					this.ready.put(player, true);
					this.players += 1;
					if(this.players > 0){
						for(Player p : score.keySet()){
							ChatUtil.send(p, "§3§l[Construa] §f"+player.getName()+" entrou no jogo ("+String.valueOf(this.players)+"/"+String.valueOf(this.maxplayers)+")");
							setReady(player);
						}
						this.increaseScore(player, 0);
					}
					
					if(this.players == this.maxplayers){
						this.cancelTasks();
						this.sendMessage("§3§l[Construa] §fSala cheia! Começando em 10 segundos!");
						TaskStart start = new TaskStart(this);
						start.runTaskLater(instance, 200);
						this.tasks.add(start);
						this.increaseScore(player, 0);
					}
					
				} else {
					ChatUtil.send(player, "§3§l[Construa] §fSala cheia!");
				}
			}
		} else {
			ChatUtil.send(player, "§3§l[Construa] §fO jogo nessa sala já começou!");
		}
			
	}
	
	private String getNewWord(){
		if(this.usesCustomWords){
			return this.getRandomWordFromConfig();
		} else {
			return instance.getRandomWord();
		}
	}
	
	private String getRandomWordFromConfig(){
		@SuppressWarnings("unchecked")
		List<String> words = (List<String>)(this.instance.getConfig().getList("rooms" + this.name + ".custom-word-list"));
		if(words.size() > 0){
			int i = words.size();
			Random r = new Random();
			return words.get(r.nextInt(i));
		} else {
			return "null";
		}
	}
	
	public void start(){
		if(!instance.started){
			this.cancelTasks();
			this.instance.started = true;
			this.word = null;
			if(this.players < 3){
				this.buildPerPlayer = 3;
			}
			this.hasBeenBuilder.clear();
			for(Player p : this.score.keySet()){
				this.hasBeenBuilder.put(p, 0);
			}
			this.startRound();
		}
	}
	
	public void startRound(){
		if(this.word != null){
			this.sendMessage("§3§l[Construa] §fA palavra era: §2"+word);
			
			/**this.sendMessage("Pontos:");
			for(Player p : score.keySet()){
				this.sendMessage(p.getName()+" ["+String.valueOf(score.get(p))+"]");
			}*/
		}
		
		this.cancelTasks();
		
		this.wordHasBeenFound = 3;
		this.playerFound = 0;
		
		this.hasFound.clear();
		this.acceptWords = true;
		this.word = this.getNewWord();
		this.buildzone.clear();
		this.getNextBuilder();
		
		//TaskAlert alert1 = new TaskAlert("§3§l[Construa] §fFaltam 60 segundos!", this.getPlayers());
		//TaskAlert alert2 = new TaskAlert("§3§l[Construa] §fFaltam 30 segundos!", this.getPlayers());
		//TaskAlert alert3 = new TaskAlert("§3§l[Construa] §fFaltam 10 segundos!", this.getPlayers());
		TaskNextRound endRound = new TaskNextRound(this);
		endRound.runTaskLater(instance, 1800);
		this.tasks.add(endRound);
		TaskAlert endRoundMsg = new TaskAlert("§3§l[Construa] §fO tempo acabou! Começando o próximo round!", this.getPlayers());
		endRoundMsg.runTaskLater(instance, 1800);
		this.tasks.add(endRoundMsg);
	}
	
	public void removePlayerFromAlerts(Player p){
		for(BukkitRunnable task : this.tasks){
			if(task instanceof TaskAlert){
				TaskAlert taskAlert = (TaskAlert)task;
				taskAlert.removePlayer(p);
			}
		}
	}
	
	public List<Player> getPlayers(){
		List<Player> result = new ArrayList<Player>();
		for(Player p : this.score.keySet()){
			result.add(p);
		}
		
		return result;
	}
	
	public void stop(){
		
		this.instance.started = false;
		
		List<Player> toKick = new ArrayList<Player>();
		for(Player p : this.score.keySet()){
			toKick.add(p);
		}
		
		for(Player p : toKick){
			this.leave(p);
		}
	}
	
	private void getNextBuilder(){
		if(this.getBuilder() != null){
			if(this.instance.getConfig().getBoolean("allow-creative")){
				this.builder.setGameMode(GameMode.ADVENTURE);
			} else {
				this.builder.setAllowFlight(false);
			}
			this.builder.teleport(this.spectateTP);
			this.builder.getInventory().setHelmet(null);
			this.builder.getInventory().setChestplate(null);
			this.builder.getInventory().setLeggings(null);
			this.builder.getInventory().setBoots(null);
			this.builder.getInventory().clear();
			this.builder = null;
		}
		for(int i = 0; i < this.buildPerPlayer; i++){
			for(Player p : this.hasBeenBuilder.keySet()){
				if(this.hasBeenBuilder.get(p) > i){
					continue;
				} else {
					this.setBuilder(p);
					return;
				}
			}
		}
		this.sendMessage("§3§l[Construa] §fFIM DO JOGO!");
		Player winner = null;
		for(Player p : this.score.keySet()){
			if(winner != null){
				if(this.score.get(p) > this.score.get(winner)){
					winner = p;
				}
			} else {
				winner = p;
			}
		}
		
		if(this.score.containsKey(winner)){
			int i = score.get(winner);
			this.sendMessage("§3§l[Construa] §fVENCEDOR: "+winner.getName()+" ["+String.valueOf(i)+"]");
			if(instance.getConfig().getBoolean("broadcast-on-game-over")){
				//ChatUtil.broadcast(instance.translator.get("broadcast-name").replace("$player", winner.getName()).replace("$room", this.getName()));
			}
		}
		this.stop();
	}
	
	private void setBuilder(Player p){
		this.builder = p;
		this.hasBeenBuilder.put(p, this.hasBeenBuilder.get(p) + 1);
		p.teleport(this.buildzone.getBottomCenter());
		if(this.instance.getConfig().getBoolean("allow-creative")){
			p.setGameMode(GameMode.CREATIVE);
		} else {
			p.setAllowFlight(true);
			for(short i = 0; i < 16; i++){
				p.getInventory().addItem(new ItemStack(Material.STAINED_CLAY, 64, i));
			}
		}
		this.sendMessage("§3§l[Construa] §f"+p.getName()+" está construindo agora!");
		this.sendMessage("§3§l[Construa] §fVocê tem 90 segundos para acertar a palavra!");
		ChatUtil.send(p, " ");
		ChatUtil.send(p, "§3§l[Construa] §fA palavra é: §c§l"+word);
		ChatUtil.send(p, " ");
	}
	
	public void sendMessage(String message){
		for(Player p : score.keySet()){
			ChatUtil.send(p, message);
		}
	}

	public String getName() {
		return name;
	}
	
	public void save(FileConfiguration file){
		file.set("rooms" + this.getName() + ".pos1", LocationUtil.LocationToString(this.buildzone.getCorner1().getLocation()));
		file.set("rooms" + this.getName() + ".pos2", LocationUtil.LocationToString(this.buildzone.getCorner2().getLocation()));
		file.set("rooms" + this.getName() + ".spawn", LocationUtil.LocationToString(this.spectateTP));
		file.set("rooms" + this.getName() + ".maxplayers", this.maxplayers);
		file.addDefault("rooms" + this.getName() + ".custom-words", false);
		List<String> signData = new ArrayList<String>();
		for(Block s : this.signs){
			if (s.getType() == Material.WALL_SIGN){
				String loc = LocationUtil.LocationToString(s.getLocation());
				String display;
				if(s.hasMetadata("display")){
					display = ";" + s.getMetadata("display").get(0).asString();
				} else {
					display = ";none";
				}
				String result = loc + display;
				signData.add(result);
			}
		}
		file.set("rooms" + this.getName() + ".signs", signData);
	}
	
	public void remove(FileConfiguration file){
		this.stop();
		file.set(this.getName(), null);
		this.instance.saveConfig();
	}
	
	public static BuildZone load(FileConfiguration file, String name, BuildMyThing instance){
		Location corner1 = LocationUtil.StringToLoc(file.getString("rooms" + name + ".pos1"));
		Location corner2 = LocationUtil.StringToLoc(file.getString("rooms" + name + ".pos2"));
		Location spawn = LocationUtil.StringToLoc(file.getString("rooms" + name + ".spawn"));
		BuildZone b = new BuildZone(new CuboidZone(corner1.getBlock(), corner2.getBlock()), spawn, name, instance);
		b.setMaxPlayers(file.getInt("rooms" + name + ".maxplayers"));
		if(file.getBoolean("rooms" + name + ".custom-words")){
			file.addDefault("rooms" + name + ".custom-word-list", BuildMyThing.DEFAULT_WORDS);
			b.setUsesCustomWords(true);
		}
		return b;
	}
	
	public void setUsesCustomWords(Boolean b){
		this.usesCustomWords = true;
	}
	
	public void setMaxPlayers(int i){
		this.maxplayers = i;
	}
	
	private boolean isEveryoneReady(){
		if(this.ready.size() > 0){
			for(Player p : ready.keySet()){
				if(ready.get(p) == true){
					continue;
				} else {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	boolean iniciado = true;
	int taskID;
	public void setReady(Player player) {
		if(!this.instance.isStarted()){
					this.ready.put(player, true);
					player.setLevel(100);
					//this.sendMessage("§3§l[Construa] §f"+player.getName()+" está pronto!");
					if(this.players > 1){
						if(this.isEveryoneReady()){
							if(iniciado) {
								iniciado=false;
								taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
						    		int timer = 100;
							      public void run() {
							    		timer = timer -1;
										for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							          p.setLevel(timer);
							          if ((timer < 11) && (timer > 0)) {
							            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
							          }
							        }
										if(timer <1) {
											cancelTask();
										}
							        if (((timer <= 10) && (timer > 1)) || (timer == 10) || (timer == 20) || (timer == 30) || (timer == 40) || (timer == 50) || (timer == 60) || (timer == 70) || (timer == 80) || (timer == 90) || (timer == 100)) {
							          Bukkit.broadcastMessage("§3§l[Construa] §f"+timer+" segundos para o inicio da partida.");
							        }
							        if(timer == 1) {
								          Bukkit.broadcastMessage("§3§l[Construa] §f"+timer+" segundo para o inicio da partida.");
								        }
							        if(timer == 0) {
								          Bukkit.broadcastMessage("§3§l[Construa] §fIniciando...");
								        }
							      }
								}, 0, 20*1);
							}
							//this.sendMessage("§3§l[Construa] §f2 ou mais jogadores na sala. Iniciando contagem regressiva.");
							TaskStart start = new TaskStart(this);
							start.runTaskLater(instance, 2000);
							this.tasks.add(start);
						}
			}
		}
	}
	private void cancelTask() {
	    Bukkit.getServer().getScheduler().cancelTask(taskID);
	}
	public Player getBuilder() {
		return builder;
	}

	public CuboidZone getBuildZone() {
		return this.buildzone;
	}

	public String getWord() {
		return word;
	}
	
	public void increaseScore(Player p, int value){
		if(this.score.containsKey(p)){
			this.score.put(p, this.score.get(p) + value);
			Score scoreBoard = objective.getScore(p);
			scoreBoard.setScore(this.score.get(p));
		}
	}

	public void wordFoundBy(Player player) {
		if(this.acceptWords && !this.hasFound.contains(player)){
			
			if(wordHasBeenFound==3){
				this.hasFound.add(player);
				
				for(Player p : this.score.keySet()){
					p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
				}
				this.sendMessage("§3§l[Construa] §2§l"+player.getName()+" §facertou! [§a+3§f]");
				this.sendMessage("§3§l[Construa] §c"+builder.getName()+" §ftambém ganhou! [§a+2§f]");
				this.increaseScore(player, 3);
				this.increaseScore(builder, 2);
				wordHasBeenFound = wordHasBeenFound -1;
			} else if(wordHasBeenFound==2){
				this.hasFound.add(player);
				
				for(Player p : this.score.keySet()){
					p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
				}
				this.sendMessage("§3§l[Construa] §2§l"+player.getName()+" §facertou! [§a+1§f]");
				this.increaseScore(player, 1);
				wordHasBeenFound = wordHasBeenFound -1;
			} else if(wordHasBeenFound==1){
				this.hasFound.add(player);
				
				for(Player p : this.score.keySet()){
					p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
				}
				this.sendMessage("§3§l[Construa] §2§l"+player.getName()+" §facertou! [§a+1§f]");
				this.sendMessage("§3§l[Construa] §2Os 3 primeiros jogadores acertaram! §a:)");
				this.sendMessage("§3§l[Construa] §c§lIniciando a próxima partida...");
				wordHasBeenFound = wordHasBeenFound -1;
				this.increaseScore(player, 1);
				this.cancelTasks();
				TaskNextRound nextRound = new TaskNextRound(this);
				nextRound.runTaskLater(instance, 65); //100
			}else{
				player.sendMessage("§3§l[Construa] §cAguarde a próxima partida");
			}
			this.playerFound++;
		}
		
		if(this.playerFound == this.players - 1){
			this.sendMessage("§3§l[Construa] §2Todo mundo acertou a palavra! §a:)");
			//this.sendMessage(instance.translator.get("next-round"));
			this.cancelTasks();
			TaskNextRound endRound = new TaskNextRound(this);
			endRound.runTaskLater(instance, 0); //100
			this.tasks.add(endRound);
		}
	}

	public void setNotAcceptWords() {
		this.acceptWords = false;
		
	}

	public int getMaxPlayers() {
		return this.maxplayers;
	}

	public boolean hasFound(Player player) {
		return this.hasFound.contains(player);
	}

	public void abondon(Player player) {
		if(this.builder.equals(player)){
			this.sendMessage("§3§l[Construa] §cO construtor abandonou a partida!");
				this.sendMessage("§3§l[Construa] §f"+player.getName()+" recebeu uma penalidade! §c[-3]");
				this.sendMessage("§3§l[Construa] §c§lIniciando a próxima partida...");
				this.decreaseScore(player, 3);
			//this.sendMessage(instance.translator.get("next-round"));
			this.cancelTasks();
			TaskNextRound nextRound = new TaskNextRound(this);
			nextRound.runTaskLater(instance, 65); //100
		}
	}

	private void decreaseScore(Player player, int i) {
		this.score.put(player, this.score.get(player) - i);
		if(this.score.get(player) < 0){
			this.score.put(player, 0);
		}
	}
}
