package com.github.claudecodegui.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathUtilsTempDirectoryTest {

    @Test
    public void temporaryRootAndChildrenAreDetected() {
        List<String> tempPaths = PathUtils.getTempPaths();
        assertFalse(tempPaths.isEmpty());

        String tempRoot = tempPaths.get(0);
        assertTrue(PathUtils.isTempDirectory(tempRoot));
        assertTrue(PathUtils.isTempDirectory(tempRoot + "/"));
        assertTrue(PathUtils.isTempDirectory(tempRoot + "/project"));
    }

    @Test
    public void siblingWithTemporaryPrefixIsNotDetected() {
        List<String> tempPaths = PathUtils.getTempPaths();
        assertFalse(tempPaths.isEmpty());

        String sibling = tempPaths.get(0) + "foo";
        assertFalse(PathUtils.isTempDirectory(sibling));
        assertFalse(PathUtils.isTempDirectory(sibling + "/project"));
    }
}
