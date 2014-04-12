package de.take_weiland.mods.commons;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.util.ScheduledListenableFuture;
import de.take_weiland.mods.commons.util.Scheduler;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

//@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/testmod.json");

		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
		if (Sides.logical(event.entity).isServer()) {
			final ScheduledListenableFuture<String> future = Scheduler.server().schedule(new Callable<String>() {

				@Override
				public String call() throws Exception {
					return "foobar!";
				}
			}, 10, TimeUnit.SECONDS);

			final Future<?>[] future2 = new Future<?>[1];
			future2[0] = Scheduler.server().scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					System.out.println("Left: " + future.getDelay(TimeUnit.SECONDS) + " seconds");
					if (future.isDone()) future2[0].cancel(true);
				}
			}, 0, 1, TimeUnit.SECONDS);

			future.addListener(new Runnable() {
				@Override
				public void run() {
					System.out.println("Finished with " + Futures.getUnchecked(future));
				}
			}, Scheduler.server());

			new Thread() {

				@Override
				public void run() {
					List<Callable<String>> callableList = Arrays.asList(new Callable<String>() {
						@Override
						public String call() throws Exception {
							return "foobar1";
						}
					}, new Callable<String>() {

						@Override
						public String call() throws Exception {
							return "foobar2";
						}
					});

					List<Future<String>> futures = null;
					try {
						futures = Scheduler.server().invokeAll(callableList);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (final Future<String> future3 : futures) {
						((ListenableFuture<String>) future3).addListener(new Runnable() {
							@Override
							public void run() {
								System.out.println(Futures.getUnchecked(future3));
							}
						}, Scheduler.server());
					}
				}
			}.start();

		}
	}

}
