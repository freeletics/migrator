package com.freeletics.migrator

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import java.lang.IllegalStateException

private val migratorName = Migrator::class.simpleName
private val migrationName = Migration::class.simpleName
private val versionStoreName = MigratorVersionStore::class.simpleName

class MigratorSpec : BehaviorSpec({

    Given("A $migratorName") {

        When("list of migrations is empty") {
            Then("IllegalArgumentException is thrown") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Migrator(migrations = emptyList(), migratorVersionStore = InMemoryMigratorVersionStore())
                }

                exception.message shouldBe "List of Migrations is empty"
            }
        }


        When("running $migratorName") {

            And("a $migrationName with negative version number") {
                val m1 = SimpleMigration(1)
                val m2 = SimpleMigration(-1)
                val versionStore = InMemoryMigratorVersionStore()
                val migrations = listOf(m1, m2)
                val migrator = Migrator(
                    migrations = migrations,
                    migratorVersionStore = versionStore
                )

                Then("IllegalArgumentException is thrown") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        migrator.runMigrationsIfNeeded()
                    }
                    exception.message shouldBe "Migration version must be > 1 " +
                        "but found a migration with version = -1"
                }
            }

            And("multiple $migrationName with same version number") {
                val m1 = SimpleMigration(1)
                val m2 = SimpleMigration(2)
                val m3 = SimpleMigration(2)
                val m4 = SimpleMigration(4)
                val versionStore = InMemoryMigratorVersionStore()
                val migrations = listOf(m1, m2, m3, m4)
                val migrator = Migrator(
                    migrations = migrations,
                    migratorVersionStore = versionStore
                )

                Then("IllegalArgumentException is thrown") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        migrator.runMigrationsIfNeeded()
                    }
                    exception.message shouldBe "At least two migrations with version = 2 found. " +
                        "Version must be unique and positive integer."
                }
            }

            And("last migration ran is version = 2 but no migration with version 2 in list") {
                val m1 = SimpleMigration(1)
                val m2 = SimpleMigration(3)
                val versionStore = InMemoryMigratorVersionStore(migrationUntilVersion = 2)
                val migrations = listOf(m1, m2)
                val migrator = Migrator(
                    migrations = migrations,
                    migratorVersionStore = versionStore
                )

                Then("IllegalStateException is thrown") {
                    val exception = shouldThrow<IllegalStateException> {
                        migrator.runMigrationsIfNeeded()
                    }
                    exception.message shouldBe "Last time we migrated to version = 2 but couldn't find " +
                        "a migration with that version in the migration list"
                }
            }

            And("multiple migrations in unsorted order") {
                val counter = Counter()
                val m1 = MigrationWithCounter(counter, 1)
                val m2 = MigrationWithCounter(counter, 2)
                val m3 = MigrationWithCounter(counter, 3)
                val versionStore = InMemoryMigratorVersionStore()
                val migrations = listOf(m3, m1, m2)
                val migrator = Migrator(
                    migrations = migrations,
                    migratorVersionStore = versionStore
                )

                migrator.runMigrationsIfNeeded()

                Then("$migrationName 1 ran first") {
                    m1.migrationCompleted shouldBe MigrationCompleted.Completed(1)
                }

                Then("$migrationName 2 ran second") {
                    m2.migrationCompleted shouldBe MigrationCompleted.Completed(2)
                }

                Then("$migrationName 3 ran third") {
                    m3.migrationCompleted shouldBe MigrationCompleted.Completed(3)
                }

                Then("$versionStoreName is at version 3") {
                    versionStore.getMigratedUntilVersion() shouldBe 3
                }
            }

            And("current App version is 4 ") {
                And("$versionStoreName has latest version runMigrationsIfNeeded 2") {

                    val counter = Counter()
                    val m1 = MigrationWithCounter(counter, 1)
                    val m2 = MigrationWithCounter(counter, 2)
                    val m3 = MigrationWithCounter(counter, 3)
                    val m4 = MigrationWithCounter(counter, 4)
                    val versionStore = InMemoryMigratorVersionStore(2)
                    val migrations = listOf(m1, m2, m3, m4)
                    val migrator = Migrator(
                        migrations = migrations,
                        migratorVersionStore = versionStore
                    )

                    migrator.runMigrationsIfNeeded()

                    Then("Migration 1 didn't runMigrationsIfNeeded") {
                        m1.migrationCompleted shouldBe MigrationCompleted.Never
                    }

                    Then("Migration 2 didn't runMigrationsIfNeeded") {
                        m2.migrationCompleted shouldBe MigrationCompleted.Never
                    }

                    Then("Migration 3 runMigrationsIfNeeded") {
                        m3.migrationCompleted shouldBe MigrationCompleted.Completed(1)
                    }
                    Then("Migration 4 runMigrationsIfNeeded") {
                        m4.migrationCompleted shouldBe MigrationCompleted.Completed(2)
                    }

                    Then("$versionStoreName is at last migration value = 4") {
                        versionStore.getMigratedUntilVersion() shouldBe 4
                    }
                }

                And("Migration with version = 4 was the last migration we ran last time") {
                    val counter = Counter()
                    val m1 = MigrationWithCounter(counter, 1)
                    val m2 = MigrationWithCounter(counter, 2)
                    val m3 = MigrationWithCounter(counter, 3)
                    val m4 = MigrationWithCounter(counter, 4)
                    val versionStore = InMemoryMigratorVersionStore(4)
                    val migrations = listOf(m1, m2, m3, m4)
                    val migrator = Migrator(
                        migrations = migrations,
                        migratorVersionStore = versionStore
                    )

                    migrator.runMigrationsIfNeeded()

                    Then("Migration 1 didn't runMigrationsIfNeeded") {
                        m1.migrationCompleted shouldBe MigrationCompleted.Never
                    }

                    Then("Migration 2 didn't runMigrationsIfNeeded") {
                        m2.migrationCompleted shouldBe MigrationCompleted.Never
                    }

                    Then("Migration 3 didn't runMigrationsIfNeeded") {
                        m3.migrationCompleted shouldBe MigrationCompleted.Never
                    }
                    Then("Migration 4 didn't runMigrationsIfNeeded") {
                        m4.migrationCompleted shouldBe MigrationCompleted.Never
                    }

                    Then("$versionStoreName is still at last migration value = 4") {
                        versionStore.getMigratedUntilVersion() shouldBe 4
                    }
                }
            }
        }
    }
})