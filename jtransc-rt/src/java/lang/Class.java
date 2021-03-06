/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscVisible;
import com.jtransc.ds.FastStringMap;
import com.jtransc.io.JTranscConsole;
import j.ClassInfo;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.jtransc.JTranscCoreReflection;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings({"unchecked", "WeakerAccess", "unused", "TryWithIdenticalCatches", "SuspiciousToArrayCall"})
public final class Class<T> implements java.io.Serializable, Type, GenericDeclaration, AnnotatedElement {
	@JTranscKeep
	private String name;

	@JTranscVisible
	public ClassInfo info;
	public int id;

	public int[] related;

	private boolean primitive = false;

	@SuppressWarnings("unused")
	private int modifiers;

	private static final int ANNOTATION = 0x00002000;
	private static final int ENUM = 0x00004000;
	private static final int SYNTHETIC = 0x00001000;

	// Returns the Class representing the superclass of the entity (class, interface, primitive type or void) represented by this Class. If this Class represents either the Object class, an interface, a primitive type, or void, then null is returned. If this object represents an array class then the Class object representing the Object class is returned.

	// Returns an array of Field objects reflecting all the fields declared by the class or interface represented by this Class object. This includes public, protected, default (package) access, and private fields, but excludes inherited fields. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface declares no fields, or if this Class object represents a primitive type, an array class, or void.
	// Returns an array of Field objects reflecting all the fields declared by the class or interface represented by this Class object. This includes public, protected, default (package) access, and private fields, but excludes inherited fields. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface declares no fields, or if this Class object represents a primitive type, an array class, or void.
	public Field[] getDeclaredFields() throws SecurityException {
		return JTranscCoreReflection.getDeclaredFields(this);
	}

	public Method[] getDeclaredMethods() throws SecurityException {
		return JTranscCoreReflection.getDeclaredMethods(this);
	}

	public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
		Constructor<?>[] constructors = _getDeclaredConstructors();
		return (constructors != null) ? constructors : new Constructor[0];
	}

	private Constructor<?>[] _getDeclaredConstructors() throws SecurityException {
		return JTranscCoreReflection.getDeclaredConstructors(this);
	}

	public Annotation[] getDeclaredAnnotations() {
		Annotation[] out = JTranscCoreReflection.getDeclaredAnnotations(this);
		return (out != null) ? out : new Annotation[0];
	}

	public Class<? super T> getSuperclass() {
		return (Class<? super T>) forName0(getSuperclassName());
	}

	private String getSuperclassName() {
		return JTranscCoreReflection.getClassNameById(JTranscCoreReflection.getSuperclassId(JTranscCoreReflection.getClassId(this)));
	}

	public Class<?>[] getInterfaces() {
		String[] names = getInterfaceNames();
		Class<?>[] out = new Class<?>[names.length];
		for (int n = 0; n < out.length; n++) out[n] = forName0(names[n]);
		return out;
	}

	private String[] getInterfaceNames() {
		int[] ids = JTranscCoreReflection.getInterfaceIds(this.id);
		String[] out = new String[ids.length];
		for (int n = 0; n < ids.length; n++) out[n] = JTranscCoreReflection.getClassNameById(ids[n]);
		return out;
	}

	public int getModifiers() {
		// Remove ACC_SUPER?
		return modifiers & ~0x20;
	}

	public T newInstance() throws InstantiationException, IllegalAccessException {
		try {
			Constructor<T> constructor = getDeclaredConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException e) {
			throw new InstantiationException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new InstantiationException(e.getMessage());
		}
	}

	native public Class<?>[] getDeclaredClasses() throws SecurityException;

	native public Method getEnclosingMethod();

	native public Constructor<?> getEnclosingConstructor() throws SecurityException;

	native public java.net.URL getResource(String name);

	public boolean isInstance(Object obj) {
		//return !this.isPrimitive() && (obj != null) && this.isAssignableFrom(obj.getClass());
		return (obj != null) && this.isAssignableFrom(obj.getClass());
	}

	native public InputStream getResourceAsStream(String name);

	public ClassLoader getClassLoader() {
		return _ClassInternalUtils.getSystemClassLoader();
	}

	native public TypeVariable<Class<T>>[] getTypeParameters();

	native public Type getGenericSuperclass();

	native public Package getPackage();

	native public Type[] getGenericInterfaces();

	public Class<?> getComponentType() {
		if (isArray()) {
			try {
				Class<?> out = forName(getName().substring(1));
				return out;
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	public Object[] getSigners() {
		return null;
	}

	native public Class<?> getDeclaringClass() throws SecurityException;

	native public Class<?> getEnclosingClass() throws SecurityException;

	native public Class<?>[] getClasses();

	//native public java.security.ProtectionDomain getProtectionDomain();

	private Class() {
	}

	Class(String name) throws ClassNotFoundException {
		this.name = name;
		this.primitive = false;
		if (!_check()) throw new ClassNotFoundException("Class constructor: Can't find class '" + name + "'");
	}

	Class(String name, boolean primitive) {
		this.name = name;
		this.primitive = primitive;
		this.id = -1;
	}

	private boolean _check() {
		this.info = JTranscCoreReflection.getClassInfoWithName(this.name);
		if (info != null) {
			this.id = info.id;
			this.related = info.related;
			this.modifiers = JTranscCoreReflection.getModifiersWithId(this.id);
		} else {
			this.id = -1;
			this.related = new int[0];
			this.modifiers = 0;
		}
		return isArray() || this.id >= 0;
	}

	public String getName() {
		return this.name;
	}

	static Class<?> getPrimitiveClass(String name) {
		return new Class<>(name, true);
	}

	public String toString() {
		return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + name;
	}

	native public String toGenericString();

	private static FastStringMap<Class<?>> _classCache;

	static public Class<?> forName0(String className) {
		if (className == null) return null;
		try {
			return forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Class<?> forName(String className) throws ClassNotFoundException {
		//Objects.requireNonNull(className, "className");
		if (className == null) return null;
		if (className.length() == 1) {
			switch (className.charAt(0)) {
				case 'V':
					return Void.TYPE;
				case 'Z':
					return Boolean.TYPE;
				case 'B':
					return Byte.TYPE;
				case 'C':
					return Character.TYPE;
				case 'S':
					return Short.TYPE;
				case 'D':
					return Double.TYPE;
				case 'F':
					return Float.TYPE;
				case 'I':
					return Integer.TYPE;
				case 'J':
					return Long.TYPE;
			}
		}
		if (className.startsWith("L") && className.endsWith(";")) {
			return forName(className.substring(1, className.length() - 1).replace('/', '.'));
		}
		if (_classCache == null) _classCache = new FastStringMap<>();
		if (!_classCache.has(className)) {
			_classCache.set(className, new Class<>(className));
		}
		Class<?> result = _classCache.get(className);
		if (result == null) {
			JTranscConsole.error("Couldn't find class " + className);
		}
		return result;
	}

	public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
		return forName(name);
	}

	public boolean isAssignableFrom(Class<?> cls) {
		if (cls != null) {
			int tid = this.id;
			if (cls.related != null) {
				for (int cid : cls.related) if (cid == tid) return true;
			}
		}
		return false;
	}

	public boolean isInterface() {
		return Modifier.isInterface(getModifiers());
	}

	public boolean isArray() {
		return this.name.startsWith("[");
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public boolean isAnnotation() {
		return (this.getModifiers() & ANNOTATION) != 0;
	}

	public boolean isSynthetic() {
		return (this.getModifiers() & SYNTHETIC) != 0;
	}

	public boolean isEnum() {
		return (this.getModifiers() & ENUM) != 0;
	}

	public String getSimpleName() {
		String out = "";
		char separator = (this.name.indexOf('$') > 0) ? '$' : '.';
		out += this.name.substring(this.name.lastIndexOf(separator) + 1);
		if (isArray()) out += "[]";
		return out;
	}

	native public String getTypeName();

	//public String getCanonicalName() {
	//return this.name.replace('.', '/');
	//}
	public String getCanonicalName() {
		return this.name.replace('$', '.');
	}

	public boolean isAnonymousClass() {
		return "".equals(getSimpleName());
	}

	public boolean isLocalClass() {
		return isLocalOrAnonymousClass() && !isAnonymousClass();
	}

	private boolean isLocalOrAnonymousClass() {
		return getEnclosingMethodInfo() != null;
	}

	public boolean isMemberClass() {
		return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
	}

	native private Object getEnclosingMethodInfo();
	//private EnclosingMethodInfo getEnclosingMethodInfo() {
	//	Object[] enclosingInfo = getEnclosingMethod0();
	//	if (enclosingInfo == null)
	//		return null;
	//	else {
	//		return new EnclosingMethodInfo(enclosingInfo);
	//	}
	//}

	private String getSimpleBinaryName() {
		Class<?> enclosingClass = getEnclosingClass();
		if (enclosingClass == null) // top level class
			return null;
		// Otherwise, strip the enclosing class' name
		try {
			return getName().substring(enclosingClass.getName().length());
		} catch (IndexOutOfBoundsException ex) {
			throw new InternalError("Malformed class name", ex);
		}
	}

	public boolean desiredAssertionStatus() {
		return JTranscSystem.isDebug();
	}

	public T[] getEnumConstants() {
		T[] values = getEnumConstantsShared();
		if (values == null) {
			System.out.println("Class " + this + " is not an enum (" + isEnum() + ")!");

			try {
				final Method valuesMethod = getMethod("values");
				System.out.println("values method:" + valuesMethod);
			} catch (NoSuchMethodException e) {
				throw new Error(e);
			}
		}
		return (values != null) ? values.clone() : null;
	}

	T[] getEnumConstantsShared() {
		if (enumConstants == null) {
			if (!isEnum()) return null;
			try {
				final Method valuesMethod = getMethod("values");
				enumConstants = (T[]) valuesMethod.invoke(null);
			} catch (Exception ex) {
				return null;
			}
		}
		return enumConstants;
	}

	private volatile transient T[] enumConstants = null;

	public T cast(Object obj) {
		if (obj != null && !isInstance(obj)) throw new ClassCastException(cannotCastMsg(obj));
		return (T) obj;
	}

	private String cannotCastMsg(Object obj) {
		return "Cannot cast " + obj.getClass().getName() + " to " + getName();
	}

	public <U> Class<? extends U> asSubclass(Class<U> clazz) {
		if (!clazz.isAssignableFrom(this)) throw new ClassCastException(this.toString());
		return (Class<? extends U>) this;
	}

	private FastStringMap<Field> _fieldsByName;
	private FastStringMap<Field> _declaredFieldsByName;

	private Field[] _allFields = null;
	private Field[] _accessibleFields = null;

	private Field[] getAllFields() throws SecurityException {
		if (_allFields == null) {
			ArrayList<Field> allFields = new ArrayList<>();
			Collections.addAll(allFields, getDeclaredFields());
			if (getSuperclass() != null) {
				Collections.addAll(allFields, getSuperclass().getFields());
			}
			_allFields = allFields.toArray(new Field[0]);
		}
		return _allFields;
	}

	// Returns an array containing Field objects reflecting all the accessible public fields of the class or interface represented by this Class object. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface has no accessible public fields, or if it represents an array class, a primitive type, or void.
	public Field[] getFields() throws SecurityException {
		if (_accessibleFields == null) {
			ArrayList<Field> accessibleFields = new ArrayList<>();
			for (Field field : getAllFields()) {
				if (field.isAccessible()) accessibleFields.add(field);
			}
			_accessibleFields = accessibleFields.toArray(new Field[0]);
		}
		return _accessibleFields;
	}

	public Field getField(String name) throws NoSuchFieldException, SecurityException {
		if (_fieldsByName == null) {
			_fieldsByName = new FastStringMap<>();
			for (Field f : this.getAllFields()) _fieldsByName.set(f.getName(), f);
		}
		Field field = _fieldsByName.get(name);
		if (field == null) throw new NoSuchFieldException(name);
		return field;
	}

	public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
		if (_declaredFieldsByName == null) {
			_declaredFieldsByName = new FastStringMap<>();
			for (Field f : this.getDeclaredFields()) _declaredFieldsByName.set(f.getName(), f);
		}
		Field field = _declaredFieldsByName.get(name);
		if (field == null) throw new NoSuchFieldException(name);
		return field;
	}

	public Method[] getMethods() throws SecurityException {
		return this.getDeclaredMethods(); // @TODO: Filter just public! + ancestors
	}

	public Annotation[] getAnnotations() {
		return this.getDeclaredAnnotations(); // @TODO: Filter just public!
	}

	public Constructor<?>[] getConstructors() throws SecurityException {
		return this.getDeclaredConstructors(); // @TODO: Filter just public! + ancestors?
	}

	public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		return getDeclaredMethod(name, parameterTypes);
	}

	public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		return getDeclaredConstructor(parameterTypes);
	}

	public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		for (Method m : getDeclaredMethods()) {
			if (Objects.equals(m.getName(), name) && Arrays.equals(m.getParameterTypes(), parameterTypes)) {
				return m;
			}
		}
		throw new NoSuchMethodException(name);
	}

	public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		//JTranscConsole.log("BEGIN getDeclaredConstructor");
		Class<?>[] parameterTypes2 = (parameterTypes != null) ? parameterTypes : new Class[0];
		for (Constructor c : getDeclaredConstructors()) {
			//JTranscConsole.log("A DECLARED CONSTRUCTOR: " + (c != null));
			if (Arrays.equals(c.getParameterTypes(), parameterTypes2)) {
				//JTranscConsole.log("END getDeclaredConstructor");
				return c;
			}
		}
		throw new NoSuchMethodException("Can't find constructor of class " + this.getName() + " with parameters " + Arrays.asList(parameterTypes));
	}


	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getDeclaredAnnotation(annotationClass) != null;
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		for (Annotation a : getAnnotations()) {
			if (a.getClass() == annotationClass) return (A) a;
		}
		return null;
	}

	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
		ArrayList<A> out = new ArrayList<>();
		for (Annotation a : getAnnotations()) {
			if (a.getClass() == annotationClass) out.add((A) a);
		}
		return (A[]) out.toArray(new Annotation[0]);
	}

	public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
		for (Annotation a : getDeclaredAnnotations()) {
			if (a.getClass() == annotationClass) return (A) a;
		}
		return null;
	}

	public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
		ArrayList<A> out = new ArrayList<>();
		for (Annotation a : getDeclaredAnnotations()) {
			if (a.getClass() == annotationClass) out.add((A) a);
		}
		return (A[]) out.toArray(new Annotation[0]);
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

