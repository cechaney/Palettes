package com.spm.palettes;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.Arrays;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    protected void setUp() {

        try {
            super.setUp();
            this.createApplication();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void permsCheck() {

        Application app = getApplication();
        String packageName = app.getPackageName();

        try {

            String[] perms = app.getPackageManager().getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS).requestedPermissions;

            List<String> permArray = Arrays.asList(perms);

            assertNotNull(permArray.indexOf(Manifest.permission.CAMERA));
            assertNotNull(permArray.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE));

        } catch (PackageManager.NameNotFoundException nnfe) {
            fail();
        }
    }
}