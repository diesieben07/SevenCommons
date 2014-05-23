package de.take_weiland.mods.commons;

import com.google.common.base.Predicate;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author diesieben07
 */
public class Test {

	public static void main(String[] args) throws IllegalAccessException, InstantiationException, InterruptedException {
		Object o = Proxy.newProxyInstance(new URLClassLoader(new URL[] { }, Test.class.getClassLoader()), new Class<?>[] {Predicate.class}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return true;
			}
		});

		ReferenceQueue<Class<?>> refQueue = new ReferenceQueue<>();
		PhantomReference<Class<?>> ref = new PhantomReference<Class<?>>(o.getClass(), refQueue);

		o = null;

		do {
			System.gc();
			Reference<?> polled = refQueue.poll();
			if (polled == ref) {
				System.out.println(polled);
				System.out.println(polled.get());
				break;
			}
			Thread.sleep(1000);
		} while (true);
	}

}
