/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Minecrell (https://github.com/Minecrell)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package vintagemc.loom.forge;

import org.cadixdev.at.AccessChange;
import org.cadixdev.at.AccessTransform;
import org.cadixdev.at.AccessTransformSet;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Based off org.cadixdev.at.io.fml.FmlWriter licensed as MIT
 */
public final class FmlLegacyATWriter {
    private static final char WILDCARD = '*';
    private static final String METHOD_WILDCARD = WILDCARD + "()";

    private final BufferedWriter writer;

    public FmlLegacyATWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public void write(AccessTransformSet set) {
        set.getClasses().forEach(throwing((originalClassName, classSet) -> {
            final String className = originalClassName;

            writeClass(className, classSet.get());

            writeField(className, null, classSet.allFields());
            classSet.getFields().forEach(throwing((name, transform) -> writeField(className, name, transform)));

            writeMethod(className, null, classSet.allMethods());
            classSet.getMethods().forEach(throwing((name, transform) -> writeMethod(className, name, transform)));
        }));
    }

    private static String getAccessModifier(AccessChange change) {
        return switch (change) {
            case PUBLIC -> "public";
            case PROTECTED -> "protected";
            case PACKAGE_PRIVATE -> "default";
            case PRIVATE -> "private";
            default -> throw new AssertionError(change);
        };
    }

    private void writeAccessTransform(String className, AccessTransform transform) throws IOException {
        if (transform.getAccess() != AccessChange.NONE) {
            this.writer.write(getAccessModifier(transform.getAccess()));
        }

        switch (transform.getFinal()) {
            case NONE:
                break;
            case REMOVE:
                this.writer.write("-f");
                break;
            case ADD:
                this.writer.write("+f");
                break;
            default:
                throw new AssertionError(transform.getFinal());
        }

        this.writer.write(' ');
        this.writer.write(className);
    }


    private void writeClass(String className, AccessTransform transform) throws IOException {
        if (transform.isEmpty()) {
            return;
        }

        writeAccessTransform(className, transform);
        this.writer.newLine();
    }

    private void writeField(String className, String name, AccessTransform transform) throws IOException {
        if (transform.isEmpty()) {
            return;
        }

        writeAccessTransform(className, transform);
        this.writer.write('.');

        if (name != null) {
            this.writer.write(name);
        } else {
            this.writer.write(WILDCARD);
        }
        this.writer.newLine();
    }

    private void writeMethod(String className, MethodSignature signature, AccessTransform transform) throws IOException {
        if (transform.isEmpty()) {
            return;
        }

        writeAccessTransform(className, transform);
        this.writer.write('.');

        if (signature != null) {
            this.writer.write(signature.getName());
            this.writer.write(signature.getDescriptor().toString());
        } else {
            this.writer.write(METHOD_WILDCARD);
        }
        this.writer.newLine();
    }


    private static <T, U> BiConsumer<T, U> throwing(FmlLegacyATWriter.ThrowingBiConsumer<T, U> consumer) {
        return consumer;
    }

    private interface ThrowingBiConsumer<T, U> extends BiConsumer<T, U> {

        void throwingAccept(T t, U u) throws IOException;

        @Override
        default void accept(T t, U u) {
            try {
                throwingAccept(t, u);
            } catch (IOException e) {
                rethrow(e);
            }
        }

        @SuppressWarnings("unchecked")
        static <E extends Exception> void rethrow(Exception e) throws E {
            throw (E) e;
        }

    }

}
