/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.akarazhev.cryptoscout.util;

import com.github.akarazhev.cryptoscout.Command;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for validating commands.
 */
public final class CommandValidator {

    private CommandValidator() {
        // Utility class, no instantiation
    }

    /**
     * Validates a command and returns it wrapped in an Optional.
     * If the command is invalid, returns an empty Optional.
     *
     * @param command The command to validate
     * @param <T> The type of the command payload
     * @return An Optional containing the command if valid, or empty if invalid
     */
    public static <T> Optional<Command<T>> validate(Command<T> command) {
        if (command == null) {
            return Optional.empty();
        }
        
        if (command.action() == null) {
            return Optional.empty();
        }
        
        return Optional.of(command);
    }

    /**
     * Validates that the command is not null and has a valid action.
     *
     * @param command The command to validate
     * @param <T> The type of the command payload
     * @throws IllegalArgumentException if the command is invalid
     */
    public static <T> void validateOrThrow(Command<T> command) {
        Objects.requireNonNull(command, "Command cannot be null");
        Objects.requireNonNull(command.action(), "Command action cannot be null");
    }
}
