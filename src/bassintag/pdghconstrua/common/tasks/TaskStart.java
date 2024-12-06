package bassintag.pdghconstrua.common.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import bassintag.pdghconstrua.common.buildZone.BuildZone;

public class TaskStart extends BukkitRunnable{
	
	BuildZone b;
	
	public TaskStart(BuildZone b){
		this.b = b;
	}

	@Override
	public void run() {
		b.start();
	}

}
