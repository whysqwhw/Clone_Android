/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint;

import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/** A reporter which emits lint warnings as plain text strings */
class TextReporter extends Reporter {
    private final Writer mWriter;

    TextReporter(Main client, Writer writer) {
        super(client, null);
        mWriter = writer;
    }

    @Override
    void write(int errorCount, int warningCount, List<Warning> issues) throws IOException {
        boolean abbreviate = mClient.getDriver().isAbbreviating();

        StringBuilder output = new StringBuilder(issues.size() * 200);
        if (issues.size() == 0) {
            mWriter.write("No issues found.");
            mWriter.write('\n');
        } else {
            for (Warning warning : issues) {
                int startLength = output.length();

                if (warning.path != null) {
                    output.append(warning.path);
                    output.append(':');

                    if (warning.line >= 0) {
                        output.append(Integer.toString(warning.line + 1));
                        output.append(':');
                    }
                    if (startLength < output.length()) {
                        output.append(' ');
                    }
                }

                output.append(warning.severity.getDescription());
                output.append(':');
                output.append(' ');

                output.append(warning.message);
                if (warning.issue != null) {
                    output.append(' ').append('[');
                    output.append(warning.issue.getId());
                    output.append(']');
                }

                output.append('\n');

                if (warning.errorLine != null && warning.errorLine.length() > 0) {
                    output.append(warning.errorLine);
                }

                if (warning.location != null && warning.location.getSecondary() != null) {
                    Location location = warning.location.getSecondary();
                    while (location != null) {
                        if (location.getMessage() != null
                                && location.getMessage().length() > 0) {
                            output.append("    "); //$NON-NLS-1$
                            String path = mClient.getDisplayPath(warning.project,
                                    location.getFile());
                            output.append(path);

                            Position start = location.getStart();
                            if (start != null) {
                                int line = start.getLine();
                                if (line >= 0) {
                                    output.append(':');
                                    output.append(Integer.toString(line + 1));
                                }
                            }

                            if (location.getMessage() != null
                                    && location.getMessage().length() > 0) {
                                output.append(':');
                                output.append(' ');
                                output.append(location.getMessage());
                            }

                            output.append('\n');
                        }

                        location = location.getSecondary();
                    }

                    if (!abbreviate) {
                        location = warning.location.getSecondary();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Also affects: ");
                        int begin = sb.length();
                        while (location != null) {
                            if (location.getMessage() == null
                                    || location.getMessage().length() > 0) {
                                if (sb.length() > begin) {
                                    sb.append(", ");
                                }

                                String path = mClient.getDisplayPath(warning.project,
                                        location.getFile());
                                sb.append(path);

                                Position start = location.getStart();
                                if (start != null) {
                                    int line = start.getLine();
                                    if (line >= 0) {
                                        sb.append(':');
                                        sb.append(Integer.toString(line + 1));
                                    }
                                }
                            }

                            location = location.getSecondary();
                        }
                        String wrapped = Main.wrap(sb.toString(), Main.MAX_LINE_WIDTH, "     "); //$NON-NLS-1$
                        output.append(wrapped);
                    }
                }
            }

            mWriter.write(output.toString());

            mWriter.write(String.format("%1$d errors, %2$d warnings",
                    errorCount, warningCount));
            mWriter.write('\n');
            mWriter.flush();
        }
    }
}