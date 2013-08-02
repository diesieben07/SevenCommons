package de.take_weiland.mods.commons.util.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.CommonUtils;

public final class ConfigInjector {

	private ConfigInjector() { }
	
	private static final ImmutableSet<Class<?>> validTypes = ImmutableSet.<Class<?>>of(
			int.class, boolean.class, double.class, String.class,
			int[].class, boolean[].class, double[].class, String[].class);
	
	/**
	 * Convenience method, equivalent to {@link ConfigInjector#inject(Configuration, Class, boolean, boolean) inject(config, clazz, true, true)}
	 * @param config
	 * @param clazz
	 */
	public static final void inject(Configuration config, Class<?> clazz) {
		inject(config, clazz, true, true);
	}
	
	/**
	 * Parse the given class for {@link GetProperty @GetProperty} annotations and process them
	 * @param config the configuration to use
	 * @param clazz the class to parse
	 * @param load if the configuration should be loaded prior to parsing
	 * @param save if the configuration should be saved after parsing
	 */
	public static final void inject(Configuration config, Class<?> clazz, boolean load, boolean save) {
		if (load) {
			config.load();
		}
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			final boolean isBlock = field.isAnnotationPresent(Block.class);
			final boolean isItem = field.isAnnotationPresent(Item.class);
			
			if (!field.isAnnotationPresent(GetProperty.class)) {
				if (isBlock || isItem) {
					SevenCommons.LOGGER.warning(String.format("Field %s in class %s has @Block or @Item annotation but not @GetProperty. That might not be intended.", field.getName(), clazz.getSimpleName()));
				}
				continue;
			}
			
			if (isItem && isBlock) {
				SevenCommons.LOGGER.warning(String.format("Field %s in class %s has both @Block and @Item annotation. That is invalid!", field.getName(), clazz.getSimpleName()));
				continue;
			}
			
			final Class<?> fieldType = field.getType();
			
			if ((isItem || isBlock) && !fieldType.equals(int.class)) {
				SevenCommons.LOGGER.warning(String.format("Field %s in class %s has @%s annotation but in not of type int. That is invalid!", field.getName(), clazz.getSimpleName(), isItem ? "Item" : "Block"));
				continue;
			}
			
			if (!Modifier.isStatic(field.getModifiers())) {
				SevenCommons.LOGGER.warning(String.format("Field %s in class %s has @GetProperty annotation but is not static. That is invalid!", field.getName(), clazz.getSimpleName()));
				continue;
			}
			
			final GetProperty ann = field.getAnnotation(GetProperty.class);
			
			final String comment = Strings.emptyToNull(ann.comment());
			final String name = Optional.fromNullable(Strings.emptyToNull(ann.name())).or(field.getName());
			
			final String category = Optional.fromNullable(Strings.emptyToNull(ann.category())).or(isItem ? Configuration.CATEGORY_ITEM : isBlock ? Configuration.CATEGORY_BLOCK : Configuration.CATEGORY_GENERAL);
			
			if (!validTypes.contains(fieldType)) {
				SevenCommons.LOGGER.warning(String.format("Field %s in class %s is of Type %s which is not valid in a configuration!", field.getName(), clazz.getSimpleName(), fieldType.getSimpleName()));
			}
			
			try {
				String getMethod = "get";
				if (isItem) {
					getMethod += "Item";
				} else if (isBlock) {
					if (field.getAnnotation(Block.class).isTerrain()) {
						getMethod += "Terrain";
					}
					getMethod += "Block";
				}
				
				// category, key, defaultValue, comment
				Method getPropertyMethod = Configuration.class.getDeclaredMethod(getMethod, String.class, String.class, fieldType, String.class);
				
				Object def = field.get(null);
				
				if (def == null) {
					if (fieldType.isArray()) {
						def = Array.newInstance(fieldType.getComponentType(), 0);
					} else if (fieldType.equals(String.class)) {
						def = "";
					}
				}
				
				Property prop = (Property) getPropertyMethod.invoke(config, category, name, def, comment);
				
				Class<?> rawType = fieldType.isArray() ? fieldType.getComponentType() : fieldType;
				StringBuilder mName = new StringBuilder().append("get");
				
				mName.append(CommonUtils.capitalize(rawType.getSimpleName()));
				
				if (fieldType.isArray()) {
					mName.append("List");
				}
				
				
				Class<?>[] paramTypes = fieldType.isPrimitive() ? new Class<?>[] {fieldType} : new Class<?>[0];
				
				Method getValueMethod = Property.class.getDeclaredMethod(mName.toString(), paramTypes);
				
				Object[] params = fieldType.isPrimitive() ? new Object[] {def} : new Object[0];
				Object value = getValueMethod.invoke(prop, params);
				
				field.set(null, value);
			} catch (ReflectiveOperationException e) {
				SevenCommons.LOGGER.warning(String.format("Exception occured while trying to inject config into class %s!", clazz.getSimpleName()));
				e.printStackTrace();
			} catch (Throwable t) {
				Throwables.propagate(t);
			}
		}
		
		if (save && config.hasChanged()) {
			config.save();
		}
	}
	
}
