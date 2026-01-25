package org.example.app.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonUtils {

  private static final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

  public static void printObject(Object object) {
    visited.clear();
    printObjectFields(object, "");
  }

  private static void printObjectFields(Object obj, String indent) {
    if (obj == null) {
      System.out.println(indent + "null");
      return;
    }

    // Avoid infinite recursion on circular references
    if (visited.contains(obj)) {
      System.out.println(indent + "[Circular Reference: " + obj.getClass().getSimpleName() + "]");
      return;
    }
    visited.add(obj);

    Class<?> clazz = obj.getClass();

    // Primitive-like outputs
    if (isSimpleType(clazz)) {
      System.out.println(indent + obj);
      return;
    }

    // Enum
    if (clazz.isEnum()) {
      System.out.println(indent + obj.toString());
      return;
    }

    // Arrays
    if (clazz.isArray()) {
      System.out.println(indent + "[");
      int length = Array.getLength(obj);
      for (int i = 0; i < length; i++) {
        printObjectFields(Array.get(obj, i), indent + "  ");
      }
      System.out.println(indent + "]");
      return;
    }

    // Collections
    if (obj instanceof Collection<?>) {
      System.out.println(indent + "[");
      for (Object element : (Collection<?>) obj) {
        printObjectFields(element, indent + "  ");
      }
      System.out.println(indent + "]");
      return;
    }

    // Map
    if (obj instanceof Map<?, ?> map) {
      System.out.println(indent + "{");
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        System.out.print(indent + "  " + entry.getKey() + ": ");
        printObjectFields(entry.getValue(), indent + "  ");
      }
      System.out.println(indent + "}");
      return;
    }

    // Complex Object (POJO)
    System.out.println(indent + clazz.getSimpleName() + " {");
    for (Field field : getAllFields(clazz)) {
      field.setAccessible(true);
      try {
        Object value = field.get(obj);
        System.out.print(indent + "  " + field.getName() + ": ");
        printObjectFields(value, indent + "  ");
      } catch (Exception e) {
        System.out.println(indent + "  " + field.getName() + ": [inaccessible]");
      }
    }
    System.out.println(indent + "}");
  }

  private static boolean isSimpleType(Class<?> clazz) {
    return clazz.isPrimitive()
        || Number.class.isAssignableFrom(clazz)
        || Boolean.class.equals(clazz)
        || Character.class.equals(clazz)
        || String.class.equals(clazz)
        || clazz.getPackageName().startsWith("java.time")   // LocalDate, LocalDateTime, etc.
        || clazz.getPackageName().startsWith("java.math"); // BigDecimal, BigInteger
  }

  private static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    while (clazz != null && !clazz.equals(Object.class)) {
      fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  public static LocalDateTime parseFlexibleDateTime(String value) {
    // check if it endes with a single letter
    if (value.matches(".*[A-Za-z]$")) {
      //Remove the letter
      value = value.substring(0, value.length() - 1).trim();
    }
    DateTimeFormatter noZone = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
    return LocalDateTime.parse(value, noZone);
  }
}
