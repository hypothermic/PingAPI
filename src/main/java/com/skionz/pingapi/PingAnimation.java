package com.skionz.pingapi;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.skionz.pingapi.PingEvent;
import com.skionz.pingapi.PingReply;
import com.skionz.pingapi.ServerInfoPacket;

public class PingAnimation {
	
	private final PingEvent event;
	private final Plugin plugin;
	private final String[] motdAnimation, protocolAnimation;
	private final int stepAmount, ticksPerStep;
	
	private int taskID;
	
	/**
	 * Constructs an animation of ping packets to send to the client.
	 * The supplied String arrays hold the actual animation (color codes are translated).
	 * Each step uses the next value, looping back to the start after it reached the end.
	 * Note that it will keep running until the supplied step amount is reached, even if the client closes the server list.
	 * @param plugin Instance of your plugin.
	 * @param event PingEvent to animate
	 * @param stepAmount The max amount of steps, after which the animation will stop.
	 * @param ticksPerStep Amount of ticks to wait between steps
	 * @param motd An array of Strings to animate the MOTD with. Each step uses the next value, looping back to the start after it reached the end.
	 */
	public PingAnimation(Plugin plugin, PingEvent event, int stepAmount, int ticksPerStep, String[] motd) {
		this(plugin, event, stepAmount, ticksPerStep, motd, null);
	}
	/**
	 * Constructs an animation of ping packets to send to the client.
	 * The supplied String arrays hold the actual animation (color codes are translated).
	 * Each step uses the next value, looping back to the start after it reached the end.
	 * Note that it will keep running until the supplied step amount is reached, even if the client closes the server list.
	 * @param plugin Instance of your plugin.
	 * @param event PingEvent to animate
	 * @param stepAmount The max amount of steps, after which the animation will stop.
	 * @param ticksPerStep Amount of ticks to wait between steps
	 * @param motd String array to animate the MOTD with.
	 * @param protocol String array to animate the protocol version with.
	 */
	public PingAnimation(Plugin plugin, PingEvent event, int stepAmount, int ticksPerStep, String[] motd, String[] protocol) {
		this.plugin = plugin;
		this.motdAnimation = motd;
		this.protocolAnimation = protocol;
		this.stepAmount = stepAmount;
		this.ticksPerStep = ticksPerStep;
		this.event = event;
	}
	
	
	/*
	 * Starts the animation
	 */
	public void start() {
		event.cancelPong(true);
		taskID = new Animation().runTaskTimerAsynchronously(plugin, 0l, ticksPerStep).getTaskId();
	}
	
	/*
	 * Forces the animation to stop
	 */
	public void stop() {
		plugin.getServer().getScheduler().cancelTask(taskID);
	}
	
	private class Animation extends BukkitRunnable {
		
		int step = 0;
		String protocol, motd;
		
		public void run() {
			
			animate();
			
			if(++step == stepAmount) {
				event.cancelPong(false);
				this.cancel();
			}
			
			ServerInfoPacket packet = event.createNewPacket(event.getReply());
			PingReply reply = packet.getPingReply();
			if(protocol != null) {
				reply.setProtocolVersion(-1);
				reply.setProtocolName(protocol);
			}
			reply.setMOTD(motd);
			
			packet.setPingReply(reply);
			packet.send();

		}
		
		void animate() {
			motd = ChatColor.translateAlternateColorCodes('&', motdAnimation[step % motdAnimation.length] );
			if(protocolAnimation != null)
				protocol = ChatColor.translateAlternateColorCodes('&', protocolAnimation[step % protocolAnimation.length] );
		}
		
		
	}

}
