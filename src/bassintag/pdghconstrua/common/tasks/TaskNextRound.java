package bassintag.pdghconstrua.common.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import bassintag.pdghconstrua.common.buildZone.BuildZone;

public class TaskNextRound extends BukkitRunnable {
	
	private final BuildZone buildZone;
	
	public TaskNextRound(BuildZone buildZone){
		this.buildZone = buildZone;
	}

	@Override
	public void run() {
		buildZone.startRound();
	}

}
