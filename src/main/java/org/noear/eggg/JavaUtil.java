/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.eggg;

import java.io.File;
import java.lang.reflect.Modifier;

/**
 * Java 工具
 *
 * @author noear
 * @since 1.0
 */
public class JavaUtil {
    /**
     * Java 版本号
     *
     */
    public static final int JAVA_MAJOR_VERSION;

    /**
     * 是否为 Windows
     */
    public static final boolean IS_WINDOWS = (File.separatorChar == '\\');

    /*
     * 获取 Java 版本号
     * http://openjdk.java.net/jeps/223
     * 1.8.x  = 8
     * 11.x   = 11
     * 17.x   = 17
     */
    static {
        int majorVersion;
        try {
            String vs = System.getProperty("java.specification.version");
            if (vs.startsWith("1.")) {
                vs = vs.substring(2);
            }
            majorVersion = Integer.parseInt(vs);
        } catch (Throwable ignored) {
            majorVersion = 8;
        }
        JAVA_MAJOR_VERSION = majorVersion;
    }

    private static Class<?> recordClass;

    /**
     * 是否为 Record 类
     */
    public static boolean isRecordClass(Class<?> clazz) {
        if (JavaUtil.JAVA_MAJOR_VERSION < 17) {
            return false;
        }

        if (clazz == null) {
            return false;
        }

        // 1. Record 类是 final 的
        if (!Modifier.isFinal(clazz.getModifiers())) {
            return false;
        }

        try {
            // 2. 通过 isAssignableFrom 检测
            if (recordClass == null) {
                recordClass = Class.forName("java.lang.Record");
            }

            return recordClass.isAssignableFrom(clazz);

        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}