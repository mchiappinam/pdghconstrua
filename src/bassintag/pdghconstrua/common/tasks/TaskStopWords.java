package bassintag.pdghconstrua.common.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import bassintag.pdghconstrua.common.buildZone.BuildZone;

public class TaskStopWords extends BukkitRunnable{
	
	private BuildZone buildzone;

	@Override
	public void run() {
		buildzone.setNotAcceptWords();
	}

}
