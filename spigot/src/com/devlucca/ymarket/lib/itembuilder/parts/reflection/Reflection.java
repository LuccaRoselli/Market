package com.devlucca.ymarket.lib.itembuilder.parts.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;


@SuppressWarnings("rawtypes")
public final class Reflection
{
    private static String OBC_PREFIX;
    private static String NMS_PREFIX;
    private static String VERSION;
    private static Pattern MATCH_VARIABLE;
    
    static {
        Reflection.OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
        Reflection.NMS_PREFIX = Reflection.OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
        Reflection.VERSION = Reflection.OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
        Reflection.MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");
    }
    
    public static Class<?> getNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        }
        catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
	public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Class[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        Constructor<?>[] constructors;
        for (int length = (constructors = clazz.getConstructors()).length, i = 0; i < length; ++i) {
            final Constructor<?> constructor = constructors[i];
            if (DataType.compare(DataType.getPrimitive(constructor.getParameterTypes()), primitiveTypes)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
    }
    
    public static <T> FieldAccessor<T> getField(final Class<?> target, final String name, final Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }
    
    public static <T> FieldAccessor<T> getField(final String className, final String name, final Class<T> fieldType) {
        return getField(getClass(className), name, fieldType, 0);
    }
    
    public static <T> FieldAccessor<T> getField(final Class<?> target, final Class<T> fieldType, final int index) {
        return getField(target, null, fieldType, index);
    }
    
    public static <T> FieldAccessor<T> getField(final String className, final Class<T> fieldType, final int index) {
        return getField(getClass(className), fieldType, index);
    }
    
    @SuppressWarnings("unchecked")
	private static <T> FieldAccessor<T> getField(final Class<?> target, final String name, final Class<T> fieldType, int index) {
        Field[] declaredFields;
        for (int length = (declaredFields = target.getDeclaredFields()).length, i = 0; i < length; ++i) {
            final Field field = declaredFields[i];
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);
                return new FieldAccessor<T>() {
					@Override
                    public T get(final Object target) {
                        try {
                            return (T)field.get(target);
                        }
                        catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }
                    
                    @Override
                    public void set(final Object target, final Object value) {
                        try {
                            field.set(target, value);
                        }
                        catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }
                    
                    @Override
                    public boolean hasField(final Object target) {
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }
                };
            }
        }
        if (target.getSuperclass() != null) {
            return (FieldAccessor<T>)getField(target.getSuperclass(), name, (Class<Object>)fieldType, index);
        }
        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }
    
    private static String expandVariables(final String name) {
        final StringBuffer output = new StringBuffer();
        final Matcher matcher = Reflection.MATCH_VARIABLE.matcher(name);
        while (matcher.find()) {
            final String variable = matcher.group(1);
            String replacement;
            if ("nms".equalsIgnoreCase(variable)) {
                replacement = Reflection.NMS_PREFIX;
            }
            else if ("obc".equalsIgnoreCase(variable)) {
                replacement = Reflection.OBC_PREFIX;
            }
            else {
                if (!"version".equalsIgnoreCase(variable)) {
                    throw new IllegalArgumentException("Unknown variable: " + variable);
                }
                replacement = Reflection.VERSION;
            }
            if (replacement.length() > 0 && matcher.end() < name.length() && name.charAt(matcher.end()) != '.') {
                replacement = String.valueOf(replacement) + ".";
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);
        return output.toString();
    }
    
    public static Class<?> getClass(final String lookupName) {
        return getCanonicalClass(expandVariables(lookupName));
    }
    
    private static Class<?> getCanonicalClass(final String canonicalName) {
        try {
            return Class.forName(canonicalName);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }
    
    public static Constructor<?> getConstructor(final String className, final PackageType packageType, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(className), parameterTypes);
    }
    
    public static Object instantiateObject(final Class<?> clazz, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
    }
    
    public static Object instantiateObject(final String className, final PackageType packageType, final Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return instantiateObject(packageType.getClass(className), arguments);
    }
    
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Class[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        Method[] methods;
        for (int length = (methods = clazz.getMethods()).length, i = 0; i < length; ++i) {
            final Method method = methods[i];
            if (method.getName().equals(methodName) && DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
                return method;
            }
        }
        throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
    }
    
    public static Method getMethod(final String className, final PackageType packageType, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(className), methodName, parameterTypes);
    }
    
    public static Object invokeMethod(final Object instance, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }
    
    public static Object invokeMethod(final Object instance, final Class<?> clazz, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }
    
    public static Object invokeMethod(final Object instance, final String className, final PackageType packageType, final String methodName, final Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
    }
    
    public static Field getField(final Class<?> clazz, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException {
        final Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
        field.setAccessible(true);
        return field;
    }
    
    public static Field getField(final String className, final PackageType packageType, final boolean declared, final String fieldName) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getField(packageType.getClass(className), declared, fieldName);
    }
    
    public static Object getValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getField(clazz, declared, fieldName).get(instance);
    }
    
    public static Object getValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getValue(instance, packageType.getClass(className), declared, fieldName);
    }
    
    public static Object getValue(final Object instance, final boolean declared, final String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getValue(instance, instance.getClass(), declared, fieldName);
    }
    
    public static void setValue(final Object instance, final Class<?> clazz, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        getField(clazz, declared, fieldName).set(instance, value);
    }
    
    public static void setValue(final Object instance, final String className, final PackageType packageType, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        setValue(instance, packageType.getClass(className), declared, fieldName, value);
    }
    
    public static void setValue(final Object instance, final boolean declared, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        setValue(instance, instance.getClass(), declared, fieldName, value);
    }
    
    public enum DataType
    {
        BYTE((Class<?>)Byte.TYPE, (Class<?>)Byte.class), 
        SHORT((Class<?>)Short.TYPE, (Class<?>)Short.class), 
        INTEGER((Class<?>)Integer.TYPE, (Class<?>)Integer.class), 
        LONG((Class<?>)Long.TYPE, (Class<?>)Long.class), 
        CHARACTER((Class<?>)Character.TYPE, (Class<?>)Character.class), 
        FLOAT((Class<?>)Float.TYPE, (Class<?>)Float.class), 
        DOUBLE((Class<?>)Double.TYPE, (Class<?>)Double.class), 
        BOOLEAN((Class<?>)Boolean.TYPE, (Class<?>)Boolean.class);
        
        private static final Map<Class<?>, DataType> CLASS_MAP;
        private final Class<?> primitive;
        private final Class<?> reference;
        
        static {
            CLASS_MAP = new HashMap<Class<?>, DataType>();
            DataType[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final DataType type = values[i];
                DataType.CLASS_MAP.put(type.primitive, type);
                DataType.CLASS_MAP.put(type.reference, type);
            }
        }
        
        private DataType(final Class<?> primitive, final Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }
        
        public Class<?> getPrimitive() {
            return this.primitive;
        }
        
        public Class<?> getReference() {
            return this.reference;
        }
        
        public static DataType fromClass(final Class<?> clazz) {
            return DataType.CLASS_MAP.get(clazz);
        }
        
        public static Class<?> getPrimitive(final Class<?> clazz) {
            final DataType type = fromClass(clazz);
            return (type == null) ? clazz : type.getPrimitive();
        }
        
        public static Class<?> getReference(final Class<?> clazz) {
            final DataType type = fromClass(clazz);
            return (type == null) ? clazz : type.getReference();
        }
        
        public static Class<?>[] getPrimitive(final Class<?>[] classes) {
            final int length = (classes == null) ? 0 : classes.length;
            final Class[] types = new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getPrimitive(classes[index]);
            }
            return (Class<?>[])types;
        }
        
        public static Class<?>[] getReference(final Class<?>[] classes) {
            final int length = (classes == null) ? 0 : classes.length;
            final Class[] types = new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getReference(classes[index]);
            }
            return (Class<?>[])types;
        }
        
        public static Class<?>[] getPrimitive(final Object[] objects) {
            final int length = (objects == null) ? 0 : objects.length;
            final Class[] types = new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getPrimitive(objects[index].getClass());
            }
            return (Class<?>[])types;
        }
        
        public static Class<?>[] getReference(final Object[] objects) {
            final int length = (objects == null) ? 0 : objects.length;
            final Class[] types = new Class[length];
            for (int index = 0; index < length; ++index) {
                types[index] = getReference(objects[index].getClass());
            }
            return (Class<?>[])types;
        }
        
        public static boolean compare(final Class<?>[] primary, final Class<?>[] secondary) {
            if (primary == null || secondary == null || primary.length != secondary.length) {
                return false;
            }
            for (int index = 0; index < primary.length; ++index) {
                final Class<?> primaryClass = primary[index];
                final Class<?> secondaryClass = secondary[index];
                if (!primaryClass.equals(secondaryClass) && !primaryClass.isAssignableFrom(secondaryClass)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public enum PackageType
    {
        MINECRAFT_SERVER("MINECRAFT_SERVER", 0, "net.minecraft.server." + getServerVersion()), 
        CRAFTBUKKIT("CRAFTBUKKIT", 1, "org.bukkit.craftbukkit." + getServerVersion()), 
        CRAFTBUKKIT_BLOCK("CRAFTBUKKIT_BLOCK", 2, PackageType.CRAFTBUKKIT, "block"), 
        CRAFTBUKKIT_CHUNKIO("CRAFTBUKKIT_CHUNKIO", 3, PackageType.CRAFTBUKKIT, "chunkio"), 
        CRAFTBUKKIT_COMMAND("CRAFTBUKKIT_COMMAND", 4, PackageType.CRAFTBUKKIT, "command"), 
        CRAFTBUKKIT_CONVERSATIONS("CRAFTBUKKIT_CONVERSATIONS", 5, PackageType.CRAFTBUKKIT, "conversations"), 
        CRAFTBUKKIT_ENCHANTMENS("CRAFTBUKKIT_ENCHANTMENS", 6, PackageType.CRAFTBUKKIT, "enchantments"), 
        CRAFTBUKKIT_ENTITY("CRAFTBUKKIT_ENTITY", 7, PackageType.CRAFTBUKKIT, "entity"), 
        CRAFTBUKKIT_EVENT("CRAFTBUKKIT_EVENT", 8, PackageType.CRAFTBUKKIT, "event"), 
        CRAFTBUKKIT_GENERATOR("CRAFTBUKKIT_GENERATOR", 9, PackageType.CRAFTBUKKIT, "generator"), 
        CRAFTBUKKIT_HELP("CRAFTBUKKIT_HELP", 10, PackageType.CRAFTBUKKIT, "help"), 
        CRAFTBUKKIT_INVENTORY("CRAFTBUKKIT_INVENTORY", 11, PackageType.CRAFTBUKKIT, "inventory"), 
        CRAFTBUKKIT_MAP("CRAFTBUKKIT_MAP", 12, PackageType.CRAFTBUKKIT, "map"), 
        CRAFTBUKKIT_METADATA("CRAFTBUKKIT_METADATA", 13, PackageType.CRAFTBUKKIT, "metadata"), 
        CRAFTBUKKIT_POTION("CRAFTBUKKIT_POTION", 14, PackageType.CRAFTBUKKIT, "potion"), 
        CRAFTBUKKIT_PROJECTILES("CRAFTBUKKIT_PROJECTILES", 15, PackageType.CRAFTBUKKIT, "projectiles"), 
        CRAFTBUKKIT_SCHEDULER("CRAFTBUKKIT_SCHEDULER", 16, PackageType.CRAFTBUKKIT, "scheduler"), 
        CRAFTBUKKIT_SCOREBOARD("CRAFTBUKKIT_SCOREBOARD", 17, PackageType.CRAFTBUKKIT, "scoreboard"), 
        CRAFTBUKKIT_UPDATER("CRAFTBUKKIT_UPDATER", 18, PackageType.CRAFTBUKKIT, "updater"), 
        CRAFTBUKKIT_UTIL("CRAFTBUKKIT_UTIL", 19, PackageType.CRAFTBUKKIT, "util");
        
        private final String path;
        
        private PackageType(final String s, final int n, final String path) {
            this.path = path;
        }
        
        private PackageType(final String s, final int n, final PackageType parent, final String path) {
            this(s, n, parent + "." + path);
        }
        
        public String getPath() {
            return this.path;
        }
        
        public Class<?> getClass(final String className) throws ClassNotFoundException {
            return Class.forName(this + "." + className);
        }
        
        @Override
        public String toString() {
            return this.path;
        }
        
        public static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }
    }
    
    public interface FieldAccessor<T>
    {
        T get(final Object p0);
        
        void set(final Object p0, final Object p1);
        
        boolean hasField(final Object p0);
    }
}
