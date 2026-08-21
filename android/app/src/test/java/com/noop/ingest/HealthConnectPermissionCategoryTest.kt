package com.noop.ingest

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import com.noop.ingest.HealthConnectImporter.ImportCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure coverage for the category consent contract in issue #645. */
class HealthConnectPermissionCategoryTest {
    private val recovery = setOf(ImportCategory.RECOVERY)
    private val activity = setOf(ImportCategory.ACTIVITY)
    private val body = setOf(ImportCategory.BODY_COMPOSITION)

    @Test
    fun categoriesPartitionEverySupportedPermissionExactlyOnce() {
        val perCategory = ImportCategory.entries.map { HealthConnectImporter.permissionsFor(setOf(it)) }

        assertEquals(perCategory.sumOf { it.size }, perCategory.flatten().toSet().size)
        assertEquals(HealthConnectImporter.PERMISSIONS, perCategory.flatten().toSet())
    }

    @Test
    fun recoveryDoesNotRequestActivityOrBodyComposition() {
        val permissions = HealthConnectImporter.permissionsFor(recovery)

        assertTrue(HealthPermission.getReadPermission(HeartRateRecord::class) in permissions)
        assertFalse(HealthPermission.getReadPermission(StepsRecord::class) in permissions)
        assertFalse(HealthPermission.getReadPermission(WeightRecord::class) in permissions)
    }

    @Test
    fun newInstallsDefaultNarrowWhileLegacyInstallsKeepExistingImports() {
        assertEquals(
            recovery,
            HealthConnectImporter.categoriesFromStoredKeys(null, hadLegacyPermissionSignature = false),
        )
        assertEquals(
            HealthConnectImporter.ALL_CATEGORIES,
            HealthConnectImporter.categoriesFromStoredKeys(null, hadLegacyPermissionSignature = true),
        )
    }

    @Test
    fun storedSelectionWinsOverLegacyFallback() {
        assertEquals(
            activity + body,
            HealthConnectImporter.categoriesFromStoredKeys(
                setOf(ImportCategory.ACTIVITY.storageKey, ImportCategory.BODY_COMPOSITION.storageKey),
                hadLegacyPermissionSignature = true,
            ),
        )
    }

    @Test
    fun narrowingNeverRepromptsAndExpandingRequestsOnlyNewCategory() {
        val recoveryPermissions = HealthConnectImporter.permissionsFor(recovery)

        assertTrue(HealthConnectImporter.unaskedPermissions(recoveryPermissions, recovery).isEmpty())
        assertEquals(
            HealthConnectImporter.permissionsFor(body),
            HealthConnectImporter.unaskedPermissions(recoveryPermissions, recovery + body),
        )
    }

    @Test
    fun partialGrantReadsOnlyGrantedTypesInsideSelectedCategories() {
        val heartRate = HealthPermission.getReadPermission(HeartRateRecord::class)
        val steps = HealthPermission.getReadPermission(StepsRecord::class)
        val readable = HealthConnectImporter.readableRecordTypes(
            categories = recovery + activity,
            grantedPermissions = setOf(heartRate, steps),
        )

        assertEquals(setOf(HeartRateRecord::class, StepsRecord::class), readable)
        assertFalse(WeightRecord::class in readable)
    }

    @Test
    fun grantFromDeselectedCategoryIsNotRead() {
        val readable = HealthConnectImporter.readableRecordTypes(
            categories = recovery,
            grantedPermissions = setOf(
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(StepsRecord::class),
            ),
        )

        assertEquals(setOf(HeartRateRecord::class), readable)
    }
}
