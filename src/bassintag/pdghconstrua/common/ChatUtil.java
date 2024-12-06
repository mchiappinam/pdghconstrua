package bassintag.pdghconstrua.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtil {
	
	private final static String PREFIX = "§7";
	
	public static void send(Player p, String message){
		p.sendMessage(PREFIX + message);
	}

	public static void broadcast(String message){
		Bukkit.broadcastMessage(PREFIX + message);
	}
}
