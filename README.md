# Migrator

Migrator is a general purpose migration library for Android.

## Dependency

Latest snapshot (directly published from master branch from Circle CI):

```
allprojects {
    repositories {
        // Your repositories.
        // ...
        // Add url to snapshot repository
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots/"
        }
    }
}

```


```
implementation 'com.freeletics.rxredux:rxredux:1.0.0-SNAPSHOT'

```

## Usage

First let's create a `Migration`. A `Migration` can do any kind of migration in your app (hence general purpose).
A migration has a `version`. Versions must be >= 1 and must be unique.
We recommend to increment the `version` by 1 with any new migration that you add to your app.

```kotlin
class V1Migration(private val context : Context) : Migration {
    override val version = 1

    override fun run(){
        // for example in version v1 we update some shared preference values
        context.getSharedPreferences(example)
            .edit()
            .putInt("SomeValue", 10)
            .apply()
    }
}
```

```kotlin
class V2Migration(private val context : Context) : Migration {
    override val version = 2

    override fun run(){
        // for example in version v2 we copy some file from a to b
        val dir = context.getFilesDir()
        val source = File(dir, "SomeFile.txt")
        val target = File(dir, "SomeNewFile.txt")

        source.copyTo(target)
        source.delete()
    }
}
```

Please note that you as a developer have to take care about doing proper error handling, i.e. rolling back all
previous changes done within a `Migration.run()` call.
In other words, it's your responsibility to make things transactional.
Migrator doesn't provide any Atomicity, Consistency, Isolation or Durability (ACID) guarantees.
THe only guarantee you get is that all Migrations run squentially (one after each other, sorted by `Migration.version`)
and if one migration succeeded Migrator wont run this migration again.
If you don't want to a migration to succeed, just throw an exception.
In that case (unless exception is caught somewhere) the app will crash.
If `Migration.run()` completes, Migrator marks this Migration as successful (unsuccessful means any exception is thrown).

Next you have to setup the `Migrator` instance
```kotlin
val migrator = Migrator( context, V1Migration(), V2Migration() )

migrator.runMigrationsIfNeeded() // Actually runs the migration. You may want to do that on a background thread.
```

### Threading
`Migrator` doesn't run any `Migration` on a specific background thread.
It just runs all migrations sequential (one after each other, sorted by `Migration.version`) on the Thread that called
`Migrator.runMigrationsIfNeeded()`.
It is your responsibility as a developer to run it the way you need it.
For example with [RxJava](https://github.com/ReactiveX/RxJava) you could run it on a background thread like this:

```kotlin
val completable = Completable.fromCallable {
    val migrator = Migrator( context, V1Migration(), V2Migration() )
    migrator.runMigrationsIfNeeded()
}.subscribeOn(Schedulers.io())
```

or with Kotlin Coroutines

```kotlin
suspend fun runMigrationInBackground(){
    val migrator = Migrator( context, V1Migration(), V2Migration() )
    migrator.runMigrationsIfNeeded()
}
```

In a nutshell, all that migrator does is run `Migrations` one after each other and guarantees that if one `Migration`
completed it wont execute a `Migration` again next time you call `migrator.runMigrationsIfNeeded()`.