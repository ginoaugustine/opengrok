/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2010, 2022, Oracle and/or its affiliates. All rights reserved.
 */
package org.opengrok.indexer.history;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengrok.indexer.condition.EnabledForRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengrok.indexer.condition.RepositoryInstalled.Type.SCCS;

/**
 * Test the {@link SCCSget} class.
 * @author Trond Norbye
 */
@EnabledForRepository(SCCS)
class SCCSgetTest {

    private File sccs_file;
    private File sccs_dir;

    @BeforeEach
    public void setUp() throws IOException {
        try {
            sccs_dir = File.createTempFile("s.test", "sccs");
            assertTrue(sccs_dir.delete());
            assertTrue(sccs_dir.mkdirs(), "Failed to set up the test-directory");
            sccs_file = new File(sccs_dir, "s.note.txt");
            try (InputStream in = getClass().getResourceAsStream("/history/s.note.txt");
                 FileOutputStream out = new FileOutputStream(sccs_file)) {

                byte[] buffer = new byte[8192];
                int nr;

                while ((nr = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, nr);
                }
                out.flush();
            }
        } catch (IOException ex) {
            if (sccs_file != null) {
                sccs_file.delete();
                sccs_dir.delete();
            }
            throw ex;
        }
    }

    @AfterEach
    public void tearDown() {
        if (sccs_file != null) {
            sccs_file.delete();
        }

        if (sccs_dir != null) {
            sccs_dir.delete();
        }
    }

    private String readInput(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[32 * 1024];
        int len;

        while ((len = in.read(buffer)) != -1) {
            if (len > 0) {
                out.write(buffer, 0, len);
            }
        }

        return out.toString();
    }

    /**
     * Test {@link SCCSget#getRevision(String, File, String)}.
     */
    @Test
    void getRevision() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/history/sccs-revisions.zip");
        assertNotNull(inputStream);
        ZipInputStream zstream = new ZipInputStream(inputStream);
        ZipEntry entry;

        while ((entry = zstream.getNextEntry()) != null) {
            String expected = readInput(zstream);
            InputStream sccs = SCCSget.getRevision("sccs", sccs_file, entry.getName());
            String got = readInput(sccs);
            sccs.close();
            zstream.closeEntry();
            assertEquals(expected, got);
        }
        zstream.close();
    }
}
