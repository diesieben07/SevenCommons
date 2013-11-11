package de.take_weiland.mods.commons.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Longs;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public final class Scheduler implements ITickHandler, Executor {

	public static Scheduler server() {
		return server != null ? server : (server = new Scheduler(Side.SERVER));
	}
	
	public static Scheduler client() {
		return client != null ? client : (client = new Scheduler(Side.CLIENT));
	}
	
	public void schedule(Runnable task, int ticks, boolean tickEnd) {
		long when = ticks + now.get();
		synchronized (queue) {
			queue.offer(new Task(task, when, tickEnd));
		}
	}
	
	@Override
	public void execute(Runnable task) {
		schedule(task, 0, false);
	}
	
	public void throwInMainThread(final Throwable t) {
		execute(new Runnable() {
			
			@Override
			public void run() {
				JavaUtils.throwUnchecked(t);
			}
		});	
	}
	
	private static Scheduler server, client;
	
	private static class Task implements Comparable<Task> {
		
		final Runnable r;
		final long when;
		final boolean tickEnd;

		Task(Runnable r, long when, boolean tickEnd) {
			this.r = r;
			this.when = when;
			this.tickEnd = tickEnd;
		}

		@Override
		public int compareTo(Task o) {
			// tasks which come earlier are first, task which run on tickStart come last
			int whenRes = Longs.compare(this.when, o.when);
			if (whenRes != 0) {
				return whenRes;
			} else {
				return this.tickEnd ? -1 : 1;
			}
		}

		@Override
		public String toString() {
			return "Task [r=" + r + ", when=" + when + ", tickEnd=" + tickEnd + "]";
		}
		
	}
	
	private final AtomicLong now;
	private final Side side;
	private final EnumSet<TickType> ticks;
	private final PriorityQueue<Task> queue;
	private final ArrayList<Task> scheduledNow;
	
	private Scheduler(Side side) {
		this.side = side;
		ticks = EnumSet.of(side.isServer() ? TickType.SERVER : TickType.CLIENT);
		queue = Queues.newPriorityQueue();
		now = new AtomicLong(0);
		scheduledNow = Lists.newArrayList();
		
		TickRegistry.registerTickHandler(this, side);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		long now = this.now.incrementAndGet();
		List<Task> sn = scheduledNow;
		sn.clear();
		synchronized (queue) {
			while (queue.size() > 0) {
				Task next = queue.peek();
				if (next.when > now) {
					break;
				}
				sn.add(queue.poll());
			}
		}
		for (int i = sn.size() - 1; i >= 0; --i) { // traverse backwards so we can fast-remove
			if (sn.get(i).tickEnd) { // if we encounter the first tickEnd element, all others will be tickEnd, too, so we can stop traversing
				break;
			}
			sn.remove(i).r.run();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		List<Task> sn = scheduledNow;
		for (int i = sn.size() - 1; i >= 0; --i) {
			sn.remove(i).r.run();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return ticks;
	}

	@Override
	public String getLabel() {
		return "SevenCommonsScheduler[" + side + "]";
	}

}
